package me.ryanhamshire.GriefPrevention.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2/19/2019.
 *
 * @author RoboMWM
 */
public class ClaimConfig
{
    private boolean initialized;
//    NEW_CLAIM_RADIUS(5, DataType.INT),
//    CLAIM_TOOL("GOLDEN_SHOVEL", DataType.STRING),
//    INSPECT_TOOL("STICK", DataType.STRING);

    private Map<String, ConfigType> configMap = new HashMap<>();

    private int getInt(String key, int def)
    {
        if (!initialized)
            configMap.putIfAbsent(key, new ConfigType(def, key));
        return configMap.get(key).getInt();
    }


    private final ConfigType configType;

    ClaimConfig(Object value, DataType type)
    {
        this.configType = new ConfigType(value, type, name());
    }

    public ConfigType getConfigType()
    {
        return configType;
    }

    public int getNewClaimRadius()
    {
        return getInt("New_Claim_Radius", 5);
    }
}