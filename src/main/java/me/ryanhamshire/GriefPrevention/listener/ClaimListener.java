package me.ryanhamshire.GriefPrevention.listener;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimPermission;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import me.ryanhamshire.GriefPrevention.funnel.GPBlockChangeTypeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 6/22/2018.
 *
 * @author RoboMWM
 */
public class ClaimListener implements Listener
{
    private ClaimRegistrar claimRegistrar;

    public ClaimListener(JavaPlugin plugin, ClaimRegistrar claimRegistrar)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.claimRegistrar = claimRegistrar;
    }

    private Map<Metadatable, Claim> claimCache = new HashMap<>();

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        claimCache.remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onClaimBuildBreak(GPBlockChangeTypeEvent event)
    {
        Claim claim = claimRegistrar.getClaim(event.getLocation(), false, claimCache.get(event.getSource()));
        if (claim == null)
        {
            event.setCancelled(applyWildernessRules(event));
            return;
        }

        //Caused by player
        if (event.isPlayer())
        {
            event.setCancelled(!claim.hasPermission(event.getSourcePlayer(), ClaimPermission.BUILD));
            return;
        }

        //Allow if natural grief is enabled
        event.setCancelled(!claim.isNaturalGriefAllowed());

        if (event.getSourceBlock() != null)
        {
            switch (event.getSourceBlock().getType())
            {
                case FIRE:
                    event.setCancelled(true);
            }
        }
    }

    private boolean applyWildernessRules(GPBlockChangeTypeEvent event)
    {
        //fire spread & burn
        //explosions above sea level
        //etc.
        return false;
    }
}
