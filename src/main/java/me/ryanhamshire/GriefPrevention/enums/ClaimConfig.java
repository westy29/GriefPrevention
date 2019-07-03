package me.ryanhamshire.GriefPrevention.enums;

/**
 * Created on 2/19/2019.
 *
 * @author RoboMWM
 */
public enum ClaimConfig
{
    NEW_CLAIM_RADIUS(5, DataType.INT),
    CLAIM_TOOL("GOLDEN_SHOVEL", DataType.STRING),
    INSPECT_TOOL("STICK", DataType.STRING);

    private final ConfigType configType;

    ClaimConfig(Object value, DataType type)
    {
        this.configType = new ConfigType(value, type, name());
    }

    public ConfigType getConfigType()
    {
        return configType;
    }
}