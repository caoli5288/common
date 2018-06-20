package com.mengcraft.util;

import java.util.function.Consumer;

/**
 * Created by on 12月7日.
 */
public class Helper {

    public static <T extends Comparable<T>> T min(T left, T other) {
        return left.compareTo(other) < 1 ? left : other;
    }

    public static <T extends Comparable<T>> T max(T left, T other) {
        return other.compareTo(left) < 1 ? left : other;
    }

    public static void range(int begin, int len, Consumer<Integer> consumer) {
        int max = begin + len;
        for (int i = begin; i < max; ) {
            consumer.accept(i++);
        }
    }

    public static boolean nil(Object any) {
        return any == null;
    }

}
