package com.mengcraft.util;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StrTemplate {

    private final String template;
    private String[] allParts;
    private boolean[] allKeys;

    public StrTemplate(@NotNull String template, @NotNull String left, @NotNull String right) {
        this.template = template;
        loadAll(left, right);
    }

    private void loadAll(@NotNull String left, @NotNull String right) {
        List<String> allParts = Lists.newArrayList();
        List<Boolean> allKeys = Lists.newArrayList();
        int len = 0;
        while (len < template.length()) {
            int beg = template.indexOf(left, len);
            if (beg == -1) {
                String text = template.substring(len);
                allParts.add(text);
                allKeys.add(false);
                break;
            }

            if (beg > len) {
                String text = template.substring(len, beg);
                allParts.add(text);
                allKeys.add(false);
            }

            int lenEnd = beg + left.length();
            int end = template.indexOf(right, lenEnd);
            if (end == -1) {
                String text = template.substring(beg);
                allParts.add(text);
                allKeys.add(false);
                break;
            }

            String key = template.substring(lenEnd, end);
//            String placeholder = template.substring(fd, end + right.length());
            allParts.add(key);
            allKeys.add(true);
            len = end + right.length();
        }
        this.allParts = allParts.toArray(new String[0]);
        this.allKeys = new boolean[allKeys.size()];
        for (int i = 0; i < allKeys.size(); i++) {
            this.allKeys[i] = allKeys.get(i);
        }
    }

    public @NotNull String replace(@NotNull Map<String, String> values) {
        StringBuilder line = new StringBuilder(template.length());
        for (int i = 0; i < allParts.length; i++) {
            line.append(allKeys[i] ? values.getOrDefault(allParts[i], "") : allParts[i]);
        }
        return line.toString();
    }

    public @NotNull String replace(@NotNull Function<String, String> values) {
        StringBuilder line = new StringBuilder(template.length());
        for (int i = 0; i < allParts.length; i++) {
            line.append(allKeys[i] ? values.apply(allParts[i]) : allParts[i]);
        }
        return line.toString();
    }
}
