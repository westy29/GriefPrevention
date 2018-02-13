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

package me.ryanhamshire.GriefPrevention.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//represents a player claim
public class Claim
{
	//two locations, which together define the boundaries of the claim
	//note that the upper Y value is always ignored, because claims ALWAYS extend up to the sky
	private Location lesserBoundaryCorner;
	private Location greaterBoundaryCorner;
	Long id = null;
    private UUID ownerUUID;
    private HashMap<UUID, ClaimPermission> playerIDToClaimPermissionMap = new HashMap<UUID, ClaimPermission>(); //permissions for this claim, see ClaimPermission class
    private boolean naturalGriefAllowed = false;

	/**
     * Unique ID number of the claim. (Should) never change.
	 * @return Unique ID of this claim.
	 */
	public Long getID()
	{
		return this.id;
	}

	/**
	 * Returns the UUID of the player that owns this claim. For admin claims, this will return null
	 * @return the owner's UUID
	 */
	public UUID getOwnerUUID()
	{
		return ownerUUID;
	}

    /**
     * The owner and managers can temporarily enable natural "grief" (explosions, fire spread, etc.)
     * @return whether this claim is allowing natural grief to occur
     */
	public boolean isNaturalGriefAllowed()
	{
		return naturalGriefAllowed;
	}

    /**
     * @param naturalGriefAllowed If true, "flags" the claim as allowing natural grief
     */
	public void setNaturalGriefAllowed(boolean naturalGriefAllowed)
	{
		this.naturalGriefAllowed = naturalGriefAllowed;
	}

	//whether or not this is an administrative claim
	//administrative claims are created and maintained by players with the griefprevention.adminclaims permission.
	public boolean isAdminClaim()
	{
		return (this.ownerUUID == null);
	}

	//copy constructor
	public Claim(Claim claim, UUID ownerUUID)
	{
		this.lesserBoundaryCorner = claim.lesserBoundaryCorner;
		this.greaterBoundaryCorner = claim.greaterBoundaryCorner;
		this.playerIDToClaimPermissionMap = claim.playerIDToClaimPermissionMap;
		this.id = claim.id;

		this.ownerUUID = ownerUUID;
	}
	
	//main constructor.  note that only creating a claim instance does nothing - a claim must be added to the storage store to be effective
	public Claim(Location lesserBoundaryCorner, Location greaterBoundaryCorner, UUID ownerUUID, List<UUID> builderIDs, List<UUID> containerIDs, List<UUID> accessorIDs, List<UUID> managerIDs, Long id)
	{
		//id
		this.id = id;
		
		//store corners
		this.lesserBoundaryCorner = lesserBoundaryCorner;
		this.greaterBoundaryCorner = greaterBoundaryCorner;
		
		//owner
		this.ownerUUID = ownerUUID;
		
		//other permissions
		for(UUID builderID : builderIDs)
		{
			this.playerIDToClaimPermissionMap.put(builderID, ClaimPermission.Build);
		}
		
		for(UUID containerID : containerIDs)
		{
			this.playerIDToClaimPermissionMap.put(containerID, ClaimPermission.Inventory);
		}
		
		for(UUID accessorID : accessorIDs)
		{
			this.playerIDToClaimPermissionMap.put(accessorID, ClaimPermission.Access);
		}
		
		for(UUID managerID : managerIDs)
		{
			this.playerIDToClaimPermissionMap.put(managerID, ClaimPermission.Manage);
		}
	}
	
	//measurements.  all measurements are in blocks

    /**
     * @return Area of claim, in blocks
     */
	public int getArea()
	{
		return this.getWidth() * this.getHeight();
	}

    /**
     * @return Width of claim, in blocks
     */
	public int getWidth()
	{
		return this.greaterBoundaryCorner.getBlockX() - this.lesserBoundaryCorner.getBlockX() + 1;		
	}

    /**
     * @return Height of claim, in blocks
     */
	public int getHeight()
	{
		return this.greaterBoundaryCorner.getBlockZ() - this.lesserBoundaryCorner.getBlockZ() + 1;		
	}

    /**
     * Distance check for claims. Distance in this case is a band around the outside of the claim rather then euclidean distance
     * @param location Location in question. Height (y value) is effectively ignored.
     * @param howNear distance in blocks to check
     * @return
     */
	public boolean isNear(Location location, int howNear)
	{
		Claim claim = new Claim
			(new Location(this.lesserBoundaryCorner.getWorld(), this.lesserBoundaryCorner.getBlockX() - howNear, this.lesserBoundaryCorner.getBlockY(), this.lesserBoundaryCorner.getBlockZ() - howNear),
			 new Location(this.greaterBoundaryCorner.getWorld(), this.greaterBoundaryCorner.getBlockX() + howNear, this.greaterBoundaryCorner.getBlockY(), this.greaterBoundaryCorner.getBlockZ() + howNear),
			 null, new ArrayList<UUID>(), new ArrayList<UUID>(), new ArrayList<UUID>(), new ArrayList<UUID>(), null);
		
		return claim.contains(location, true);
	}

	//grants a permission for a player or the public
	public void setPermission(String playerID, ClaimPermission permissionLevel)
	{
		this.playerIDToClaimPermissionMap.put(UUID.fromString(playerID), permissionLevel);
	}

	//revokes a permission for a player or the public
	public void dropPermission(String playerID)
	{
		this.playerIDToClaimPermissionMap.remove(playerID.toLowerCase());
	}

	//clears all permissions (except owner of course)
	public void clearPermissions()
	{
		this.playerIDToClaimPermissionMap.clear();
	}

