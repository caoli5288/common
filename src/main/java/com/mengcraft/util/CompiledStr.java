package com.mengcraft.util;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiledStr implements Function<OfflinePlayer, String> {

    private final String template;
    private String[] texts;
    private PlaceholderExpansion[] expansions;
    private String[] commands;

    private CompiledStr(String template) {
        this.template = template;
    }

    public static CompiledStr compile(String text) {
        return compile(Style.PERCENT, text);
    }

    public static CompiledStr compile(Style style, String text) {
        CompiledStr let = new CompiledStr(text);
        let.compile(style);
        return let;
    }

    @Override
    public String apply(OfflinePlayer player) {
        StringBuilder sb = new StringBuilder(template.length());
        for (int i = 0; i < texts.length; i++) {
            PlaceholderExpansion it = expansions[i];
            if (it == null) {
                sb.append(texts[i]);
            } else {
                sb.append(it.onRequest(player, commands[i]));
            }
        }
        return sb.toString();
    }

    private void compile(Style style) {
        ArrayList<String> textList = new ArrayList<>();
        ArrayList<PlaceholderExpansion> expansionList = new ArrayList<>();
        ArrayList<String> commandList = new ArrayList<>();
        LocalExpansionManager lem = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
        Matcher mat = style.matcher(template);
        int seg = 0;
        while (mat.find()) {
            int f = mat.start();
            if (seg != f) {
                String segment = template.substring(seg, f);
                textList.add(segment);
                expansionList.add(null);
                commandList.add(null);
            }
            seg = mat.end();
            String exp = mat.group(1);
            String command = mat.group(2);
            PlaceholderExpansion expansion = lem.getExpansion(exp);
            if (expansion != null) {
                textList.add(null);
                expansionList.add(expansion);
                commandList.add(command);
            }
        }
        if (seg != template.length()) {
            String segment = template.substring(seg);
            textList.add(segment);
            expansionList.add(null);
            commandList.add(null);
        }
        texts = textList.toArray(new String[0]);
        expansions = expansionList.toArray(new PlaceholderExpansion[0]);
        commands = commandList.toArray(new String[0]);
    }

    public enum Style {
        PERCENT(Pattern.compile("%([^%_]+)_([^%]+)%")),
        DOLLAR(Pattern.compile("\\$\\{([^}_]+)_([^}]+)}"));

        private final Pattern pattern;

        Style(Pattern pattern) {
            this.pattern = pattern;
        }

        public Matcher matcher(String template) {
            return pattern.matcher(template);
        }
    }
}
