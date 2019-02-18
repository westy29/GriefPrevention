package me.ryanhamshire.GriefPrevention.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on 2/17/2019.
 *
 * @author RoboMWM
 */
public class TaskQueue
{
    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private boolean run = true;

    public TaskQueue()
    {
        new Thread(() ->
        {
            while (run)
            {
                try
                {
                    tasks.take().run();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void add(Runnable runnable)
    {
        tasks.add(runnable);
    }

    public void stop()
    {
        add(() -> run = false);
    }
}
