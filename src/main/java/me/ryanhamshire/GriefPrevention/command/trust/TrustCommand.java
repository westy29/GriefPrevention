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
public class TrustCommand extends ClaimManagementCommands
{
    private JavaPlugin plugin;

    public TrustCommand(ClaimClerk claimClerk)
    {
        super(claimClerk);
    }

    @Override
    public boolean execute(Player player, Command cmd, String[] args, Claim claim)
    {
        if (args.length == 0)
            return false;

        Map<UUID, ClaimPermission> trustees = claim.getTrustees();

        ClaimPermission permission;

        switch (cmd.getName().toLowerCase())
        {
            case "managertrust":
                permission = ClaimPermission.MANAGE;
                break;
            case "trust":
                permission = ClaimPermission.BUILD;
                break;
            case "containertrust":
                permission = ClaimPermission.CONTAINER;
                break;
            default:
                return false;
        }

        for (String playerName : args)
        {
            OfflinePlayer trustee = plugin.getServer().getOfflinePlayer(playerName);
            if (trustee.getUniqueId() == null)
            {
                player.sendMessage(playerName + " is not a valid name." + "");
                continue;
            }

            trustees.put(trustee.getUniqueId(), permission);
            player.sendMessage("Granted " + trustee + " " + permission.name() + " trust to this claim.");
        }

        claimClerk.changeTrustees(claim, trustees);

        return true;
    }
}
