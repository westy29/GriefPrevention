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

package me.ryanhamshire.GriefPrevention;

import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import me.ryanhamshire.GriefPrevention.command.AbandonClaimCommand;
import me.ryanhamshire.GriefPrevention.command.CreateClaimCommand;
import me.ryanhamshire.GriefPrevention.command.ExtendClaimCommand;
import me.ryanhamshire.GriefPrevention.command.trust.TrustCommand;
import me.ryanhamshire.GriefPrevention.command.trust.UntrustCommand;
import me.ryanhamshire.GriefPrevention.listener.ClaimListener;
import me.ryanhamshire.GriefPrevention.listener.ClaimTool;
import me.ryanhamshire.GriefPrevention.enums.Message;
import me.ryanhamshire.GriefPrevention.listener.PlayerEventHandler;
import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import me.ryanhamshire.GriefPrevention.storage.FlatFileStorage;
import me.ryanhamshire.GriefPrevention.storage.Storage;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class GriefPrevention extends JavaPlugin
{
    private ClaimRegistrar claimRegistrar;
    private PlayerDataRegistrar playerDataRegistrar;
    private Storage storage; //TODO: add setter, config-controlled
    private VisualizationManager visualizationManager;

    public void onEnable()
    {
        initializeMessages(new File(getDataFolder() + File.separator + "messages.yml"));

        initializeStorage();
        claimRegistrar = new ClaimRegistrar(this, storage);
        playerDataRegistrar = new PlayerDataRegistrar(storage, 1000); //TODO: config
        visualizationManager = new VisualizationManager(this);

        ClaimClerk claimClerk = new ClaimClerk(this, claimRegistrar, playerDataRegistrar, storage, visualizationManager);

        new ClaimListener(this, claimRegistrar);
        new ClaimTool(this, claimClerk, visualizationManager, Material.GOLDEN_SHOVEL); //TODO: config
        new PlayerEventHandler(this, playerDataRegistrar);

        //register commands

        getCommand("newclaim").setExecutor(new CreateClaimCommand(claimClerk));
        getCommand("extendclaim").setExecutor(new ExtendClaimCommand(claimClerk));
        getCommand("abandonclaim").setExecutor(new AbandonClaimCommand(claimClerk, claimRegistrar, visualizationManager));
        getCommand("trust").setExecutor(new TrustCommand(claimClerk));
        getCommand("untrust").setExecutor(new UntrustCommand(claimClerk));

        new Metrics(this);
    }

    public static void log(Object object)
    {
        System.out.println("[GPv20] " + String.valueOf(object));
    }

    @Override
    public void onDisable()
    {
        storage.close();
    }

    public void initializeMessages(File messagesFile)
    {
        YamlConfiguration messagesYaml = Message.initialize(YamlConfiguration.loadConfiguration(messagesFile));
        UsefulUtil.saveStringToFile(this, messagesFile, messagesYaml.saveToString());
    }

    public void initializeStorage() //TODO: pass in config for alternate storage option
    {
        this.storage = new FlatFileStorage(this);
    }
}
