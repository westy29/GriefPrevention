package me.ryanhamshire.GriefPrevention.player;

import me.ryanhamshire.GriefPrevention.storage.Storage;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    public PlayerData getPlayerData(UUID uuid)
    {
        //first, look in cache
        PlayerData playerData = this.playerDataCache.get(uuid);
        if (playerData != null)
        {
            return playerData;
        }

        playerData = storage.getPlayerData(uuid);

        //cache if found
        if (playerData != null)
            playerDataCache.put(uuid, playerData);
        return playerData;
    }

    /**
     * Creates and caches a new PlayerData if none exists in storage. Does not save this new PlayerData to storage.
     *
     * @param uuid
     * @return a PlayerData object. Nonnull.
     */
    public PlayerData getOrCreatePlayerData(UUID uuid)
    {
        PlayerData playerData = getPlayerData(uuid);

        if (playerData == null)
        {
            playerData = new PlayerData(uuid, defaultAccruedBlocks, 0);
            playerDataCache.put(uuid, playerData);
        }

        return playerData;
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
