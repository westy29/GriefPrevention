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

package me.ryanhamshire.GriefPrevention.message;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public enum Message
{
    CLAIM_CREATED("Claim created! Use /trust to share it with friends."),
    CLAIM_FAIL_INSUFFICIENT_CLAIMBLOCKS("Not enough claim blocks."),
    CLAIM_FAIL_OVERLAPS("Overlaps another claim."),
    CLAIM_ABANDONED("Claim abandoned."),
    NoClaimHere("No claim here."),
    CLAIM_PERMISSION_CHANGE_DENIED("Not your claim.");
    private String message;
    private static PluginManager pluginManager;

    Message(String defaultMessage)
    {
        this.message = defaultMessage;
    }

    public static YamlConfiguration initialize(PluginManager pluginManager, YamlConfiguration messagesFile)
    {
        Message.pluginManager = pluginManager;
        for (Message message : Message.values())
        {
            String messageFromFile = messagesFile.getString(message.name());
            if (messageFromFile == null)
                messagesFile.addDefault(message.name(), message.message);
            else
                message.message = messageFromFile;
        }
        messagesFile.options().copyDefaults(true);
        return messagesFile;
    }

    public void send(CommandSender sender)
    {
        if (message.isEmpty())
            return;
        //TODO: GPSendMessageEvent
        sender.sendMessage(message);
    }

    public void send(CommandSender sender, String... args)
    {
        if (message.isEmpty())
            return;

        String formattedMessage = getMessage();

        for (int i = 0; i < args.length; i++)
            formattedMessage = formattedMessage.replaceAll("\\{" + i + "}", args[i]);

        //TODO: GPSendMessageEvent
        sender.sendMessage(formattedMessage);
    }

    public String getMessage()
    {
        return message;
    }
}
