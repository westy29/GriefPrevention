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

package me.ryanhamshire.GriefPrevention.data;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.player.PlayerData;

import java.util.UUID;

/**
 * Stores and retrieves data from storage.
 */
public interface DataStore
{
    //pattern for unique user identifiers (UUIDs)
    //protected final static Pattern uuidpattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

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
     * Gets and increments the next available claim ID
     *
     * @return the next available claim ID. -1 if there's an issue
     */
    long nextClaimId();

    /**
     * Retrieves the playerData for the specified player from storage
     *
     * @param playerID
     * @return
     */
    PlayerData getPlayerData(UUID playerID);

    /**
     * Saves the playerData to storage
     *
     * @param playerID
     * @param playerData
     */
    void savePlayerData(UUID playerID, PlayerData playerData);
}
