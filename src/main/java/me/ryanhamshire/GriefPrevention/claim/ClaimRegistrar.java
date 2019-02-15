package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.event.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.storage.Storage;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 5/19/2017.
 *
 * @author RoboMWM
 */
public class ClaimRegistrar implements Listener
{
    private JavaPlugin plugin;
    private Set<Claim> claims;
    private Map<Long, Set<Claim>> chunksToClaimsMap = new ConcurrentHashMap<>();
    private Storage storage;
    private long lastUsedClaimId;

    public ClaimRegistrar(JavaPlugin plugin, Storage storage)
    {
        this.plugin = plugin;
        this.storage = storage;
        claims = storage.getClaims();
        lastUsedClaimId = System.currentTimeMillis();
    }

    /**
     * @return the "next" available claim ID.
     */
    public synchronized long nextClaimId()
    {
        long currentTime = System.currentTimeMillis();
        if (currentTime == lastUsedClaimId)
            ++currentTime;
        lastUsedClaimId = currentTime;
        return currentTime;
    }

    /**
     * Deletes a claim entirely, from storage and in internal register
     *
     * @param claim
     */
    public synchronized void deleteClaim(Claim claim)
    {
        storage.deleteClaim(claim);
        claims.remove(claim);

        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(claim);
        for (Long chunkHash : chunkHashes)
            this.chunksToClaimsMap.get(chunkHash).remove(claim);

        plugin.getServer().getPluginManager().callEvent(new ClaimDeletedEvent(claim));
    }


    //Utilities useful for claims

    /**
     * Gets the claim at a specific location
     *
     * @param ignoreDepth Whether a location underneath the claim should return the claim.
     * @param cachedClaim can be NULL, but will help performance if you have a reasonable guess about which claim the location is in
     */
    public synchronized Claim getClaim(Location location, boolean ignoreDepth, Claim cachedClaim)
    {
        //check cachedClaim guess first.  if it's in the datastore and the location is inside it, we're done
        if (cachedClaim != null && claims.contains(cachedClaim) && cachedClaim.contains(location, ignoreDepth))
            return cachedClaim;

        Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(ClaimUtils.getChunkHash(location));
        if (claimsInChunk == null)
            return null;
        for (Claim claim : claimsInChunk)
        {
            if (claim.contains(location, ignoreDepth))
                return claim;
        }

        return null;
    }

    /**
     * Get a collection of all land claims.
     * <p>
     * If you need to make changes, use provided methods like .deleteClaim() and .createClaim().
     * This will ensure primary memory (RAM) and secondary memory (disk, database) stay in sync
     *
     * @return a read-only access point for the list of all land claims
     */
    public synchronized Collection<Claim> getClaims()
    {
        return Collections.unmodifiableCollection(this.claims);
    }

    /**
     * Get a collection of all land claims owned by a Player (via UUID)
     *
     * @param uuid
     * @return
     */
    public synchronized Collection<Claim> getClaims(UUID uuid)
    {
        Set<Claim> claims = new HashSet<>();
        for (Claim claim : this.claims)
            if (uuid.equals(claim.getOwnerUUID()))
                claims.add(claim);

        return claims;
    }

    /**
     * Get a collection of all land claims within the specified chunk coordinates.
     * <p>
     * Note that there is no world parameter, so this will include all claims from all worlds in this chunk coordinate.
     *
     * @return a read-only access point for the list of all land claims
     */
    public Collection<Claim> getClaims(int chunkx, int chunkz)
    {
        Set<Claim> chunkClaims = this.chunksToClaimsMap.get(ClaimUtils.getChunkHash(chunkx, chunkz));
        if (chunkClaims != null)
        {
            return Collections.unmodifiableCollection(chunkClaims);
        }

        return Collections.unmodifiableCollection(new HashSet<>());
    }

