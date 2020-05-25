package me.ryanhamshire.GriefPrevention.listener;

import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import me.ryanhamshire.GriefPrevention.util.TaskQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2/17/2019.
 *
 * @author RoboMWM
 */
public class PlayerEventHandler implements Listener
{
    private Plugin plugin;
    private Map<Player, BukkitTask> tasks = new HashMap<>();
    private TaskQueue taskQueue = new TaskQueue();
    private PlayerDataRegistrar playerDataRegistrar;

    public PlayerEventHandler(Plugin plugin, PlayerDataRegistrar playerDataRegistrar)
    {
        this.plugin = plugin;
        this.playerDataRegistrar = playerDataRegistrar;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
