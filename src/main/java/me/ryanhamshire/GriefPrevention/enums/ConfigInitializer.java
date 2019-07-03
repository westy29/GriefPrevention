package me.ryanhamshire.GriefPrevention.enums;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created on 7/3/2019.
 *
 * Initializes config files. May move to another class later.
 *
 * @author RoboMWM
 */
public class ConfigInitializer
{
    public static YamlConfiguration initialize(YamlConfiguration file, String prefix)
    {
        for (ClaimConfig thing : ClaimConfig.values())
        {
            if (!thing.name().startsWith(prefix))
                continue;

            Object value = null;

            switch (thing.getConfigType().getType())
            {
                case INT:
                    value = file.getInt(thing.key);
                    break;
                case STRING:
                    value = file.getString(thing.key);
                    break;
                case DOUBLE:
                    value = file.getDouble(thing.key);
                    break;
            }

            if (value == null)
                file.addDefault(thing.name(), thing.value);
            else
                thing.value = value;
        }

        file.options().copyDefaults(true);
        return file;
    }
}
