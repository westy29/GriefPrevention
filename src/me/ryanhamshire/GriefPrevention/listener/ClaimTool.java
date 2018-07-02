package me.ryanhamshire.GriefPrevention.listener;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

/**
 * Created on 7/1/2018.
 *
 * @author RoboMWM
 */
public class ClaimTool implements Listener
{
    private ClaimClerk claimClerk;

    public ClaimTool(JavaPlugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerRightClickShovel(PlayerInteractEvent event)
    {
        //TODO: prevent accidental "spamming" use of claim tool (small cooldown)

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        //Ignore left clicks
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        //TODO: configurable.
        //TODO: 1.13
        if (event.getItem().getType() != Material.GOLD_SPADE)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            block = getTargetBlock(player, 100); //TODO: use view distance(?)

        if (block == null)
            return; //TODO: error message

        //If clicking within a claim (no matter the mode), inspect (and reset claim creation/extension)
        Claim claim = claimClerk.getClaim(player, block.getLocation(), false);
        if (claim != null)
        {
            //todo: visualize
            return;
        }

        //TODO: determine claim mode and etc.


    }

    private Block getTargetBlock(Player player, int maxDistance) throws IllegalStateException
    {
        Location eye = player.getEyeLocation();
        Material eyeMaterial = eye.getBlock().getType();
        boolean passThroughWater = (eyeMaterial == Material.WATER || eyeMaterial == Material.STATIONARY_WATER);
        BlockIterator iterator = new BlockIterator(player.getLocation(), player.getEyeHeight(), maxDistance);
        Block result = player.getLocation().getBlock().getRelative(BlockFace.UP);
        while (iterator.hasNext())
        {
            result = iterator.next();
            Material type = result.getType();
            if(type != Material.AIR &&
                    (!passThroughWater || type != Material.STATIONARY_WATER) &&
                    (!passThroughWater || type != Material.WATER) &&
                    type != Material.LONG_GRASS &&
                    type != Material.SNOW) return result;
        }

        return result;
    }
}
