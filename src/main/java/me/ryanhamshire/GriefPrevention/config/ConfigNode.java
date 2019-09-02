package me.ryanhamshire.GriefPrevention.config;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 8/2/2019.
 *
 * @author RoboMWM
 */
public class ConfigNode
{
    private Map<String, ConfigOption> options;

    private boolean initialized = false;
    public ConfigOption<Integer> NEW_CLAIM_RADIUS = createOption("new_claim_radius", 5);
    public ConfigOption<String> CLAIM_TOOL = createOption("claim_tool", "GOLDEN_SHOVEL");
    public ConfigOption<String> INSPECT_TOOL = createOption("inspect_tool", "STICK");

    ConfigNode(Map<String, ConfigOption> options)
    {
        this.options = options;
    }

    private ConfigOption<String> createOption(String key, String value)
    {
        ConfigOption<String> option = new ConfigOption<>(key, value);
        options.put(key, option);
        return option;
    }

    private ConfigOption<Integer> createOption(String key, Integer value)
    {
        ConfigOption<Integer> option = new ConfigOption<>(key, value);
        options.put(key, option);
        return option;
    }

    //    private boolean setValue(String key, Number value)
//    {
//        Object optionValue = options.get(key).getValue();
//
//        if (optionValue instanceof Integer)
//            value.byteValue()
//    }
}


