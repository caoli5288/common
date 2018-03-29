package com.mengcraft.util;

import lombok.AccessLevel;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Add support for {@code ${abc${bcd${cde}}}} like placeholder.
 */
public class Formatter {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{(?<label>.+)}");

    @Setter(AccessLevel.PACKAGE)
    private static boolean replacePlaceholder;

    public static String format(Player p, String input) {
        return input == null ? null : splitLine(replacePlaceholder ? multi(p, input) : ChatColor.translateAlternateColorCodes('&', input));
    }

    protected static String multi(Player p, String input) {
        Matcher matcher = PATTERN.matcher(input);
        while (matcher.find()) {
            String label = PlaceholderAPI.setPlaceholders(p, "%" + multi(p, matcher.group("label")) + "%");
            input = input.replace(matcher.group(), label);
        }
        return PlaceholderAPI.setPlaceholders(p, input);
    }

    public static String splitLine(String input) {
        return input == null ? null : input.replace("\\n", "\n");
    }
}
