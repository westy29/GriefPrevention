package me.ryanhamshire.GriefPrevention.events.funnel;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
 * Fires the respective funnel GP events, and handles cancellation
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

    private boolean callEvent(GPBaseEvent event)
    {
        Cancellable baseEvent = null;

        //GPevent.isCancelled() = baseevent.isCancelled()
        if (event.getBaseEvent() instanceof Cancellable)
        {
            baseEvent = (Cancellable)event.getBaseEvent();
            event.setCancelled(baseEvent.isCancelled());
        }

        plugin.getServer().getPluginManager().callEvent(event);

        //baseevent.isCancelled() = GPevent.isCancelled()
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
        callEvent(new GPMutateBlockTypeEvent(event, event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onBlockBreak(BlockBreakEvent event)
    {
        callEvent(new GPMutateBlockTypeEvent(event, event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onPaintingPlace(HangingPlaceEvent event)
    {
        //TODO: block location, or entity location? Big_Scary used entity location
        callEvent(new GPMutateBlockTypeEvent(event, event.getPlayer(), event.getEntity().getLocation(), event.getEntity()));
    }

    @EventHandler(priority = LOWEST)
    private void onPaintingBreak(HangingBreakEvent event)
    {
        Entity destroyerEntity = null;
        if (event instanceof HangingBreakByEntityEvent)
        {
            HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent)event;
            destroyerEntity = entityEvent.getRemover();
        }

        callEvent(new GPMutateBlockTypeEvent(event, destroyerEntity, event.getEntity().getLocation(), event.getEntity()));
    }

    @EventHandler(priority = LOWEST)
    private void onBlockLikeEntityDamage(EntityDamageByEntityEvent event)
    {
        switch (event.getEntityType())
        {
            case ITEM_FRAME:
            case ARMOR_STAND:
            case ENDER_CRYSTAL:
                callEvent(new GPMutateBlockTypeEvent(event, event.getDamager(), event.getEntity().getLocation(), event.getEntity()));
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
            if (callWithoutCancelingEvent(new GPMutateBlockTypeEvent(event, event.getEntity(), block.getLocation(), block)))
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
            if (callWithoutCancelingEvent(new GPMutateBlockTypeEvent(event, event.getBlock(), block.getLocation(), block)))
                blocksToRemove.add(block);
        }
        event.blockList().removeAll(blocksToRemove);
    }

    @EventHandler(priority = LOWEST)
    private void onEntityFormBlock(EntityBlockFormEvent event) //Frost walker
    {
        callEvent(new GPMutateBlockTypeEvent(event, event.getEntity(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onBlockBurn(BlockBurnEvent event)
    {
        callEvent(new GPMutateBlockTypeEvent(event, event.getIgnitingBlock(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onFireSpread(BlockSpreadEvent event)
    {
        if (event.getSource().getType() != Material.FIRE) //Ignore other blocks like vines, grass, etc.
            return;
        callEvent(new GPMutateBlockTypeEvent(event, event.getSource(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onVehicleDamage(VehicleDamageEvent event)
    {
        //TODO: entity damage
        //callEvent(new GPMutateBlockTypeEvent(event, event.getAttacker(), event.getVehicle().getLocation(), event.getAttacker()));
    }
}
