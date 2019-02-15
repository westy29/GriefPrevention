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

package me.ryanhamshire.GriefPrevention.enums;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

public enum Message
{
    CLAIM_CREATED("&aClaim created! Use /trust to share it with friends."),
    CLAIMTOOL_CREATE_START("&bEntered claim creation mode, now click the opposite corner to create a claim."),
    CLAIM_FAIL_INSUFFICIENT_CLAIMBLOCKS("&cYou do not have enough claim blocks to do this. You have {0} claimblocks."),
    CLAIM_FAIL_OVERLAPS("Overlaps another claim."),
    CLAIM_FAIL_NO_PERMISSION("&cYou currently do not have the power to claim."),
    CLAIM_RESIZED("&aClaim resized. You now have {0} claimblocks remaining."),
    CLAIMTOOL_RESIZE_START("&bResizing claim. Click where you want to move this corner."),
    CLAIM_ABANDONED("Claim abandoned."),
    NoClaimHere("No claim here."),
    CLAIM_NO_TRUST_MANAGE("&cYou are not trusted to manage this claim."),
    TOOL_NO_BLOCK_FOUND("No block found! (Or too far away.)");

    private String message;

    Message(String defaultMessage)
    {
        this.message = ChatColor.translateAlternateColorCodes('&', defaultMessage);
    }

    public static YamlConfiguration initialize(YamlConfiguration messagesFile)
    {
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
