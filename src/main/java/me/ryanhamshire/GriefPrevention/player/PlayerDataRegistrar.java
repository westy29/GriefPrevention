package me.ryanhamshire.GriefPrevention.player;

import com.sun.xml.internal.ws.util.CompletedFuture;
import me.ryanhamshire.GriefPrevention.storage.Storage;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author RoboMWM
 * Created on 5/25/2017.
 */
public class PlayerDataRegistrar
{
    private int defaultAccruedBlocks;
    private Storage storage;
    private ConcurrentHashMap<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<UUID, PlayerData>();

    public PlayerDataRegistrar(Storage storage, int defaultAccruedBlocks)
    {
        this.storage = storage;
        this.defaultAccruedBlocks = defaultAccruedBlocks;
    }

    /**
     * @param uuid
     * @return null if the PlayerData does not exist
     */
    public Future<PlayerData> getPlayerData(UUID uuid)
    {
        //first, look in cache
        PlayerData playerData = this.playerDataCache.get(uuid);
        if (playerData != null)
        {
            return new CompletedFuture<>(playerData, null);
        }

        return new FutureTask<>(() ->
        {
            PlayerData playerData1 = storage.getPlayerData(uuid).get();

            //cache if found
            if (playerData1 != null)
                playerDataCache.put(uuid, playerData1);
            return playerData;
        });

    }

    /**
     * Creates and caches a new PlayerData if none exists in storage. Does not save this new PlayerData to storage.
     *
     * @param uuid
     * @return a PlayerData object. Nonnull.
     */
    public Future<PlayerData> getOrCreatePlayerData(UUID uuid)
    {
        return new FutureTask<>(() ->
        {
            PlayerData playerData = getPlayerData(uuid).get();

            if (playerData == null)
            {
                playerData = new PlayerData(uuid, defaultAccruedBlocks, 0);
                playerDataCache.put(uuid, playerData);
            }

            return playerData;
        });
    }

    public boolean savePlayerData(UUID uuid)
    {
        if (!playerDataCache.containsKey(uuid))
            return false;
        storage.savePlayerData(playerDataCache.get(uuid));
        return true;
    }

    public void savePlayerData(PlayerData playerData)
    {
        storage.savePlayerData(playerData);
    }
}
