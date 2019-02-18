package me.ryanhamshire.GriefPrevention.funnel;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.EventPriority.LOWEST;

/**
 * What is the "funnel" package?
 * It simply collects and abstracts Bukkit API events into simpler events for GP to respond to.
 * I.e., all events that cause blocks to be broken (BlockBreakEvent, BlockFromToEvent, BlockExplodeEvent, EntityExplodeEvent, etc.) are fired as a "GPMutateTypeEvent".
 * These GP-labeled events cancel the wrapped Bukkit event when canceled.
 *
 * This not only simplifies GP's event handling, but allows addons to undo or alter GP's behavior if desired.
 *
 * TODO: Explain above better
 * TODO: listen at lowest priority?
 * TODO: call events even if canceled?
 * Created on 2/23/2017.
 *
 * @author RoboMWM
 */
public class BukkitToGPEventListener implements Listener
{
    private GriefPrevention plugin;

    public BukkitToGPEventListener(GriefPrevention griefPrevention)
    {
        this.plugin = griefPrevention;
    }

    /**
     *
     * @param event
     * @return true if event was canceled
     */
    private boolean callEvent(GPBaseEvent event)
    {
        Cancellable baseEvent = null;

        //GPevent.cancel = baseevent.isCancelled()
        if (event.getBaseEvent() instanceof Cancellable)
        {
            baseEvent = (Cancellable)event.getBaseEvent();
            event.setCancelled(baseEvent.isCancelled());
        }

        plugin.getServer().getPluginManager().callEvent(event);

        //baseevent.cancel = GPevent.isCancelled()
        if (baseEvent != null)
        {
            baseEvent.setCancelled(event.isCancelled());
        }

        return event.isCancelled();
    }

    private boolean callWithoutCancelingEvent(GPBaseEvent event)
    {
        plugin.getServer().getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    @EventHandler(priority = LOWEST)
    private void onBlockPlace(BlockPlaceEvent event)
    {
        callEvent(new GPBlockChangeTypeEvent(event, event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onBlockBreak(BlockBreakEvent event)
    {
        callEvent(new GPBlockChangeTypeEvent(event, event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onPaintingPlace(HangingPlaceEvent event)
    {
        callEvent(new GPBlockChangeTypeEvent(event, event.getPlayer(), event.getEntity().getLocation(), event.getEntity()));
    }

    @EventHandler(priority = LOWEST)
    private void onPaintingBreak(HangingBreakEvent event)
    {
        Entity destroyerEntity = null;
        if (event instanceof HangingBreakByEntityEvent)
        {
            HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent)event;
            destroyerEntity = entityEvent.getRemover();
            callEvent(new GPBlockChangeTypeEvent(event, destroyerEntity, event.getEntity().getLocation(), event.getEntity()));
            return;
        }

        //Only call event for instances where the hanging entity is directly destroyed (else you have floating stuff when the block is removed, etc.)
        switch (event.getCause())
        {
            case ENTITY:
            case EXPLOSION:
                callEvent(new GPBlockChangeTypeEvent(event, destroyerEntity, event.getEntity().getLocation(), event.getEntity()));
        }

    }

    @EventHandler(priority = LOWEST)
    private void onBlockLikeEntityDamage(EntityDamageByEntityEvent event)
    {
        switch (event.getEntityType())
        {
            case ITEM_FRAME:
            case ARMOR_STAND:
            case ENDER_CRYSTAL:
                callEvent(new GPBlockChangeTypeEvent(event, event.getDamager(), event.getEntity().getLocation(), event.getEntity()));
        }
    }

    @EventHandler(priority = LOWEST)
    private void onEntityExplode(EntityExplodeEvent event)
    {
        //Call an event for each block that's to-be-destroyed
        //Thus, the base event won't be canceled unless it's explicitly canceled
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : event.blockList())
        {
            if (callWithoutCancelingEvent(new GPBlockChangeTypeEvent(event, event.getEntity(), block.getLocation(), block)))
                blocksToRemove.add(block);
        }
        event.blockList().removeAll(blocksToRemove);
    }

    @EventHandler(priority = LOWEST)
    private void onBlockExplode(BlockExplodeEvent event) //largely same as above, but block as source
    {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : event.blockList())
        {
            if (callWithoutCancelingEvent(new GPBlockChangeTypeEvent(event, event.getBlock(), block.getLocation(), block)))
                blocksToRemove.add(block);
        }
        event.blockList().removeAll(blocksToRemove);
    }

    @EventHandler(priority = LOWEST)
    private void onEntityFormBlock(EntityBlockFormEvent event) //Frost walker
    {
        if (event.getEntity().getType() == EntityType.SNOWMAN) //ignore snowmen
            return;
        callEvent(new GPBlockChangeTypeEvent(event, event.getEntity(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onBlockBurn(BlockBurnEvent event)
    {
        callEvent(new GPBlockChangeTypeEvent(event, event.getIgnitingBlock(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onFireSpread(BlockSpreadEvent event)
    {
        if (event.getSource().getType() != Material.FIRE) //Ignore other blocks like vines, grass, etc.
            return;
        GPBaseEvent gpEvent = (new GPBlockChangeTypeEvent(event, event.getSource(), event.getBlock().getLocation(), event.getBlock()));

        //Extinguish fire within claim if it causes firespread to occur
        //Many players used to GP with fireplaces aren't aware that they aren't built to fire safety regulations
        //Also prevents a wildfire occurring if the claim is abandoned (manually or automatically)
        if (callEvent(gpEvent) && gpEvent.getOption(EventOption.REMOVE_SOURCE_FIRE_BLOCK))
            event.getSource().setType(Material.AIR, false);
    }

    @EventHandler(priority = LOWEST)
    private void onVehicleDamage(VehicleDamageEvent event)
    {
        //TODO: entity damage
        //callEvent(new GPBlockChangeTypeEvent(event, event.getAttacker(), event.getVehicle().getLocation(), event.getAttacker()));
    }
}
