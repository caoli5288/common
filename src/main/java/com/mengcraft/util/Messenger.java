package com.mengcraft.util;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created on 16-4-13.
 */
public class Messenger {

    private final File ymlFile;
    private final YamlConfiguration yml;

    public Messenger(Plugin plugin) {
        ymlFile = new File(plugin.getDataFolder(), "message.yml");
        yml = YamlConfiguration.loadConfiguration(ymlFile);
    }

    @SneakyThrows
    private void save() {
        yml.save(ymlFile);
    }

    public String find(String path) {
        return find(path, "");
    }

    public String find(String path, String input) {
        val found = yml.get(path, null);
        if (found == null) {
            if (input == null) {
                return path;
            }
            if (input.indexOf('\n') == -1) {
                yml.set(path, input);
            } else {
                yml.set(path, Arrays.asList(input.split("\n")));
            }
            save();
            return input;
        } else if (found instanceof List) {
            StringJoiner joiner = new StringJoiner("\n");
            for (Object line : (List) found) {
                joiner.add(String.valueOf(line));
            }
            return joiner.toString();
        }
        return found.toString();
    }

    public void send(CommandSender receive, String path) {
        send(receive, path, "");
    }

    public void send(CommandSender receive, String path, String input) {
        sendMessage(receive, find(path, input));
    }

    public static void sendMessage(CommandSender receive, String message) {
        if (message.indexOf('\n') == -1) {
            sendLine(receive, message);
        } else {
            for (String line : message.split("\n")) {
                sendLine(receive, line);
            }
        }
    }

    public static void sendLine(CommandSender receive, String line) {
        receive.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
    }

}
