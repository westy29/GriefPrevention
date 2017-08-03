package me.ryanhamshire.GriefPrevention.visualization;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
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
    JavaPlugin instance;

    public VisualizationManager(JavaPlugin plugin)
    {
        instance = plugin;
    }

    //sends a visualization to a player
    public void Apply(Player player, Visualization visualization)
    {
        //if he has any current visualization, clear it first
        revert(player);

        //if he's online, create a task to send him the visualization
        if(player.isOnline() && !visualization.getElements().isEmpty() && visualization.getElements().get(0).location.getWorld().equals(player.getWorld()))
        {
            GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new VisualizationApplicationTask(player, playerData, visualization), 1L);

        }
    }

    //reverts a visualization by sending another block change list, this time with the real world block values
    public void revert(Player player)
    {
        if(!player.isOnline()) return;

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        Visualization visualization = playerData.currentVisualization;

        if(playerData.currentVisualization != null)
        {
            //locality
            int minx = player.getLocation().getBlockX() - 100;
            int minz = player.getLocation().getBlockZ() - 100;
            int maxx = player.getLocation().getBlockX() + 100;
            int maxz = player.getLocation().getBlockZ() + 100;

            //remove any elements which are too far away
            visualization.removeElementsOutOfRange(visualization.elements, minx, minz, maxx, maxz);

            //send real block information for any remaining elements
            for(int i = 0; i < visualization.elements.size(); i++)
            {
                VisualizationElement element = visualization.elements.get(i);

                //check player still in world where visualization exists
                if(i == 0)
                {
                    if(!player.getWorld().equals(element.location.getWorld())) return;
                }

                player.sendBlockChange(element.location, element.realMaterial, element.realData);
            }

            playerData.currentVisualization = null;
        }
    }

    //convenience method to build a visualization from a claim
    //visualizationType determines the style (gold blocks, silver, red, diamond, etc)
    public static Visualization FromClaim(Claim claim, int height, VisualizationType visualizationType, Location locality)
    {
        Visualization visualization = new Visualization();

        //special visualization for administrative land claims
        if(claim.isAdminClaim() && visualizationType == VisualizationType.Claim)
        {
            visualizationType = VisualizationType.AdminClaim;
        }

        //add top level last so that it takes precedence (it shows on top when the child claim boundaries overlap with its boundaries)
        visualization.addClaimElements(claim, height, visualizationType, locality);

        return visualization;
    }
}
