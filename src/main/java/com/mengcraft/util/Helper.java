package com.mengcraft.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mengcraft.util.Tuple.tuple;

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
        for (int i = begin; i < max; i++) {
            consumer.accept(i);
        }
    }

    public static <E> String join(Collection<E> input, Function<E, String> function, String delimiter) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (E ele : input) {
            joiner.add(function.apply(ele));
        }
        return joiner.toString();
    }

    public static boolean nil(Object any) {
        return any == null;
    }

    public static <K, V, KK, VV> Map<KK, VV> map(Map<K, V> input, Function<Tuple<K, V>, Tuple<KK, VV>> function) {
        Map<KK, VV> container = new HashMap<>();
        input.forEach((k, v) -> {
            Tuple<KK, VV> tuple = function.apply(tuple(k, v));
            container.put(tuple.left(), tuple.right());
        });
        return container;
    }

}
