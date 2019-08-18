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
    private Map<String, ConfigOption> options = new HashMap<>();
    private YamlConfiguration configuration = new YamlConfiguration();

    private boolean initialized = false;
    public ConfigOption<Integer> NEW_CLAIM_RADIUS = createOption("new_claim_radius", 5);
    public ConfigOption<String> CLAIM_TOOL = createOption("claim_tool", "GOLDEN_SHOVEL");
    public ConfigOption<String> INSPECT_TOOL = createOption("inspect_tool", "STICK");

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

    ConfigNode(YamlConfiguration section)
    {
        for (Map.Entry<String, ConfigOption> entry : options.entrySet())
        {
            Object configValue = section.get(entry.getKey());

            //Add missing or invalid config node
            if (configValue == null)
            {
                System.out.println("[GP] missing config node " + entry.getKey());
                configuration.set(entry.getKey(), entry.getValue().getValue());
                continue;
            }

            //TODO combine, after debugging whether YamlConfiguration reads in types as expected or not...
            if (!entry.getValue().getValue().getClass().isAssignableFrom(configValue.getClass()))
            {
                System.out.println("[GP] invalid config value for " + entry.getKey() +
                        ". Type was " + configValue.getClass().getName() + ", should be " + entry.getValue().getClass().getName());
                configuration.set(entry.getKey(), entry.getValue().getValue());
                continue;
            }

            entry.getValue().value = section.get(entry.getKey());
            configuration.set(entry.getKey(), entry.getValue().getValue());
        }
    }

    //TODO: have factory obtain this and/or somehow return after constructor (as it's only needed to update config after loading)
    public YamlConfiguration getConfiguration()
    {
        return configuration;
    }

    //    private boolean setValue(String key, Number value)
//    {
//        Object optionValue = options.get(key).getValue();
//
//        if (optionValue instanceof Integer)
//            value.byteValue()
//    }
}

class ConfigOption<T>
{
    T value;
    String key;

    ConfigOption(String key, T value)
    {
        this.key = key;
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }
}


