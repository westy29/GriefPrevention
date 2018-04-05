package me.ryanhamshire.GriefPrevention.claim;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 4/4/2018.
 *
 * @author RoboMWM
 */
public class ClaimUtils
{
    private ClaimUtils(){} //do not instantiate

    /**
     * Gets an almost-unique, persistent identifier for a chunk
     * @return the hash of the chunk
     */
    public static Long getChunkHash(long chunkx, long chunkz)
    {
        return (chunkz ^ (chunkx << 32));
    }

    /**
     * Gets an almost-unique, persistent identifier for the chunk within this location
     * @return the hash of the chunk
     */
    public static Long getChunkHash(Location location)
    {
        return getChunkHash(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static Set<Long> getChunkHashes(Claim claim)
    {
        Location lesserCorner = claim.getLesserBoundaryCorner();
        Location greaterCorner = claim.getGreaterBoundaryCorner();
        Set<Long> hashes = new HashSet<>();
        int smallX = lesserCorner.getBlockX() >> 4;
        int smallZ = lesserCorner.getBlockZ() >> 4;
        int largeX = greaterCorner.getBlockX() >> 4;
        int largeZ = greaterCorner.getBlockZ() >> 4;

        for(int x = smallX; x <= largeX; x++)
        {
            for(int z = smallZ; z <= largeZ; z++)
            {
                hashes.add(getChunkHash(x, z));
            }
        }

        return hashes;
    }


    /**
     * Does claim overlap otherClaim?
     * @param claim The claim we wish to place, generally
     * @param otherClaim The claim that already exists in the world, generally
     * @return whether or not two claims overlap
     * @throws IllegalArgumentException if claim == otherClaim
     */
    public static boolean overlaps(Claim claim, Claim otherClaim)
    {
        Validate.isTrue(claim != otherClaim, "Cannot check if a claim is overlapping itself (Well, you can, but this is very likely a mistake.");

        Location lesserCorner = claim.getLesserBoundaryCorner();
        Location greaterCorner = claim.getGreaterBoundaryCorner();

        Location otherLesserCorner = otherClaim.getLesserBoundaryCorner();
        Location otherGreaterCorner = otherClaim.getGreaterBoundaryCorner();

        if(!lesserCorner.getWorld().equals(otherClaim.getLesserBoundaryCorner().getWorld())) return false;

        //first, check the corners (easy to determine if corners are within a claim).
        if(otherClaim.contains(lesserCorner, false)) return true;
        if(otherClaim.contains(greaterCorner, false)) return true;
        if(otherClaim.contains(new Location(lesserCorner.getWorld(), lesserCorner.getBlockX(), 0, greaterCorner.getBlockZ()), false)) return true;
        if(otherClaim.contains(new Location(lesserCorner.getWorld(), greaterCorner.getBlockX(), 0, lesserCorner.getBlockZ()), false)) return true;

        //verify that no claim's lesser boundary point is inside claim new claim, to cover the "existing claim is entirely inside new claim" case
        if(claim.contains(otherClaim.getLesserBoundaryCorner(), false)) return true;

        //verify claim claim doesn't band across an existing claim, either horizontally or vertically
        if(	lesserCorner.getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() &&
                lesserCorner.getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                lesserCorner.getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
                greaterCorner.getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
            return true;

        if(	greaterCorner.getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() &&
                greaterCorner.getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                lesserCorner.getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
                greaterCorner.getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
            return true;

        if(	lesserCorner.getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() &&
                lesserCorner.getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() &&
                lesserCorner.getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                greaterCorner.getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
            return true;

        if(	greaterCorner.getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() &&
                greaterCorner.getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() &&
                lesserCorner.getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                greaterCorner.getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
            return true;

        return false;
    }

    /**
     * Returns a set of the chunks fully contained within the claim (no partial chunks)
     * @return the chunks within this claim
     */
    public static Set<Chunk> getChunks(Claim claim)
    {
        Set<Chunk> chunks = new HashSet<>();

        World world = claim.getLesserBoundaryCorner().getWorld();
        Chunk lesserChunk = claim.getLesserBoundaryCorner().getChunk();
        Chunk greaterChunk = claim.getGreaterBoundaryCorner().getChunk();

        for(int x = lesserChunk.getX(); x <= greaterChunk.getX(); x++)
        {
            for(int z = lesserChunk.getZ(); z <= greaterChunk.getZ(); z++)
            {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    /**
     * Whether or not the given location is in this claim, ignoring y value.
     * @param location
     * @return
     */
    public static boolean isWithin(Claim claim, Location location)
    {
        return isWithin(claim, location, false);
    }

    /**
     * Whether or not the given location is in this claim
     * @param location
     * @param includeHeight Whether a location underneath the claim should be considered within the claim.
     *                      If true, a location underneath the claim will return TRUE
     * @return
     */
    public static boolean isWithin(Claim claim, Location location, boolean includeHeight)
    {
        return claim.contains(location, includeHeight);
    }
}
