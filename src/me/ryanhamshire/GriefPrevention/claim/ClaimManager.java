package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.storage.Storage;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.message.Messages;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.visualization.Visualization;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationType;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 5/19/2017.
 *
 * @author RoboMWM
 */
public class ClaimManager
{
    private JavaPlugin plugin;
    private Set<Claim> claims;
    private ConcurrentHashMap<Long, Set<Claim>> chunksToClaimsMap = new ConcurrentHashMap<>();
    private Storage storage;
    private long lastUsedClaimId;

    public ClaimManager(JavaPlugin plugin, Storage storage)
    {
        this.plugin = plugin;
        this.storage = storage;
        claims.addAll(storage.getClaims());
        lastUsedClaimId = System.currentTimeMillis();
    }

    /**
     * @return the "next" available claim ID.
     */
    public long nextClaimId()
    {
        long currentTime = System.currentTimeMillis();
        if (currentTime == lastUsedClaimId)
            ++currentTime;
        lastUsedClaimId = currentTime;
        return currentTime;
    }

    public void transferClaim(Claim claim, UUID newOwnerID)
    {
        //determine current claim owner
        PlayerData ownerData = null;
        if(!claim.isAdminClaim())
        {
            ownerData = storage.getPlayerData(claim.ownerID);
        }

        //determine new owner
        PlayerData newOwnerData = null;

        if(newOwnerID != null)
        {
            newOwnerData = this.getPlayerData(newOwnerID);
        }

        //transfer
        //TODO: use copy constructor
        claim.setOwnerID(newOwnerID);
        this.saveClaim(claim);

        //adjust blocks and other records
        if(ownerData != null)
        {
            ownerData.getClaims().remove(claim);
        }

        if(newOwnerData != null)
        {
            newOwnerData.getClaims().add(claim);
        }
    }

    //adds a claim to the datastore, making it an effective claim
    public void addClaim(Claim newClaim, boolean writeToStorage)
    {
        //add it and mark it as added
        this.claims.add(newClaim);
        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(newClaim);
        for(Long chunkHash : chunkHashes)
        {
            Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(chunkHash);
            if(claimsInChunk == null)
            {
                claimsInChunk = new HashSet<>();
                this.chunksToClaimsMap.put(chunkHash, claimsInChunk);
            }
            claimsInChunk.add(newClaim);
        }

        //except for administrative claims (which have no owner), update the owner's playerData with the new claim
        if(!newClaim.isAdminClaim() && writeToStorage)
        {
            PlayerData ownerData = this.getPlayerData(newClaim.ownerID);
            ownerData.getClaims().add(newClaim);
        }

        //make sure the claim is saved to disk
        if(writeToStorage)
        {
            this.saveClaim(newClaim);
        }
    }

    public void deleteClaim(Claim claim)
    {
        claims.remove(claim);

        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(claim);
        for(Long chunkHash : chunkHashes)
        {
            this.chunksToClaimsMap.get(chunkHash).remove(claim);
        }

        storage.deleteClaim(claim);

        plugin.getServer().getPluginManager().callEvent(new ClaimDeletedEvent(claim));
    }



    //Utilities useful for claims

    /**
     * Gets the claim at a specific location
     *
     * @param ignoreDepth Whether a location underneath the claim should return the claim.
     * @param cachedClaim can be NULL, but will help performance if you have a reasonable guess about which claim the location is in
     */
    public Claim getClaim(Location location, boolean ignoreDepth, Claim cachedClaim)
    {
        //check cachedClaim guess first.  if it's in the datastore and the location is inside it, we're done
        if(cachedClaim != null && claims.contains(cachedClaim) && cachedClaim.contains(location, ignoreDepth))
            return cachedClaim;

        Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(ClaimUtils.getChunkHash(location));
        if(claimsInChunk == null)
            return null;
        for(Claim claim : claimsInChunk)
        {
            if(claim.contains(location, ignoreDepth))
                return claim;
        }

        return null;
    }

    /**
     * Get a collection of all land claims.
     *
     * If you need to make changes, use provided methods like .deleteClaim() and .createClaim().
     * This will ensure primary memory (RAM) and secondary memory (disk, database) stay in sync
     *
     * @returns a read-only access point for the list of all land claims
     */
    public Collection<Claim> getClaims()
    {
        return Collections.unmodifiableCollection(this.claims);
    }

