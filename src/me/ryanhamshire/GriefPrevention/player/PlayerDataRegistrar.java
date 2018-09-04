package me.ryanhamshire.GriefPrevention.player;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.storage.Storage;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author RoboMWM
 * Created on 5/25/2017.
 */
public class PlayerDataRegistrar
{
    private Storage storage;

    //in-memory cache for player storage
    private ConcurrentHashMap<UUID, PlayerData> playerNameToPlayerDataMap = new ConcurrentHashMap<UUID, PlayerData>();

    /**
     *
     * @param uuid
     * @return null if the PlayerData does not exist
     */
    public PlayerData getPlayerData(UUID uuid)
    {
        //first, look in cache
        PlayerData playerData = this.playerNameToPlayerDataMap.get(uuid);

        if (playerData == null)
        {
            playerData = storage.getPlayerData(uuid);

            //cache if found
            if (playerData != null)
                playerNameToPlayerDataMap.put(uuid, playerData);
        }

        return playerData;
    }

    /**
     * Creates and caches a new PlayerData if none exists in storage. Does not save this new PlayerData to storage.
     * @param uuid
     * @return a PlayerData object. Nonnull.
     */
    public PlayerData getOrCreatePlayerData(UUID uuid)
    {
        PlayerData playerData = getOrCreatePlayerData(uuid);

        if (playerData == null)
        {
            //TODO: fill with config defaults
            playerData = new PlayerData(uuid, 0, 0);
            playerNameToPlayerDataMap.put(uuid, playerData);
        }

        return playerData;
    }

    public boolean savePlayerData(UUID uuid)
    {
        if (!playerNameToPlayerDataMap.containsKey(uuid))
            return false;
        return storage.savePlayerData(playerNameToPlayerDataMap.get(uuid));
    }

    public boolean savePlayerData(PlayerData playerData)
    {
        return storage.savePlayerData(playerData);
    }
}
