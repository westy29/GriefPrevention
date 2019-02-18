package me.ryanhamshire.GriefPrevention.listener;

import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import me.ryanhamshire.GriefPrevention.task.AccrueBlocksTask;
import me.ryanhamshire.GriefPrevention.util.TaskQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        startAccrualTask(event.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        stopAccrualTask(event.getPlayer());
    }

    public void startAccrualTask(Player player)
    {
        taskQueue.add(() ->
        {
            if (tasks.containsKey(player))
                tasks.remove(player).cancel();

            try
            {
                tasks.put(player,
                        new AccrueBlocksTask(
                                playerDataRegistrar,
                                player,
                                16,
                                playerDataRegistrar.getOrCreatePlayerData(player.getUniqueId()).get())
                                .runTaskTimer(plugin, 600L, 600L));
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        });
    }

    public void stopAccrualTask(Player player)
    {
        taskQueue.add(() ->
        {
            if (tasks.containsKey(player))
                tasks.remove(player).cancel();
        });
    }

    public void close()
    {
        taskQueue.stop();
    }
}
