package me.ryanhamshire.GriefPrevention.enums;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created on 2/19/2019.
 *
 * @author RoboMWM
 */
public enum Config
{
    CLAIM__NEW_CLAIM_RADIUS(5, DataType.INT),
    CLAIM__CLAIM_TOOL("GOLDEN_SHOVEL", DataType.STRING),
    CLAIM__INSPECT_TOOL("STICK", DataType.STRING);

    private final DataType type;
    private Object value;
    private final String key;

    Config(Object value, DataType type)
    {
        this.type = type;
        this.value = value;
        this.key = name().substring(name().indexOf("__") + 1).replaceAll("__", ".");
    }

    public static YamlConfiguration initialize(YamlConfiguration file, String prefix)
    {
        for (Config thing : Config.values())
        {
            if (!thing.name().startsWith(prefix))
                continue;

            Object value = null;

            switch (thing.type)
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

enum DataType
{
    STRING,
    INT,
    DOUBLE;
}