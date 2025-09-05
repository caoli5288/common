package com.mengcraft.util;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiledStr implements Function<OfflinePlayer, String> {

    private final String template;
    private List<Function<OfflinePlayer, String>> list;

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
        StringBuilder sb = new StringBuilder();
        for (Function<OfflinePlayer, String> fun : list) {
            sb.append(fun.apply(player));
        }
        return sb.toString();
    }

    private void compile(Style style) {
        list = Lists.newArrayList();
        LocalExpansionManager lem = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
        Matcher mat = style.matcher(template);
        int seg = 0;
        while (mat.find()) {
            int f = mat.start();
            if (seg != f) {
                String segment = template.substring(seg, f);
                list.add(__ -> segment);
            }
            seg = mat.end();
            String exp = mat.group(1);
            String command = mat.group(2);
            PlaceholderExpansion expansion = lem.getExpansion(exp);
            if (expansion != null) {
                list.add(it -> expansion.onRequest(it, command));
            }
        }
        if (seg != template.length()) {
            String segment = template.substring(seg);
            list.add(__ -> segment);
        }
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
