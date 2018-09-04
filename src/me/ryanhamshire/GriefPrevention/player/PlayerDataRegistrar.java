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

    public PlayerData getPlayerData(UUID uuid)
    {
        //first, look in cache
        PlayerData playerData = this.playerNameToPlayerDataMap.get(uuid);

        //TODO: if not there, look and load from flatfile

        playerData = storage.getPlayerData(uuid);

        return playerData;
    }

    private void loadDataFromSecondaryStorage()
    {
        //reach out to secondary storage to get any storage there
        PlayerData storageData = GriefPrevention.instance.storage.getPlayerData(this.playerID);

        if(this.accruedClaimBlocks == null)
        {
            if(storageData.accruedClaimBlocks != null)
            {
                this.accruedClaimBlocks = storageData.accruedClaimBlocks;

                //ensure at least minimum accrued are accrued (in case of settings changes to increase initial amount)
                if(this.accruedClaimBlocks < GriefPrevention.instance.config_claims_initialBlocks)
                {
                    this.accruedClaimBlocks = GriefPrevention.instance.config_claims_initialBlocks;
                }

            }
            else
            {
                this.accruedClaimBlocks = GriefPrevention.instance.config_claims_initialBlocks;
            }
        }

        if(this.bonusClaimBlocks == null)
        {
            if(storageData.bonusClaimBlocks != null)
            {
                this.bonusClaimBlocks = storageData.bonusClaimBlocks;
            }
            else
            {
                this.bonusClaimBlocks = 0;
            }
        }
    }
}
