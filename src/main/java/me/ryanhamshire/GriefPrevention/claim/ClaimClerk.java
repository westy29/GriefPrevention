package me.ryanhamshire.GriefPrevention.claim;

import com.sun.xml.internal.ws.util.CompletedFuture;
import me.ryanhamshire.GriefPrevention.enums.Message;
import me.ryanhamshire.GriefPrevention.enums.Permission;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import me.ryanhamshire.GriefPrevention.storage.Storage;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationManager;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on 6/7/2018.
 * <p>
 * Utility class to register and access claims, and handle errors (somewhat) gracefully.
 * Provides a cache for getClaim calls.
 *
 * Could also be called a "storage assistant" at this point, really.
 * <p>
 *
 * @author RoboMWM
 */
public class ClaimClerk implements Listener
{
    private Storage storage;
    private ClaimRegistrar claimRegistrar;
    private PlayerDataRegistrar playerDataRegistrar;
    private VisualizationManager visualizationManager;
    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    /**
     * Creates a new ClaimClerk, which helps assist in obtaining and performing actions on the claim and playerdata registrars.
     * <p>
     * You really <i>shouldn't</i> be instantiating this unless you're performing unusual actions and would benefit from a separate getClaim cache.
     *
     * @param plugin Used to cleanup caches created using getClaim. Can be null <i>only</i> if you never use getClaim or always pass null for the Player param in getClaim.
     * @param claimRegistrar
     * @param playerDataRegistrar
     * @param storage
     */
    public ClaimClerk(Plugin plugin, ClaimRegistrar claimRegistrar, PlayerDataRegistrar playerDataRegistrar, Storage storage, VisualizationManager visualizationManager)
    {
        if (plugin != null)
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.claimRegistrar = claimRegistrar;
        this.playerDataRegistrar = playerDataRegistrar;
        this.storage = storage;
        this.visualizationManager = visualizationManager;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        tasks.take().run();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Registers a new claim
     *
     * @param firstCorner
     * @param secondCorner
     */
    public Future<Boolean> registerNewClaim(Player player, Location firstCorner, Location secondCorner)
    {
        FutureTask<Boolean> task = new FutureTask<>(() ->
        {
            PlayerData playerData = playerDataRegistrar.getOrCreatePlayerData(player.getUniqueId()).get();
            if (playerData.getRemainingClaimBlocks(claimRegistrar) < ClaimUtils.getArea(firstCorner, secondCorner))
            {
                Message.CLAIM_FAIL_INSUFFICIENT_CLAIMBLOCKS.send(player);
                return false;
            }

            CreateClaimResult claimResult = claimRegistrar.createClaim(firstCorner, secondCorner, player.getUniqueId());
            if (claimResult.isSuccess())
            {
                Message.CLAIM_CREATED.send(player);
                visualizationManager.apply(player, visualizationManager.fromClaim(claimResult.getClaim(), VisualizationType.Claim, player.getLocation()));
                return true;
            }

            if (claimResult.getClaim() == null) //Another plugin canceled the event
                return false;

            Message.CLAIM_FAIL_OVERLAPS.send(player);
            visualizationManager.apply(player, claimResult.getClaim(), true);
            //TODO: suggest merge if overlapping claim is owned by player
            return false;
        });

        tasks.add(task);
        return task;
    }

    /**
     * Resizes a given claim
     *
     * @param claim
     * @param firstCorner
     * @param secondCorner
     * @return
     */
    public Future<Boolean> resizeClaim(Player player, Claim claim, Location firstCorner, Location secondCorner)
    {
        if (Permission.CLAIM_CREATE.hasNot(player, Message.CLAIM_FAIL_NO_PERMISSION))
            return new CompletedFuture<>(false, null);

        if (!claim.hasPermission(player, ClaimPermission.MANAGE))
        {
            Message.CLAIM_NO_TRUST_MANAGE.send(player);
            return new CompletedFuture<>(false, null);
        }

        FutureTask<Boolean> task = new FutureTask<>(() ->
        {
            PlayerData playerData = playerDataRegistrar.getOrCreatePlayerData(claim.getOwnerUUID()).get();

            if (playerData.getRemainingClaimBlocks(claimRegistrar) + claim.getArea() < ClaimUtils.getArea(firstCorner, secondCorner))
            {
                Message.CLAIM_FAIL_INSUFFICIENT_CLAIMBLOCKS.send(player);
                return false;
            }

            CreateClaimResult claimResult = claimRegistrar.resizeClaim(claim, firstCorner, secondCorner);
            if (claimResult.isSuccess())
            {
                Message.CLAIM_RESIZED.send(player, Integer.toString(playerData.getRemainingClaimBlocks(claimRegistrar)));
                return true;
            }

            if (claimResult.getClaim() == null)
                return false;

            Message.CLAIM_FAIL_OVERLAPS.send(player);
            visualizationManager.apply(player, claimResult.getClaim(), true);
            //TODO: suggest merge if overlapping claim is owned by player
            return false;
        });

        tasks.add(task);
        return task;
    }

    //For caching last-known claim
    private Map<Player, Claim> lastAccessedClaim = new HashMap<>();

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        lastAccessedClaim.remove(event.getPlayer());
    }

    /**
     * Gets the claim at a specific location
     *
     * @param player      The player for whom we're retrieving the claim for. Used for caching.
     * @param ignoreDepth Whether a location underneath the claim should return the claim.
     */
    public Claim getClaim(Player player, Location location, boolean ignoreDepth)
    {
        Claim claim = claimRegistrar.getClaim(location, ignoreDepth, lastAccessedClaim.get(player));
        lastAccessedClaim.put(player, claim);
        return claim;
    }

    /**
     * Replaces the old map of trustees with the provided map, and saves the claim to storage
     *
     * @param claim
     * @param newTrustees
     * @return false if there was an issue saving the claim (may need to undo changes)
     */
    public void changeTrustees(Claim claim, Map<UUID, ClaimPermission> newTrustees)
    {
        claim.getTrusteesMap().clear();
        claim.getTrusteesMap().putAll(newTrustees);
        storage.saveClaim(claim);
    }
}
