package me.ryanhamshire.GriefPrevention.events.funnel;

/**
 * Created on 3/11/2017.
 * Called when a block's state is modified.
 * Does NOT include blocks with toggleable (two) states, use GPToggleBlockStateEvent
 * Corresponds to /containertrust (a.k.a. /farmtrust, /chesttrust)
 * <p>
 * Crop farming (placing/breaking crops) is included here.
 * <p>
 * Includes:
 * Chest access
 * Item frame - rotation
 * Redstone settings
 * Uprooting crops
 * Planting crops
 * Anvil use
 *
 * @author RoboMWM
 */
public class GPMutateBlockStateEvent
{
}
