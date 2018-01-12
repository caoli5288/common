package com.mengcraft.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class TimeTickUtil {

    private static final TimeUnit UNIT_FALLBACK = TimeUnit.SECONDS;

    private static final Map<?, Integer> UNIT = ImmutableMap.of(
            "m", 60,
            "h", 3600,
            "d", 86400
    );

    private static final Pattern NUM = Pattern.compile("\\d+");
    private static final Pattern NUM_WITH_UNIT = Pattern.compile("\\d+[mhd]");

    public static long toTick(Object input) {
        return toTime(input, TimeUnit.SECONDS) * 20;
    }

    public static long toTime(Object input, TimeUnit unit) {
        if (input instanceof Number) {
            return unit.convert(((Number) input).intValue(), UNIT_FALLBACK);
        }

        String num = String.valueOf(input);

        if (NUM.matcher(num).matches()) {
            return unit.convert(Integer.parseInt(num), UNIT_FALLBACK);
        }

        if (NUM_WITH_UNIT.matcher(num).matches()) {
            return unit.convert(Integer.parseInt(num.substring(0, num.length() - 1)) * UNIT.get(num.substring(num.length() - 1)), TimeUnit.SECONDS);
        }

        throw new IllegalArgumentException(num);
    }

}
