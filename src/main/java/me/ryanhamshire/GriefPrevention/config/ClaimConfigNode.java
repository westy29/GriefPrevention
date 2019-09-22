package me.ryanhamshire.GriefPrevention.config;

/**
 * Created on 9/2/2019.
 *
 * @author RoboMWM
 */
public class ClaimConfigNode extends ConfigNode
{
    public ConfigEntry<Integer> NEW_CLAIM_RADIUS = createOption("new_claim_radius", 5);
    public ConfigEntry<String> CLAIM_TOOL = createOption("claim_tool", "GOLDEN_SHOVEL");
    public ConfigEntry<String> INSPECT_TOOL = createOption("inspect_tool", "STICK");
}
