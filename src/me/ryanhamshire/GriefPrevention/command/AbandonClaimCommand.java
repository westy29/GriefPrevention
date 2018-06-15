package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 6/13/2018.
 *
 * @author RoboMWM
 */
public class AbandonClaimCommand implements CommandExecutor
{
    private ClaimClerk claimClerk;
    private ClaimRegistrar claimRegistrar;

    public AbandonClaimCommand(ClaimClerk claimClerk)
    {
        this.claimClerk = claimClerk;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        Claim claim = claimClerk.getClaim(player, player.getLocation(), true);

        if (claim == null)
        {
            player.sendMessage("no claim here");
            return true;
        }

        if (claim.getOwnerUUID() != player.getUniqueId())
        {
            player.sendMessage("not your claim");
            return true;
        }

        //TODO: ask user to confirm

        if (!claimRegistrar.deleteClaim(claim))
        {
            player.sendMessage("error in deleting your claim");
            return true;
        }

        return true;
    }
}
