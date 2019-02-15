package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.enums.Message;
import me.ryanhamshire.GriefPrevention.enums.Permission;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 6/6/2018.
 *
 * @author RoboMWM
 */
public class CreateClaimCommand extends ClaimManagementCommands
{
    public CreateClaimCommand(ClaimClerk claimClerk)
    {
        super(claimClerk);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        return execute(player, cmd, args, null);
    }

    @Override
    public boolean execute(Player player, Command cmd, String[] args, Claim claim)
    {
        if (Permission.CLAIM_CREATE.hasNot(player, Message.CLAIM_FAIL_NO_PERMISSION))
            return true;

        int radius = 10; //TODO: replace with config

        try
        {
            if (args.length > 0)
                radius = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            return false;
        }

        Location firstCorner = player.getLocation().add(radius, 0, radius);
        Location secondCorner = player.getLocation().subtract(radius, 0, radius);

        claimClerk.registerNewClaim(player, firstCorner, secondCorner);

        return true;
    }
}
