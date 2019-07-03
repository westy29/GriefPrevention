package me.ryanhamshire.GriefPrevention.enums;

/**
 * Created on 7/3/2019.
 *
 * @author RoboMWM
 */
public class ConfigType
{
    private final DataType type;
    private Object value;
    private final String key;

    public ConfigType(Object value, DataType type, String name)
    {
        this.type = type;
        this.value = value;
        this.key = name;
    }

    public DataType getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }
}

enum DataType
{
    STRING,
    INT,
    DOUBLE;
}
