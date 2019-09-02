package me.ryanhamshire.GriefPrevention.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/2/2019.
 *
 * @author RoboMWM
 */
public abstract class ConfigNode
{
    Map<String, ConfigOption> options = new HashMap<>();

    protected ConfigOption<String> createOption(String key, String value)
    {
        ConfigOption<String> option = new ConfigOption<>(key, value);
        options.put(key, option);
        return option;
    }

    protected ConfigOption<Integer> createOption(String key, Integer value)
    {
        ConfigOption<Integer> option = new ConfigOption<>(key, value);
        options.put(key, option);
        return option;
    }
}


