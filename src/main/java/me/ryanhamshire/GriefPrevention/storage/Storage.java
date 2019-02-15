/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention.storage;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.player.PlayerData;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 *
 * As of right now:
 * Storing data is asynchronous (void return, close() method)
 */
public interface Storage
{

    /**
     * Retrieves a set of all claims in storage
     * Usually synchronous since this is called once on server start
     *
     * @return a set of all claims
     */
    Set<Claim> getClaims();

    /**
     * Saves claim to storage
     *
     * @param claim
     */
    void saveClaim(Claim claim);

    /**
     * Deletes claim from storage
     *
     * @param claim
     */
    void deleteClaim(Claim claim);

    /**
     * Retrieves the playerData for the specified player from storage. Creates a new one if none exists.
     *
     * @param uuid player's uuid
     * @return An instantiated PlayerData object, or null if not found
     */
    Future<PlayerData> getPlayerData(UUID uuid);

    /**
     * Saves the playerData to storage
     *
     * @param playerData
     */
    void savePlayerData(PlayerData playerData);

    /**
     * Called when GriefPrevention is being disabled. Notifies the database to finish any saving tasks.
     */
    void close();
}