	//gets ALL permissions
    @Deprecated
	public void getPermissions(ArrayList<String> builders, ArrayList<String> containers, ArrayList<String> accessors, ArrayList<String> managers)
	{
		//loop through all the entries in the hash map
		Iterator<Map.Entry<UUID, ClaimPermission>> mappingsIterator = this.playerIDToClaimPermissionMap.entrySet().iterator();
		while(mappingsIterator.hasNext())
		{
			Map.Entry<UUID, ClaimPermission> entry = mappingsIterator.next();

			//build up a list for each permission level
			if(entry.getValue() == ClaimPermission.Build)
			{
				builders.add(entry.getKey().toString());
			}
			else if(entry.getValue() == ClaimPermission.Inventory)
			{
				containers.add(entry.getKey().toString());
			}
			else if (entry.getValue() == ClaimPermission.Access)
			{
				accessors.add(entry.getKey().toString());
			}
			else if (entry.getValue() == ClaimPermission.Manage)
			{
				managers.add(entry.getKey().toString());
			}
		}
	}

    /**
     * @return a copy of the location representing lower x, y, z limits
     */
	public Location getLesserBoundaryCorner()
	{
		return this.lesserBoundaryCorner.clone();
	}

	//Used for resizing, obviously
    public void setLesserBoundaryCorner(Location lesserBoundaryCorner)
    {
        this.lesserBoundaryCorner = lesserBoundaryCorner;
    }

    public void setGreaterBoundaryCorner(Location greaterBoundaryCorner)
    {
        this.greaterBoundaryCorner = greaterBoundaryCorner;
    }

    /**
     * NOTE: remember upper Y will always be ignored, all claims always extend to the sky
     * @return returns a copy of the location representing upper x, y, z limits
     */
	public Location getGreaterBoundaryCorner()
	{
		return this.greaterBoundaryCorner.clone();
	}

	/**
	 * whether or not a location is in or under a claim. (Ignores height.)
	 * @param location
	 * @return
	 */
	public boolean contains(Location location)
	{
		return contains(location, true);
	}

	/**
	 * whether or not a location is in a claim
	 * @param location
	 * @param ignoreHeight true means location UNDER the claim will return TRUE
	 * @return
	 */
	public boolean contains(Location location, boolean ignoreHeight)
    {
        //not in the same world implies false
        if (!location.getWorld().equals(this.lesserBoundaryCorner.getWorld())) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        //main check
        return (ignoreHeight || y >= this.lesserBoundaryCorner.getY()) &&
                x >= this.lesserBoundaryCorner.getX() &&
                x < this.greaterBoundaryCorner.getX() + 1 &&
                z >= this.lesserBoundaryCorner.getZ() &&
                z < this.greaterBoundaryCorner.getZ() + 1;
    }

    /**
     * used internally to prevent overlaps when creating claims
     * @param otherClaim
     * @return whether or not two claims overlap
     */
	public boolean overlaps(Claim otherClaim)
	{
		//NOTE:  if trying to understand this makes your head hurt, don't feel bad - it hurts mine too.  
		//try drawing pictures to visualize test cases.
		
		if(!this.lesserBoundaryCorner.getWorld().equals(otherClaim.getLesserBoundaryCorner().getWorld())) return false;
		
		//first, check the corners of this claim aren't inside any existing claims
		if(otherClaim.contains(this.lesserBoundaryCorner, false)) return true;
		if(otherClaim.contains(this.greaterBoundaryCorner, false)) return true;
		if(otherClaim.contains(new Location(this.lesserBoundaryCorner.getWorld(), this.lesserBoundaryCorner.getBlockX(), 0, this.greaterBoundaryCorner.getBlockZ()), false)) return true;
		if(otherClaim.contains(new Location(this.lesserBoundaryCorner.getWorld(), this.greaterBoundaryCorner.getBlockX(), 0, this.lesserBoundaryCorner.getBlockZ()), false)) return true;
		
		//verify that no claim's lesser boundary point is inside this new claim, to cover the "existing claim is entirely inside new claim" case
		if(this.contains(otherClaim.getLesserBoundaryCorner(), false)) return true;
		
		//verify this claim doesn't band across an existing claim, either horizontally or vertically		
		if(	this.getLesserBoundaryCorner().getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() && 
			this.getLesserBoundaryCorner().getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() && 
			this.getLesserBoundaryCorner().getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
			this.getGreaterBoundaryCorner().getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
			return true;
		
		if(	this.getGreaterBoundaryCorner().getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() && 
			this.getGreaterBoundaryCorner().getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() && 
			this.getLesserBoundaryCorner().getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
			this.getGreaterBoundaryCorner().getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
			return true;
		
		if(	this.getLesserBoundaryCorner().getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() && 
			this.getLesserBoundaryCorner().getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() && 
			this.getLesserBoundaryCorner().getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
			this.getGreaterBoundaryCorner().getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
			return true;

		if(	this.getGreaterBoundaryCorner().getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() &&
			this.getGreaterBoundaryCorner().getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() &&
			this.getLesserBoundaryCorner().getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
			this.getGreaterBoundaryCorner().getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
			return true;

		return false;
	}

    /**
     * @return the chunks within this claim
     */
    public ArrayList<Chunk> getChunks()
    {
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        
        World world = this.getLesserBoundaryCorner().getWorld();
        Chunk lesserChunk = this.getLesserBoundaryCorner().getChunk();
        Chunk greaterChunk = this.getGreaterBoundaryCorner().getChunk();
        
        for(int x = lesserChunk.getX(); x <= greaterChunk.getX(); x++)
        {
            for(int z = lesserChunk.getZ(); z <= greaterChunk.getZ(); z++)
            {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        
        return chunks;
    }
}
