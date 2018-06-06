package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.ClaimManager;
import me.ryanhamshire.GriefPrevention.claim.ClaimUtils;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataManager;
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

        int width = 10; //TODO: replace with config

        try
        {
            if (args.length > 0)
                width = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ignored){}

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        if (playerData.getRemainingClaimBlocks() >= ClaimUtils.getArea())

    }
}
