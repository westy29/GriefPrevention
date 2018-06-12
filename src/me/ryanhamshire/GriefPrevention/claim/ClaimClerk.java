package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created on 6/7/2018.
 *
 * Utility class to register claims and handle errors.
 *
 * Maybe I should've named it a "butler" or "agent" since you instantiate your own. Hmm.
 *
 * @author RoboMWM
 */
public class ClaimClerk
{
    private ClaimManager claimManager;
    private PlayerData playerData;
    private Player player;

    public ClaimClerk(ClaimManager claimManager, PlayerData playerData, Player player)
    {
        this.claimManager = claimManager;
        this.playerData = playerData;
        this.player = player;
    }

    /**
     * Registers a new claim
     * @param firstCorner
     * @param secondCorner
     * @return true if successful, false otherwise
     */
    public boolean registerNewClaim(Location firstCorner, Location secondCorner)
    {
        if (playerData.getRemainingClaimBlocks() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            player.sendMessage("Not enough claim blocks");
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimManager.createClaim(firstCorner, secondCorner, player.getUniqueId());
            if (claimResult.isSuccess())
                return true;

            player.sendMessage("Overlaps another claim");
            //send overlapped claim
        }
        catch (Exception e)
        {
            player.sendMessage("Error occurred while attempting to save your new claim, see console log for details.");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Resizes a given claim
     * @param claim
     * @param firstCorner
     * @param secondCorner
     * @return
     */
    public boolean resizeClaim(Claim claim, Location firstCorner, Location secondCorner)
    {
        if (playerData.getRemainingClaimBlocks() + claim.getArea() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            player.sendMessage("Not enough claim blocks");
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimManager.resizeClaim(claim, firstCorner, secondCorner);
            if (claimResult.isSuccess())
                return true;

            player.sendMessage("Overlaps another claim");
            //send overlapped claim
        }
        catch (Exception e)
        {
            player.sendMessage("Error occurred while attempting to save your resized claim, see console log for details.");
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
