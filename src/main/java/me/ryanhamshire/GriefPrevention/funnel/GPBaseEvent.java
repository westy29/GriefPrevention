package me.ryanhamshire.GriefPrevention.funnel;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.Metadatable;

import java.util.Map;

/**
 * Foundation event
 * <p>
 * Created on 2/23/2017.
 *
 * @author RoboMWM
 */
public class GPBaseEvent extends Event implements Cancellable
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    private boolean cancel = false;

    @Override
    public boolean isCancelled()
    {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancel = cancelled;
    }

    private Event baseEvent;
    private Metadatable source;
    //Most of these events are caused by an entity
    //Thus, we'll store it to avoid needless instanceof calls
    private Entity sourceEntity;
    private Location location;
    private Metadatable target;
    private Map<EventOption, Boolean> options;

    /**
     * Called when something other than an entity is not the source of this event (e.g. another block)
     *
     * @param baseEvent
     * @param source    Can be null
     * @param location
     * @param target
     */
    public GPBaseEvent(Event baseEvent, Metadatable source, Location location, Metadatable target)
    {
        this.baseEvent = baseEvent;
        this.location = location;
        this.target = target;
        this.source = source;
    }

    /**
     * @param baseEvent
     * @param sourceEntity Can be null. Set as both the source and cause of the event.
     * @param location
     * @param target
     */
    public GPBaseEvent(Event baseEvent, Entity sourceEntity, Location location, Metadatable target)
    {
        this.baseEvent = baseEvent;
        this.sourceEntity = sourceEntity;
        this.source = sourceEntity;
        this.location = location;
        this.target = target;
    }

    public Event getBaseEvent()
    {
        return baseEvent;
    }

    /**
     * Gets the thing (usually an entity or block) that caused this event.
     *
     * @return
     */
    public Metadatable getSource()
    {
        return source;
    }

    /**
     * Gets the entity that caused this event
     *
     * @return null if no source, or if source is not an entity
     */
    public Entity getSourceEntity()
    {
        return sourceEntity;
    }

    //TODO: maybe should be private?
    public boolean isPlayer()
    {
        return sourceEntity != null && sourceEntity.getType() == EntityType.PLAYER;
    }

    public Player getSourcePlayer()
    {
        if (isPlayer())
            return (Player)sourceEntity;
        return null;
    }

    //TODO: cache result
    public Block getSourceBlock()
    {
        if (source instanceof Block)
            return (Block)source;
        return null;
    }

    /**
     * Gets location of block/entity affected by this event.
     *
     * @return
     */
    public Location getLocation()
    {
        return location;
    }

    public Metadatable getTarget()
    {
        return target;
    }

    //TODO: setter
    public boolean getOption(EventOption option)
    {
        if (options == null)
            return true;
        Boolean b = options.get(option);
        if (b != null)
            return b;
        return true;
    }
}
