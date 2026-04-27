package com.mengcraft.util;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StrTemplate {

    private final List<Function<Function<String, String>, String>> all = Lists.newArrayList();
    private final String template;

    public StrTemplate(@NotNull String template, @NotNull String left, @NotNull String right) {
        this.template = template;
        loadAll(left, right);
    }

    private void loadAll(@NotNull String left, @NotNull String right) {
        int len = 0;
        while (len < template.length()) {
            int beg = template.indexOf(left, len);
            if (beg == -1) {
                String text = template.substring(len);
                all.add(__ -> text);
                break;
            }

            if (beg > len) {
                String text = template.substring(len, beg);
                all.add(__ -> text);
            }

            int lenEnd = beg + left.length();
            int end = template.indexOf(right, lenEnd);
            if (end == -1) {
                String text = template.substring(beg);
                all.add(__ -> text);
                break;
            }

            String key = template.substring(lenEnd, end);
//            String placeholder = template.substring(fd, end + right.length());
            all.add(f -> f.apply(key));
            len = end + right.length();
        }
    }

    public @NotNull String replace(@NotNull Map<String, String> values) {
        return replace(key -> values.getOrDefault(key, ""));
    }

    public @NotNull String replace(@NotNull Function<String, String> values) {
        StringBuilder line = new StringBuilder(template.length());
        for (Function<Function<String, String>, String> f : all) {
            line.append(f.apply(values));
        }
        return line.toString();
    }
}
