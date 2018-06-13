package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created on 6/7/2018.
 *
 * Utility class to register claims and handle errors.
 *
 * @author RoboMWM
 */
public class ClaimClerk
{
    private ClaimRegistrar claimRegistrar;
    private PlayerDataRegistrar playerDataRegistrar;

    public ClaimClerk(ClaimRegistrar claimRegistrar, PlayerDataRegistrar playerDataRegistrar)
    {
        this.claimRegistrar = claimRegistrar;
        this.playerDataRegistrar = playerDataRegistrar;
    }

    /**
     * Registers a new claim
     * @param firstCorner
     * @param secondCorner
     * @return true if successful, false otherwise
     */
    public boolean registerNewClaim(Player player, Location firstCorner, Location secondCorner)
    {
        PlayerData playerData = playerDataRegistrar.getPlayerData(player.getUniqueId());
        if (playerData.getRemainingClaimBlocks() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            player.sendMessage("Not enough claim blocks");
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimRegistrar.createClaim(firstCorner, secondCorner, player.getUniqueId());
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
    public boolean resizeClaim(Player player, Claim claim, Location firstCorner, Location secondCorner)
    {
        PlayerData playerData = playerDataRegistrar.getPlayerData(claim.getOwnerUUID());

        if (playerData.getRemainingClaimBlocks() + claim.getArea() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            player.sendMessage("Not enough claim blocks");
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimRegistrar.resizeClaim(claim, firstCorner, secondCorner);
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
