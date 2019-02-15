package me.ryanhamshire.GriefPrevention.enums;

import org.bukkit.entity.Player;

/**
 * Created on 2/15/2019.
 *
 * @author RoboMWM
 */
public enum Permission
{
    CLAIM_CREATE("griefprevention.claim.create");

    private String value;

    Permission(String value)
    {
        this.value = value;
    }

    public String value()
    {
        return value;
    }

    public boolean has(Player player)
    {
        return player.hasPermission(value);
    }

    public boolean hasNot(Player player)
    {
        return !has(player);
    }

    public boolean hasNot(Player player, Message error)
    {
        if (has(player))
            return false;
        error.send(player);
        return true;
    }
}
