package me.ryanhamshire.GriefPrevention.funnel;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;

/**
 * Created on 3/11/2017.
 * Called when a block's data is toggled (only has two states, toggleable by player directly interacting).
 * Corresponds to /accesstrust (a.k.a. /toggletrust)
 * <p>
 * Opening doors
 * Opening trap doors
 * Toggling levers
 * Pressing buttons
 * TODO: include data from redstone mechanisms like comparators, repeaters, etc.? Or make separate event?
 * <p>
 * Pressure plates are ignored.
 *
 * @author RoboMWM
 */
public class GPBlockToggleDataEvent extends GPBaseEvent
{
    /**
     * Called when something other than an entity is not the source of this event (e.g. another block)
     *
     * @param baseEvent
     * @param source    Can be null
     * @param location
     * @param target
     */
    public GPBlockToggleDataEvent(Event baseEvent, Metadatable source, Location location, Metadatable target)
    {
        super(baseEvent, source, location, target);
    }

    /**
     * @param baseEvent
     * @param sourceEntity Can be null. Set as both the source and cause of the event.
     * @param location
     * @param target
     */
    public GPBlockToggleDataEvent(Event baseEvent, Entity sourceEntity, Location location, Metadatable target)
    {
        super(baseEvent, sourceEntity, location, target);
    }
}