    /**
     * Get a collection of all land claims within the specified chunk coordinates.
     *
     * Note that there is no world parameter, so this will include all claims from all worlds in this chunk coordinate.
     *
     * @returns a read-only access point for the list of all land claims
     */
    public Collection<Claim> getClaims(int chunkx, int chunkz)
    {
        Set<Claim> chunkClaims = this.chunksToClaimsMap.get(ClaimUtils.getChunkHash(chunkx, chunkz));
        if(chunkClaims != null)
        {
            return Collections.unmodifiableCollection(chunkClaims);
        }
        else
        {
            return Collections.unmodifiableCollection(new HashSet<>());
        }
    }

    //creates a claim.
    //if the new claim would overlap an existing claim, returns a failure along with a reference to the existing claim
    //if the new claim would overlap a WorldGuard region where the player doesn't have permission to build, returns a failure with NULL for claim
    //otherwise, returns a success along with a reference to the new claim
    //use ownerName == "" for administrative claims
    //for top level claims, pass parent == NULL
    //DOES adjust claim blocks available on success (players can go into negative quantity available)
    //DOES check for world guard regions where the player doesn't have permission
    //does NOT check a player has permission to create a claim, or enough claim blocks.
    //does NOT check minimum claim size constraints
    //does NOT visualize the new claim for any players
    public CreateClaimResult createClaim(World world, Location firstCorner, Location secondCorner, UUID ownerID)
    {
        int smallx, bigx, smally, bigy, smallz, bigz;

        int x1 = firstCorner.getBlockX(); int x2 = secondCorner.getBlockX();
        int y1 = firstCorner.getBlockY(); int y2 = secondCorner.getBlockY();
        int z1 = firstCorner.getBlockZ(); int z2 = secondCorner.getBlockZ();

        //determine small versus big inputs
        if(x1 < x2)
        {
            smallx = x1;
            bigx = x2;
        }
        else
        {
            smallx = x2;
            bigx = x1;
        }

        if(y1 < y2)
        {
            smally = y1;
            bigy = y2;
        }
        else
        {
            smally = y2;
            bigy = y1;
        }

        if(z1 < z2)
        {
            smallz = z1;
            bigz = z2;
        }
        else
        {
            smallz = z2;
            bigz = z1;
        }

        //create a new claim instance (but don't save it, yet)
        Claim claimCandidate = new Claim(
                new Location(world, smallx, smally, smallz),
                new Location(world, bigx, bigy, bigz),
                ownerID,null, nextClaimId());

        //ensure this new claim won't overlap any existing claims
        for(Claim claim : this.claims)
        {
            //if we find an existing claim which will be overlapped
            if(ClaimUtils.overlaps(claimCandidate, claim))
            {
                //result = fail, return conflicting claim
                return new CreateClaimResult(false, otherClaim);
            }
        }

        //otherwise add this new claim to the storage store to make it effective
        this.addClaim(claimCandidate, true);

        //then return success along with reference to new claim
        result.succeeded = true;
        result.claim = claimCandidate;
        return result;
    }

    //extends a claim to a new depth
    //respects the max depth config variable
    synchronized public void extendClaim(Claim claim, int newDepth)
    {
        if(newDepth < GriefPrevention.instance.config_claims_maxDepth) newDepth = GriefPrevention.instance.config_claims_maxDepth;

        if(claim.parent != null) claim = claim.parent;

        //adjust to new depth
        claim.lesserBoundaryCorner.setY(newDepth);
        claim.greaterBoundaryCorner.setY(newDepth);

        //save changes
        this.saveClaim(claim);
    }

