package me.ryanhamshire.GriefPrevention;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

/**
 * Created on 1/7/2019.
 *
 * Stripped-down version of https://github.com/RoboMWM/UsefulUtil
 *
 * @author RoboMWM
 */
public class UsefulUtil
{
    /**
     * Asynchronously save the specified string into a file
     *
     * @param plugin
     * @param storageFile File to store contents in
     * @param contents
     */
    public static void saveStringToFile(Plugin plugin, File storageFile, String contents)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //delete file if empty
                if (contents == null || contents.isEmpty())
                {
                    storageFile.delete();
                }

                try
                {
                    storageFile.getParentFile().mkdirs();
                    storageFile.delete(); //always overwrite file
                    storageFile.createNewFile();
                    Files.write(storageFile.toPath(), Collections.singletonList(contents), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                }
                catch (Exception e)
                {
                    plugin.getLogger().severe("Could not save " + storageFile.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
