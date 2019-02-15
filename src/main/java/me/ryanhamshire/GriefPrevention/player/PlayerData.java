/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2011 Ryan Hamshire

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

package me.ryanhamshire.GriefPrevention.player;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;

import java.util.UUID;

//holds all of a player's claim-related attributes
public class PlayerData
{
    //the player's uuid
    private UUID uuid;

    //how many claim blocks the player has earned via play time
    private int accruedClaimBlocks;

    //how many claim blocks the player has been gifted/purchased
    private int bonusClaimBlocks;

    public PlayerData(UUID uuid, int accruedClaimBlocks, int bonusClaimBlocks)
    {
        this.uuid = uuid;
        this.accruedClaimBlocks = accruedClaimBlocks;
        this.bonusClaimBlocks = bonusClaimBlocks;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    //the number of claim blocks a player has available for claiming land
    //utility method - requires the ClaimRegistrar since it needs to calculate against consumed claims
    public synchronized int getRemainingClaimBlocks(ClaimRegistrar claimRegistrar)
    {
        int remainingBlocks = this.getAccruedClaimBlocks() + this.getBonusClaimBlocks();

        for (Claim claim : claimRegistrar.getClaims(this.uuid))
            remainingBlocks -= claim.getArea();

        return remainingBlocks;
    }

    public int getAccruedClaimBlocks()
    {
        return accruedClaimBlocks;
    }

    public void setAccruedClaimBlocks(int accruedClaimBlocks)
    {
        this.accruedClaimBlocks = accruedClaimBlocks;
    }

    public int getBonusClaimBlocks()
    {
        return bonusClaimBlocks;
    }

    public void setBonusClaimBlocks(int bonusClaimBlocks)
    {
        this.bonusClaimBlocks = bonusClaimBlocks;
    }
}