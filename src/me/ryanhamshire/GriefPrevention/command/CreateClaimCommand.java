package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimManager;
import me.ryanhamshire.GriefPrevention.claim.ClaimUtils;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 6/6/2018.
 *
 * @author RoboMWM
 */
public class CreateClaimCommand implements CommandExecutor
{
    private PlayerDataManager playerDataManager;
    private ClaimManager claimManager;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        //TODO: permission checks

        int radius = 10; //TODO: replace with config

        try
        {
            if (args.length > 0)
                radius = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ignored){}

        Location firstCorner = player.getLocation().add(radius, 0, radius);
        Location secondCorner = player.getLocation().subtract(radius, 0, radius);

        new ClaimClerk(claimManager, playerDataManager.getPlayerData(player.getUniqueId()), player).registerNewClaim(firstCorner, secondCorner);

        return true;
    }
}