    /**
     * Creates and registers a new claim
     *
     * @param firstCorner
     * @param secondCorner
     * @param ownerID      the owner of this new claim. Null designates administrative claim.
     * @return a ClaimClaimResult
     * @throws IllegalArgumentException if corners' worlds don't match
     * @throws Exception                if the newly-created claim was not able to be saved.
     * @see CreateClaimResult
     */
    public synchronized CreateClaimResult createClaim(Location firstCorner, Location secondCorner, UUID ownerID)
    {
        Location[] corners = ClaimUtils.retrieveSortedCorners(firstCorner, secondCorner);

        //create a new claim instance (but don't save it, yet)
        Claim claimCandidate = new Claim(corners[0], corners[1], ownerID, null, nextClaimId());

        //ensure this new claim won't overlap any existing claims
        for (Claim claim : this.claims)
        {
            //if we find an existing claim which will be overlapped
            if (ClaimUtils.overlaps(claimCandidate, claim))
            {
                //Failed, return conflicting claim
                return new CreateClaimResult(false, claim);
            }
        }

        //TODO: fire ClaimCreateEvent

        this.registerClaim(claimCandidate);

        //then return success along with reference to new claim
        return new CreateClaimResult(true, claimCandidate);
    }

    /**
     * @param claim Claim to resize
     * @param firstCorner
     * @param secondCorner
     * @return a CreateClaimResult, or null if the claim does not exist in the registrar
     * @throws IllegalArgumentException if corners' worlds don't match
     * @throws Exception                if the newly-created claim was not able to be saved.
     * @see CreateClaimResult
     */
    public synchronized CreateClaimResult resizeClaim(Claim claim, Location firstCorner, Location secondCorner)
    {
        if (!claims.contains(claim))
            return null;

        Location[] corners = ClaimUtils.retrieveSortedCorners(firstCorner, secondCorner);

        //retain original depth
        corners[0].setY(claim.getLesserBoundaryCorner().getBlockY());

        //create a new claim instance (but don't save it, yet)
        Claim claimCandidate = new Claim(corners[0], corners[1], claim.getOwnerUUID(), null, claim.getID());

        //ensure this new claim won't overlap any existing claims
        for (Claim existingClaim : this.claims)
        {
            //skip claim we are resizing
            if (existingClaim == claim)
                continue;

            //if we find an existing claim which will be overlapped
            if (ClaimUtils.overlaps(existingClaim, claimCandidate))
            {
                //Failed, return conflicting claim
                return new CreateClaimResult(false, existingClaim);
            }
        }

        //Extend original claim
        claim.setLesserBoundaryCorner(corners[0]);
        claim.setGreaterBoundaryCorner(corners[1]);

        //Remove the claim from the chunkhash map (may be unnecessary but likely helps avoid checking invalidated claims in a chunk)
        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(claim);
        for (Long chunkHash : chunkHashes)
            this.chunksToClaimsMap.get(chunkHash).remove(claim);

        //TODO: fire ClaimResizeEvent

        this.registerClaim(claim);

        return new CreateClaimResult(true, claim);
    }

    /**
     * Returns a set of claims near a given location
     *
     * @param location
     * @param radius   specified in blocks
     * @return a set of claims
     */
    public synchronized Set<Claim> getNearbyClaims(Location location, int radius)
    {
        Set<Claim> claims = new HashSet<Claim>();

        Chunk lesserChunk = location.getWorld().getChunkAt(location.subtract(radius, 0, radius));
        Chunk greaterChunk = location.getWorld().getChunkAt(location.add(radius, 0, radius));

        for (int chunk_x = lesserChunk.getX(); chunk_x <= greaterChunk.getX(); chunk_x++)
        {
            for (int chunk_z = lesserChunk.getZ(); chunk_z <= greaterChunk.getZ(); chunk_z++)
            {
                Chunk chunk = location.getWorld().getChunkAt(chunk_x, chunk_z);
                Long chunkID = ClaimUtils.getChunkHash(chunk.getBlock(0, 0, 0).getLocation());
                Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(chunkID);
                if (claimsInChunk != null)
                {
                    for (Claim claim : claimsInChunk)
                    {
                        if (claim.getLesserBoundaryCorner().getWorld().equals(location.getWorld()))
                        {
                            claims.add(claim);
                        }
                    }
                }
            }
        }

        return claims;
    }

    //Used internally to register a claim
    //Registers both the claim and its chunk hash to the respective maps, as well as saving to storage
    private void registerClaim(Claim newClaim)
    {
        storage.saveClaim(newClaim);
        this.claims.add(newClaim);
        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(newClaim);
        for (Long chunkHash : chunkHashes)
        {
            Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(chunkHash);
            if (claimsInChunk == null)
            {
                claimsInChunk = new HashSet<>();
                this.chunksToClaimsMap.put(chunkHash, claimsInChunk);
            }
            claimsInChunk.add(newClaim);
        }
    }
}
