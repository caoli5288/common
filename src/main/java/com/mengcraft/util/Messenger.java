package com.mengcraft.util;

import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 16-4-13.
 */
public class Messenger {

    private final static String PREFIX = "message.";
    private final Plugin plugin;

    public Messenger(Plugin plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender receive, String path) {
        send(receive, path, "");
    }

    public void send(CommandSender receive, String path, String input) {
        val found = find(path, input);
        if (found.indexOf('\n') == -1) {
            sendLine(receive, found);
        } else {
            for (String line : found.split("\n")) {
                sendLine(receive, line);
            }
        }
    }

    public void sendLine(CommandSender receive, String line) {
        receive.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
    }

    public String find(String path) {
        return find(path, "");
    }

    public String find(String path, String input) {
        val found = plugin.getConfig().get(real(path), null);
        if (found == null) {
            if (input == null || input.isEmpty()) return path;
            return find__(path, input);
        } else if (found instanceof List) {
            return find_((List) found);
        }
        return found.toString();
    }

    private String find_(List found) {
        val b = new StringBuilder();
        val i = found.iterator();
        while (i.hasNext()) {
            b.append(i.next());
            if (i.hasNext()) b.append('\n');
        }
        return b.toString();
    }

    private String find__(String path, String input) {
        if (input.indexOf('\n') == -1) {
            plugin.getConfig().set(real(path), input);
            plugin.saveConfig();
        } else {
            plugin.getConfig().set(real(path), Arrays.asList(input.split("\n")));
            plugin.saveConfig();
        }
        return input;
    }

    private String real(String path) {
        return PREFIX + path;
    }

}
