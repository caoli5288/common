package com.mengcraft.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Created on 16-3-9.
 */
public final class ListHelper {

    @SuppressWarnings("unchecked")
    public static <E> List<E> concat(Collection<E> i, Collection<E> i1) {
        List<E> out = new ArrayList<>(i.size() + i1.size());
        out.addAll(i);
        out.addAll(i1);
        return out;
    }

    public static <E> int count(Collection<E> input, Object any) {
        return Math.toIntExact(input.stream().filter(i -> i.equals(any)).count());
    }

    public static <R, E> List<R> collect(List<E> in, Function<E, R> func) {
        return in.stream().map(func).collect(toList());
    }

    public static <T> T[] asArray(T... i) {// 这里会有语义问题
        if (i == null) throw new NullPointerException();
        return i;
    }

    public static <T> boolean any(Collection<T> input, Predicate<T> p) {
        for (T i : input) {
            if (p.test(i)) return true;
        }
        return false;
    }

    public static <E> String join(Collection<E> i, String separator) {
        val out = new StringBuilder();
        i.forEach(l -> {
            if (out.length() > 0) out.append(separator);
            out.append(l);

        });
        return out.toString();
    }

    public static <E> String join(Iterator<E> input, String separator) {
        StringBuilder buf = new StringBuilder();
        input.forEachRemaining(l -> {
            if (buf.length() >= 1) {
                buf.append(separator);
            }
            buf.append(String.valueOf(l));
        });
        return String.valueOf(buf);
    }

    public static <T> List<T> remove(Collection<T> origin, Collection<T> other) {
        return origin.stream().filter(i -> !other.contains(i)).collect(toList());
    }

    public static <T> List<T> reduce(Collection<T> in, Predicate<T> p) {
        List<T> out = new ArrayList<>();
        walk(in, p, t -> {
            out.add(t);
        });
        return out;
    }

    public static <E> void walk(Iterator<E> i, Predicate<E> p, Consumer<E> c) {
        StreamSupport.stream(((Iterable<E>) (() -> i)).spliterator(), false).filter(p).forEach(c);
    }

    public static <E> void walk(Collection<E> i, Predicate<E> p, Consumer<E> c) {
        i.stream().filter(p).forEach(c);
    }

    public static <E> void walk(Collection<E> input, BiConsumer<E, Integer> consumer) {
        if (input == null || input.isEmpty() || consumer == null) {
            return;
        }
        Iterator<E> it = input.iterator();
        for (int idx = -1; it.hasNext(); ) {
            consumer.accept(it.next(), ++idx);
        }
    }

    public static <T> List<T> filter(List<T> all, Predicate<T> filter) {
        return all.stream().filter(filter).collect(toList());
    }

    public static <K, V> Multimap<K, V> groupBy(List<V> all, Function<V, K> func, K defaultKey) {
        Multimap<K, V> out = ArrayListMultimap.create();
        all.forEach(l -> {
            K key = func.apply(l);
            out.put((key == null && !(defaultKey == null)) ? defaultKey : key, l);
        });
        return out;
    }

    public static <I, K, V> void mapping(Collection<I> input, Function<I, Pair<K, V>> func, Map<K, V> map) {
        input.stream().map(func).forEach(pair -> map.put(pair.key, pair.value));
    }

    @Data
    public static class Pair<K, V> {

        private final K key;
        private final V value;
    }

}
