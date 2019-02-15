package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import me.ryanhamshire.GriefPrevention.enums.Message;

import me.ryanhamshire.GriefPrevention.visualization.VisualizationManager;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Created on 6/13/2018.
 *
 * @author RoboMWM
 */
public class AbandonClaimCommand extends ClaimManagementCommands
{
    private ClaimRegistrar claimRegistrar;
    private VisualizationManager visualizationManager;

    public AbandonClaimCommand(ClaimClerk claimClerk, ClaimRegistrar claimRegistrar, VisualizationManager visualizationManager)
    {
        super(claimClerk);
        this.claimRegistrar = claimRegistrar;
    }

    @Override
    public boolean execute(Player player, Command command, String[] args, Claim claim)
    {
        //TODO: ask user to confirm

        claimRegistrar.deleteClaim(claim);

        Message.CLAIM_ABANDONED.send(player);
        visualizationManager.revert(player);

        return true;
    }
}
