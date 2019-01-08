package me.ryanhamshire.GriefPrevention.funnel;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;

/**
 * Created on 3/11/2017.
 * Called when a block's state is modified.
 * Does NOT include blocks with toggleable (two) states, use GPBlockToggleDataEvent
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
public class GPBlockChangeStateEvent extends GPBaseEvent
{
    /**
     * Called when something other than an entity is not the source of this event (e.g. another block)
     *
     * @param baseEvent
     * @param source    Can be null
     * @param location
     * @param target
     */
    public GPBlockChangeStateEvent(Event baseEvent, Metadatable source, Location location, Metadatable target)
    {
        super(baseEvent, source, location, target);
    }

    /**
     * @param baseEvent
     * @param sourceEntity Can be null. Set as both the source and cause of the event.
     * @param location
     * @param target
     */
    public GPBlockChangeStateEvent(Event baseEvent, Entity sourceEntity, Location location, Metadatable target)
    {
        super(baseEvent, sourceEntity, location, target);
    }
}
