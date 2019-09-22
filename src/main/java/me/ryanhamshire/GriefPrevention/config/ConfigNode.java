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
    Map<String, ConfigEntry> entries = new HashMap<>();

    protected ConfigEntry<String> createOption(String key, String value)
    {
        ConfigEntry<String> option = new ConfigEntry<>(key, value);
        entries.put(key, option);
        return option;
    }

    protected ConfigEntry<Integer> createOption(String key, Integer value)
    {
        ConfigEntry<Integer> option = new ConfigEntry<>(key, value);
        entries.put(key, option);
        return option;
    }
}


