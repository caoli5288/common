package com.mengcraft.util;

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
}
