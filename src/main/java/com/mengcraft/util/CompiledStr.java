package com.mengcraft.util;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.OfflinePlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiledStr implements Function<OfflinePlayer, String> {

    private final @NotNull String template;
    private @NotNull String[] texts;
    private @NotNull PlaceholderExpansion[] expansions;

    private CompiledStr(@NotNull String template) {
        this.template = template;
    }

    public static @NotNull CompiledStr compile(@NotNull String text) {
        return compile(Style.PERCENT, text);
    }

    public static @NotNull CompiledStr compile(@NotNull Style style, @NotNull String text) {
        CompiledStr let = new CompiledStr(text);
        let.compile(style);
        return let;
    }

    @Override
    public @NotNull String apply(@Nullable OfflinePlayer player) {
        StringBuilder sb = new StringBuilder(template.length());
        for (int i = 0; i < texts.length; i++) {
            PlaceholderExpansion it = expansions[i];
            if (it == null) {
                sb.append(texts[i]);
            } else {
                sb.append(it.onRequest(player, texts[i]));
            }
        }
        return sb.toString();
    }

    private void compile(@NotNull Style style) {
        List<String> allTexts = Lists.newArrayList();
        List<PlaceholderExpansion> allExpansions = Lists.newArrayList();
        LocalExpansionManager lem = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
        Matcher mat = style.matcher(template);
        int seg = 0;
        while (mat.find()) {
            int f = mat.start();
            if (seg != f) {
                String segment = template.substring(seg, f);
                allTexts.add(segment);
                allExpansions.add(null);
            }
            seg = mat.end();
            String exp = mat.group(1);
            String command = mat.group(2);
            PlaceholderExpansion expansion = lem.getExpansion(exp);
            if (expansion != null) {
                allTexts.add(command);
                allExpansions.add(expansion);
            }
        }
        if (seg != template.length()) {
            String segment = template.substring(seg);
            allTexts.add(segment);
            allExpansions.add(null);
        }
        texts = allTexts.toArray(new String[0]);
        expansions = allExpansions.toArray(new PlaceholderExpansion[0]);
    }

    public enum Style {
        PERCENT(Pattern.compile("%([^%_]+)_([^%]+)%")),
        DOLLAR(Pattern.compile("\\$\\{([^}_]+)_([^}]+)}"));

        private final Pattern pattern;

        Style(@NotNull Pattern pattern) {
            this.pattern = pattern;
        }

        public @NotNull Matcher matcher(@NotNull String template) {
            return pattern.matcher(template);
        }
    }
}
