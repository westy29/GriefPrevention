package me.ryanhamshire.GriefPrevention.task;

import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 2/17/2019.
 *
 * @author RoboMWM
 */
public class AccrueBlocksTask extends BukkitRunnable
{
    private PlayerDataRegistrar playerDataRegistrar;
    private PlayerData playerData;
    private Player player;
    private Location previousLocation;
    private int accrualAmount;

    public AccrueBlocksTask(PlayerDataRegistrar registrar, Player player, int amount, PlayerData playerData)
    {
        this.playerDataRegistrar = registrar;
        this.playerData = playerData;
        this.player = player;
        this.previousLocation = player.getLocation();
        this.accrualAmount = amount;
    }

    @Override
    public void run()
    {
        Location location = player.getLocation();
        if (location.getWorld() == previousLocation.getWorld() &&
        previousLocation.distanceSquared(location) < 9)
            return;

        int blocks = playerData.getAccruedClaimBlocks() + 16;
        if (blocks > 80000) //todo: config
            return;

        playerData.setAccruedClaimBlocks(blocks);
        playerDataRegistrar.savePlayerData(playerData);
    }
}
