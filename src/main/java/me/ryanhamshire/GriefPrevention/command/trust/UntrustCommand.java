package me.ryanhamshire.GriefPrevention.command.trust;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimPermission;
import me.ryanhamshire.GriefPrevention.command.ClaimManagementCommands;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

/**
 * Created on 6/16/2018.
 *
 * @author RoboMWM
 */
public class UntrustCommand extends ClaimManagementCommands
{
    private JavaPlugin plugin;

    public UntrustCommand(ClaimClerk claimClerk)
    {
        super(claimClerk);
    }

    @Override
    public boolean execute(Player player, Command cmd, String[] args, Claim claim)
    {
        if (args.length == 0)
            return false;

        Map<UUID, ClaimPermission> trustees = claim.getTrustees();

        for (String playerName : args)
        {
            OfflinePlayer trustee = plugin.getServer().getOfflinePlayer(playerName);
            if (trustee.getUniqueId() == null)
            {
                player.sendMessage(playerName + " is not a valid name." + "");
                continue;
            }

            trustees.remove(trustee.getUniqueId());
            player.sendMessage("Revoked trust from " + trustee + " to this claim.");
        }

        claimClerk.changeTrustees(claim, trustees);

        return true;
    }
}