    //deletes all claims owned by a player
    synchronized public void deleteClaimsForPlayer(UUID playerID, boolean releasePets)
    {
        //make a list of the player's claims
        ArrayList<Claim> claimsToDelete = new ArrayList<Claim>();
        for(int i = 0; i < this.claims.size(); i++)
        {
            Claim claim = this.claims.get(i);
            if((playerID == claim.ownerID || (playerID != null && playerID.equals(claim.ownerID))))
                claimsToDelete.add(claim);
        }

        //delete them one by one
        for(int i = 0; i < claimsToDelete.size(); i++)
        {
            Claim claim = claimsToDelete.get(i);
            claim.removeSurfaceFluids(null);

            this.deleteClaim(claim, releasePets);

            //if in a creative mode world, delete the claim
            if(GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()))
            {
                GriefPrevention.instance.restoreClaim(claim, 0);
            }
        }
    }

    //tries to resize a claim
    //see CreateClaim() for details on return value
    synchronized public CreateClaimResult resizeClaim(Claim claim, int newx1, int newx2, int newy1, int newy2, int newz1, int newz2, Player resizingPlayer)
    {
        //try to create this new claim, ignoring the original when checking for overlap
        CreateClaimResult result = this.createClaim(claim.getLesserBoundaryCorner().getWorld(), newx1, newx2, newy1, newy2, newz1, newz2, claim.ownerID, claim.parent, claim.id, resizingPlayer);

        //if succeeded
        if(result.succeeded)
        {
            //copy permissions from old claim
            ArrayList<String> builders = new ArrayList<String>();
            ArrayList<String> containers = new ArrayList<String>();
            ArrayList<String> accessors = new ArrayList<String>();
            ArrayList<String> managers = new ArrayList<String>();
            claim.getPermissions(builders, containers, accessors, managers);

            for(int i = 0; i < builders.size(); i++)
                result.claim.setPermission(builders.get(i), ClaimPermission.BUILD);

            for(int i = 0; i < containers.size(); i++)
                result.claim.setPermission(containers.get(i), ClaimPermission.CONTAINER);

            for(int i = 0; i < accessors.size(); i++)
                result.claim.setPermission(accessors.get(i), ClaimPermission.ACCESS);

            for(int i = 0; i < managers.size(); i++)
            {
                result.claim.managers.add(managers.get(i));
            }

            //save those changes
            this.saveClaim(result.claim);

            //make original claim ineffective (it's still in the hash map, so let's make it ignored)
            claim.inDataStore = false;
        }

        return result;
    }

    void resizeClaimWithChecks(Player player, PlayerData playerData, int newx1, int newx2, int newy1, int newy2, int newz1, int newz2)
    {
        //for top level claims, apply size rules and claim blocks requirement
        if(playerData.claimResizing.parent == null)
        {
            //measure new claim, apply size rules
            int newWidth = (Math.abs(newx1 - newx2) + 1);
            int newHeight = (Math.abs(newz1 - newz2) + 1);
            boolean smaller = newWidth < playerData.claimResizing.getWidth() || newHeight < playerData.claimResizing.getHeight();

            if(!player.hasPermission("griefprevention.adminclaims") && !playerData.claimResizing.isAdminClaim() && smaller)
            {
                if(newWidth < GriefPrevention.instance.config_claims_minWidth || newHeight < GriefPrevention.instance.config_claims_minWidth)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeClaimTooNarrow, String.valueOf(GriefPrevention.instance.config_claims_minWidth));
                    return;
                }

                int newArea = newWidth * newHeight;
                if(newArea < GriefPrevention.instance.config_claims_minArea)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeClaimInsufficientArea, String.valueOf(GriefPrevention.instance.config_claims_minArea));
                    return;
                }
            }

            //make sure player has enough blocks to make up the difference
            if(!playerData.claimResizing.isAdminClaim() && player.getName().equals(playerData.claimResizing.getOwnerName()))
            {
                int newArea =  newWidth * newHeight;
                int blocksRemainingAfter = playerData.getRemainingClaimBlocks() + playerData.claimResizing.getArea() - newArea;

                if(blocksRemainingAfter < 0)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeNeedMoreBlocks, String.valueOf(Math.abs(blocksRemainingAfter)));
                    this.tryAdvertiseAdminAlternatives(player);
                    return;
                }
            }
        }

        //special rule for making a top-level claim smaller.  to check this, verifying the old claim's corners are inside the new claim's boundaries.
        //rule: in any mode, shrinking a claim removes any surface fluids
        Claim oldClaim = playerData.claimResizing;
        boolean smaller = false;
        if(oldClaim.parent == null)
        {
            //temporary claim instance, just for checking contains()
            Claim newClaim = new Claim(
                    new Location(oldClaim.getLesserBoundaryCorner().getWorld(), newx1, newy1, newz1),
                    new Location(oldClaim.getLesserBoundaryCorner().getWorld(), newx2, newy2, newz2),
                    null, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null);

            //if the new claim is smaller
            if(!newClaim.contains(oldClaim.getLesserBoundaryCorner(), true, false) || !newClaim.contains(oldClaim.getGreaterBoundaryCorner(), true, false))
            {
                smaller = true;

                //remove surface fluids about to be unclaimed
                oldClaim.removeSurfaceFluids(newClaim);
            }
        }

        //ask the datastore to try and resize the claim, this checks for conflicts with other claims
        CreateClaimResult result = GriefPrevention.instance.storage.resizeClaim(playerData.claimResizing, newx1, newx2, newy1, newy2, newz1, newz2, player);

        if(result.succeeded)
        {
            //decide how many claim blocks are available for more resizing
            int claimBlocksRemaining = 0;
            if(!playerData.claimResizing.isAdminClaim())
            {
                UUID ownerID = playerData.claimResizing.ownerID;
                if(playerData.claimResizing.parent != null)
                {
                    ownerID = playerData.claimResizing.parent.ownerID;
                }
                if(ownerID == player.getUniqueId())
                {
                    claimBlocksRemaining = playerData.getRemainingClaimBlocks();
                }
                else
                {
                    PlayerData ownerData = this.getPlayerData(ownerID);
                    claimBlocksRemaining = ownerData.getRemainingClaimBlocks();
                    OfflinePlayer owner = GriefPrevention.instance.getServer().getOfflinePlayer(ownerID);
                    if(!owner.isOnline())
                    {
                        this.clearCachedPlayerData(ownerID);
                    }
                }
            }

            //inform about success, visualize, communicate remaining blocks available
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.ClaimResizeSuccess, String.valueOf(claimBlocksRemaining));
            Visualization visualization = Visualization.FromClaim(result.claim, player.getEyeLocation().getBlockY(), VisualizationType.Claim, player.getLocation());
            Visualization.apply(player, visualization);

            //if resizing someone else's claim, make a log entry
            if(!player.getUniqueId().equals(playerData.claimResizing.ownerID) && playerData.claimResizing.parent == null)
            {
                GriefPrevention.AddLogEntry(player.getName() + " resized " + playerData.claimResizing.getOwnerName() + "'s claim at " + GriefPrevention.getfriendlyLocationString(playerData.claimResizing.lesserBoundaryCorner) + ".");
            }

            //if in a creative mode world and shrinking an existing claim, restore any unclaimed area
            if(smaller && GriefPrevention.instance.creativeRulesApply(oldClaim.getLesserBoundaryCorner()))
            {
                GriefPrevention.sendMessage(player, TextMode.Warn, Messages.UnclaimCleanupWarning);
                GriefPrevention.instance.restoreClaim(oldClaim, 20L * 60 * 2);  //2 minutes
                GriefPrevention.AddLogEntry(player.getName() + " shrank a claim @ " + GriefPrevention.getfriendlyLocationString(playerData.claimResizing.getLesserBoundaryCorner()));
            }

            //clean up
            playerData.claimResizing = null;
            playerData.lastShovelLocation = null;
        }
        else
        {
            if(result.claim != null)
            {
                //inform player
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeFailOverlap);

                //show the player the conflicting claim
                Visualization visualization = Visualization.FromClaim(result.claim, player.getEyeLocation().getBlockY(), VisualizationType.ErrorClaim, player.getLocation());
                Visualization.apply(player, visualization);
            }
            else
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeFailOverlapRegion);
            }
        }
    }

    //gets all the claims "near" a location
    Set<Claim> getNearbyClaims(Location location)
    {
        Set<Claim> claims = new HashSet<Claim>();

        Chunk lesserChunk = location.getWorld().getChunkAt(location.subtract(150, 0, 150));
        Chunk greaterChunk = location.getWorld().getChunkAt(location.add(300, 0, 300));

        for(int chunk_x = lesserChunk.getX(); chunk_x <= greaterChunk.getX(); chunk_x++)
        {
            for(int chunk_z = lesserChunk.getZ(); chunk_z <= greaterChunk.getZ(); chunk_z++)
            {
                Chunk chunk = location.getWorld().getChunkAt(chunk_x, chunk_z);
                Long chunkID = ClaimUtils.getChunkHash(chunk.getBlock(0,  0,  0).getLocation());
                ArrayList<Claim> claimsInChunk = this.chunksToClaimsMap.get(chunkID);
                if(claimsInChunk != null)
                {
                    for(Claim claim : claimsInChunk)
                    {
                        if(claim.inDataStore && claim.getLesserBoundaryCorner().getWorld().equals(location.getWorld()))
                        {
                            claims.add(claim);
                        }
                    }
                }
            }
        }

        return claims;
    }

    //deletes all the land claims in a specified world
    void deleteClaimsInWorld(World world, boolean deleteAdminClaims)
    {
        for(int i = 0; i < claims.size(); i++)
        {
            Claim claim = claims.get(i);
            if(claim.getLesserBoundaryCorner().getWorld().equals(world))
            {
                if(!deleteAdminClaims && claim.isAdminClaim()) continue;
                this.deleteClaim(claim, false, false);
                i--;
            }
        }
    }

    //saves any changes to a claim to secondary storage
    public void saveClaim(Claim claim)
    {
        //ensure a unique identifier for the claim which will be used to name the file on disk
        if(claim.id == null || claim.id == -1)
        {
            claim.id = this.nextClaimID;
            this.incrementNextClaimID();
        }

        this.writeClaimToStorage(claim);
    }

    /**
     * TODO: move to ClaimManager
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
}
