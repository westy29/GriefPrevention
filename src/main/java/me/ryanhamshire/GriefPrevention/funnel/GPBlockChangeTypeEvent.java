package me.ryanhamshire.GriefPrevention.funnel;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;

/**
 * Fired when a block or block-like sourceEntity is placed or destroyed.
 * (I.e. the type is modified from/to AIR).
 * Block-like entities include item frames, armor stands, paintings, etc.
 * Corresponds to /trust (build trust)
 * <p>
 * Exception to this is placing/breaking crops - see GPBlockChangeStateEvent
 * <p>
 * Created on 2/23/2017.
 *
 * @author RoboMWM
 */
public class GPBlockChangeTypeEvent extends GPBaseEvent
{
    public GPBlockChangeTypeEvent(Event baseEvent, Entity sourceEntity, Location location, Metadatable target)
    {
        super(baseEvent, sourceEntity, location, target);
    }

    public GPBlockChangeTypeEvent(Event baseEvent, Metadatable source, Location location, Metadatable target)
    {
        super(baseEvent, source, location, target);
    }
}
