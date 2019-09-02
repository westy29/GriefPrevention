package me.ryanhamshire.GriefPrevention.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created on 9/2/2019.
 *
 * @author RoboMWM
 */
public class ConfigNodeFactory
{
    /**
     * Returns a "tuple" containing the updated YamlConfiguration and the created ConfigNode
     * @param log A logger to log messages. Please pass your plugin's logger via plugin#getLogger
     * @param section The YamlConfiguration to load
     * @return
     */
    public AbstractMap.SimpleEntry<YamlConfiguration, ConfigNode> createConfigNode(Logger log, YamlConfiguration section)
    {
        Map<String, ConfigOption> options = new HashMap<>();

        for (Map.Entry<String, ConfigOption> entry : options.entrySet())
        {
            Object configValue = section.get(entry.getKey());

            //Add missing or invalid config node
            YamlConfiguration configuration = new YamlConfiguration();
            if (configValue == null)
            {
                log.info("missing config node " + entry.getKey());
                configuration.set(entry.getKey(), entry.getValue().getValue());
                continue;
            }

            //TODO combine, after debugging whether YamlConfiguration reads in types as expected or not...
            if (!entry.getValue().getValue().getClass().isAssignableFrom(configValue.getClass()))
            {
                log.info("invalid config value for " + entry.getKey() +
                        ". Type was " + configValue.getClass().getName() + ", should be " + entry.getValue().getClass().getName());
                configuration.set(entry.getKey(), entry.getValue().getValue());
                continue;
            }

            entry.getValue().value = section.get(entry.getKey());
            configuration.set(entry.getKey(), entry.getValue().getValue());
        }

        return new AbstractMap.SimpleEntry<>(section, new ConfigNode(options));
    }
}
