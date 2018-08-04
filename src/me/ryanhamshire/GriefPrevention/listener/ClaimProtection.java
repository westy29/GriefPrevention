package me.ryanhamshire.GriefPrevention.listener;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimPermission;
import me.ryanhamshire.GriefPrevention.events.funnel.GPBlockMutateTypeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created on 7/31/2018.
 *
 * @author RoboMWM
 */
public class ClaimProtection implements Listener
{
    private ClaimClerk claimClerk;

    public ClaimProtection(ClaimClerk claimClerk)
    {
        this.claimClerk = claimClerk;
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockMutate(GPBlockMutateTypeEvent event)
    {
        Player player = event.getPlayer();
        if (player == null)
        {
            //TODO: check explosions temp flag, etc.
            event.setCancelled(true);
            return;
        }

        Claim claim = claimClerk.getClaim(player, event.getLocation(), false);

        event.setCancelled(!claim.hasPermission(player, ClaimPermission.BUILD));
    }
}
