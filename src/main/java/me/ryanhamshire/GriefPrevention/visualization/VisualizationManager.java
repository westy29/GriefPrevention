package me.ryanhamshire.GriefPrevention.visualization;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.claim.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 8/2/2017.
 *
 * @author RoboMWM
 */
public class VisualizationManager
{
    public final static String METADATA_KEY = "GP_VISUALIZATION";

    private JavaPlugin plugin;

    public VisualizationManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void apply(Player player, Claim claim)
    {
        apply(player, claim, false);
    }

    public void apply(Player player, Claim claim, boolean error)
    {
        VisualizationType type;
        if (error)
            type = VisualizationType.ErrorClaim;
        else if (claim.isAdminClaim())
            type = VisualizationType.AdminClaim;
        else
            type = VisualizationType.Claim;
        Visualization visualization = fromClaim(claim, type, player.getLocation());
        apply(player, visualization);
    }

    //sends a visualization to a player
    public void apply(Player player, Visualization visualization)
    {
        //if he has any current visualization, clear it first
        revert(player);

        if (visualization.getWorld() != player.getWorld())
            return;

        new VisualizationApplicationTask(player, visualization, plugin).runTask(plugin);

        new VisualizationReversionTask(player, this).runTaskLater(plugin, 1200L); //1 minute
    }

    //reverts a visualization by sending another block change list, this time with the real world block values
    public void revert(Player player)
    {
        if (!player.hasMetadata(METADATA_KEY))
            return;

        Visualization visualization = (Visualization)player.getMetadata(METADATA_KEY).get(0).value();
        player.removeMetadata(METADATA_KEY, plugin);

        if (!player.isOnline())
            return;
        if (player.getWorld() != visualization.getWorld())
            return;

        //locality
        int minx = player.getLocation().getBlockX() - 100;
        int minz = player.getLocation().getBlockZ() - 100;
        int maxx = player.getLocation().getBlockX() + 100;
        int maxz = player.getLocation().getBlockZ() + 100;

        //remove any elements which are too far away
        visualization.removeElementsOutOfRange(visualization.getElements(), minx, minz, maxx, maxz);

        //send real block information for any remaining elements
        for (VisualizationElement element : visualization.getElements())
        {
            GriefPrevention.log("reverting visualization for " + player.getName());
            player.sendBlockChange(element.getLocation(), element.getRealBlock());
        }
    }

    //convenience method to build a visualization from a claim
    //visualizationType determines the style (gold blocks, silver, red, diamond, etc)
    public Visualization fromClaim(Claim claim, VisualizationType visualizationType, Location locality)
    {
        Visualization visualization = new Visualization();

        //special visualization for administrative land claims
        if (claim.isAdminClaim() && visualizationType == VisualizationType.Claim)
        {
            visualizationType = VisualizationType.AdminClaim;
        }

        visualization.addClaimElements(claim, visualizationType, locality);

        return visualization;
    }

    public Visualization fromClaims(Iterable<Claim> claims, VisualizationType type, Location locality)
    {
        Visualization visualization = new Visualization();

        for (Claim claim : claims)
        {
            visualization.addClaimElements(claim, type, locality);
        }

        return visualization;
    }
}
