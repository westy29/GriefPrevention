package me.ryanhamshire.GriefPrevention.mock;

import me.ryanhamshire.GriefPrevention.config.ConfigEntry;
import me.ryanhamshire.GriefPrevention.config.ConfigNode;

/**
 * Created on 9/22/2019.
 *
 * @author RoboMWM
 */
public class MockConfigNode extends ConfigNode
{
    public ConfigEntry<String> STRING_TEST = createOption("STRING_TEST", "hello world");
    public ConfigEntry<Integer> INTEGER_TEST = createOption("INTEGER_TEST", 43110);
}
