package me.ryanhamshire.GriefPrevention.enums;

/**
 * Created on 7/3/2019.
 *
 * @author RoboMWM
 */
public class ConfigType
{
    private final DataType type;
    private final int intValue;
    private final double doubleValue;
    private final String stringValue;
    private final String key;

//    public ConfigType(Object value, DataType type, String name)
//    {
//        this.type = type;
//        this.value = value;
//        this.key = name;
//    }

    public ConfigType(int value, String name)
    {
        this.type = DataType.INT;
        this.intValue = value;
        this.key = name;
    }

    public int getInt()
    {
        return intValue;
    }

    public DataType getType()
    {
        return type;
    }

    public <Any> Any getValue()
    {
        switch (type)
        {
            case INT:
                return (Any)(Integer)intValue;
            case DOUBLE:
                return doubleValue;
            case STRING:
                return stringValue;
            default:
                return null;
        }
    }
}

enum DataType
{
    STRING,
    INT,
    DOUBLE;
}
