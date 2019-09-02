package me.ryanhamshire.GriefPrevention.config;

/**
 * Created on 9/2/2019.
 *
 * @author RoboMWM
 */
public class ClaimConfigNode extends ConfigNode
{
    public ConfigOption<Integer> NEW_CLAIM_RADIUS = createOption("new_claim_radius", 5);
    public ConfigOption<String> CLAIM_TOOL = createOption("claim_tool", "GOLDEN_SHOVEL");
    public ConfigOption<String> INSPECT_TOOL = createOption("inspect_tool", "STICK");
}
