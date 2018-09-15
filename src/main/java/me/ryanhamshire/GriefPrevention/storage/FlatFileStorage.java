/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention.storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimPermission;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

//manages storage stored in the file system
public class FlatFileStorage implements Storage
{
    private GriefPrevention plugin;
    private File claimDataFolder;
    private File playerDataFolder;

    private ExecutorService playerDataPool = Executors.newCachedThreadPool();
    private ExecutorService claimDataPool = Executors.newCachedThreadPool();

    public FlatFileStorage(GriefPrevention griefPrevention)
    {
        plugin = griefPrevention;
        claimDataFolder = new File(plugin.getDataFolder(), "ClaimData");
        playerDataFolder = new File(plugin.getDataFolder(), "PlayerData");

        //Generate respective storage folders
        if(!playerDataFolder.exists() || !claimDataFolder.exists())
        {
            playerDataFolder.mkdirs();
            claimDataFolder.mkdirs();
        }
    }

	public Set<Claim> getClaims()
	{
        File[] files = claimDataFolder.listFiles();
        Set<Claim> claims = new HashSet<>();

        for(int i = 0; i < files.length; i++)
        {           
            if(!files[i].isFile())  //avoids folders
                continue;

            try
            {
                claims.add(this.loadClaim(files[i]));
            }
            //if there's any problem with the file's content, log an error message and skip it
            catch(Exception e)
            {
                plugin.getLogger().severe("Could not load claim " + files[i].getName());
                continue;
            }
        }
        return claims;
    }
	
	Claim loadClaim(File file) throws ClassCastException, NumberFormatException
	{
        Claim claim;
        long claimID = Long.parseLong(file.getName().split("\\.")[0]);
	    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        
        //boundaries
        Location lesserBoundaryCorner = (Location)yaml.get("lesserBoundaryCorner");
        Location greaterBoundaryCorner = (Location)yaml.get("greaterBoundaryCorner");
        
        //owner
        String ownerIdentifier = yaml.getString("owner");
        UUID ownerID = null;
        if(!ownerIdentifier.isEmpty())
        {
            try
            {
                ownerID = UUID.fromString(ownerIdentifier);
            }
            catch(Exception ex)
            {
                plugin.getLogger().info("Error - this is not a valid UUID: " + ownerIdentifier + " in claim file " + file.getName());
                plugin.getLogger().info("  Converted land claim to administrative @ " + lesserBoundaryCorner.toString());
            }
        }

        Map<UUID, ClaimPermission> trustees = new HashMap<>();
        ConfigurationSection trusteesSection = yaml.getConfigurationSection("trustees");

        for (String trustee : trusteesSection.getKeys(false))
        {
            trustees.put(UUID.fromString(trustee), ClaimPermission.valueOf(trusteesSection.getString(trustee)));
        }
        
        //instantiate
        claim = new Claim(lesserBoundaryCorner, greaterBoundaryCorner, ownerID, trustees, claimID);
        
        return claim;
	}

	private File getClaimFile(Claim claim) throws NumberFormatException
    {
        return new File(claimDataFolder.getPath() + File.separator + Long.toString(claim.getID()) + ".yml");
    }

	public void saveClaim(Claim claim)
	{
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("lesserBoundaryCorner", claim.getLesserBoundaryCorner().toString());
        yaml.set("greaterBoundaryCorner", claim.getGreaterBoundaryCorner().toString());
        yaml.set("owner", claim.getOwnerUUID().toString());
        yaml.set("trustees", claim.getTrustees()); //TODO: does this store enum's string or int value??
        yaml.set("publicPermission", claim.getPublicPermission());
        claimDataPool.execute(new SaveClaimDataThread(yaml.saveToString(), getClaimFile(claim), plugin.getLogger()));
	}

	public void deleteClaim(Claim claim)
	{
		File claimFile = getClaimFile(claim);
		claimFile.delete();
	}

	public PlayerData getPlayerData(UUID uuid)
	{
		File playerFile = new File(playerDataFolder.getPath() + File.separator + uuid.toString());
		
		//Return new PlayerData file if none exists
		if(!playerFile.exists())
		    return null;

        try
        {
            //read the file content and immediately close it
            List<String> lines = Files.readAllLines(playerFile.toPath());
            Iterator<String> iterator = lines.iterator();

            //first line is accrued claim blocks
            int accrued = Integer.parseInt(iterator.next());

            //second line is any bonus claim blocks granted by administrators
            int bonus = Integer.parseInt(iterator.next());

            return new PlayerData(uuid, accrued, bonus);
        }
        catch (Throwable rock)
        {
            plugin.getLogger().severe("Failed to load playerData for UUID " + uuid.toString());
            rock.printStackTrace();
        }
        return null;
	}
	
	//saves changes to player storage.
	public void savePlayerData(PlayerData playerData)
	{
		//never save storage for the "administrative" account.  null for claim owner ID indicates administrative account
		if(playerData == null || playerData.getUuid() == null)
		    return;
		
		ArrayList<String> fileContent = new ArrayList<>();
		try
		{
			//first line is accrued claim blocks
			fileContent.add(Integer.toString(playerData.getAccruedClaimBlocks()));
			
			//second line is bonus claim blocks
			fileContent.add(Integer.toString(playerData.getBonusClaimBlocks()));
			
			//write storage to file
            File playerDataFile = new File(playerDataFolder + File.separator + playerData.getUuid().toString());
            playerDataPool.execute(new SavePlayerDataThread(fileContent, playerDataFile, plugin.getLogger()));
		}
		catch(Throwable rock)
		{
			plugin.getLogger().severe("Error occurred while attempting to store playerData for UUID " + playerData.getUuid().toString());
			rock.printStackTrace();
			return;
		}
		return;
	}

	public void close()
    {
        playerDataPool.shutdown();
        claimDataPool.shutdown();
        boolean notified = false;
        while (!playerDataPool.isTerminated() || !claimDataPool.isTerminated())
        {
            if (!notified)
            {
                notified = true;
                plugin.getLogger().info("Waiting for save tasks to complete...");
            }
        }
    }
}

class SavePlayerDataThread implements Runnable
{
    private List<String> lines;
    private File file;
    private Logger logger;

    public SavePlayerDataThread(List<String> lines, File file, Logger logger)
    {
        this.lines = lines;
        this.file = file;
    }

    public void run()
    {
        try
        {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            //TODO: is logger thread safe?
            logger.severe("Failed to save player data file " + file.toPath().toString());
            e.printStackTrace();
        }

    }
}

class SaveClaimDataThread implements Runnable
{
    private String content;
    private File file;
    private Logger logger;

    public SaveClaimDataThread(String content, File file, Logger logger)
    {
        this.content = content;
        this.file = file;
        this.logger = logger;
    }

    public void run()
    {
        try
        {
            Files.write(file.toPath(), Collections.singletonList(content), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            //TODO: is logger thread safe?
            logger.severe("Failed to save claim data file " + file.toPath().toString());
            e.printStackTrace();
        }

    }
}

