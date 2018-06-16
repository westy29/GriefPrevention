package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Created on 6/16/2018.
 *
 * @author RoboMWM, BigScary
 */
public class ExtendClaimCommand implements CommandExecutor
{
    private ClaimClerk claimClerk;

    public ExtendClaimCommand(ClaimClerk claimClerk)
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

        int amount = 10;

        try
        {
            if (args.length > 0)
                amount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ignored){}

        Vector direction = player.getLocation().getDirection();

        Location lc = claim.getLesserBoundaryCorner();
        Location gc = claim.getGreaterBoundaryCorner();
        int newx1 = lc.getBlockX();
        int newx2 = gc.getBlockX();
        int newy1 = lc.getBlockY();
        int newy2 = gc.getBlockY();
        int newz1 = lc.getBlockZ();
        int newz2 = gc.getBlockZ();

        //if changing Z only
        if(Math.abs(direction.getX()) < .3)
        {
            if(direction.getZ() > 0)
            {
                newz2 += amount;  //north
            }
            else
            {
                newz1 -= amount;  //south
            }
        }

        //if changing X only
        else if(Math.abs(direction.getZ()) < .3)
        {
            if(direction.getX() > 0)
            {
                newx2 += amount;  //east
            }
            else
            {
                newx1 -= amount;  //west
            }
        }

        //diagonals
        else
        {
            if(direction.getX() > 0)
            {
                newx2 += amount;
            }
            else
            {
                newx1 -= amount;
            }

            if(direction.getZ() > 0)
            {
                newz2 += amount;
            }
            else
            {
                newz1 -= amount;
            }
        }

        lc.zero();
        gc.zero();
        lc.add(newx1, newy1, newz1);
        gc.add(newx2, newy2, newz2);

        claimClerk.resizeClaim(player, claim, lc, gc);

        return true;
    }
}
