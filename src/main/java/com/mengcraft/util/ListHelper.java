package com.mengcraft.util;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public static <E> int count(Collection<E> i, Object obj) {
        int count = 0;
        for (E e : i) {
            if (e.equals(obj)) {
                count++;
            }
        }
        return count;
    }

    public static <E> String join(Collection<E> i, String separator) {
        val out = new StringBuilder();
        val l = i.iterator();
        while (l.hasNext()) {
            if (out.length() > 0) out.append(separator);
            out.append(l.next());
        }
        return out.toString();
    }

    public static <E> void forEach(Collection<E> i, Predicate<E> p, Consumer<E> c) {
        for (E e : i) {
            if (p.test(e)) {
                c.accept(e);
            }
        }
    }

    public static <T> void forEachRemaining(Iterator<T> i, Predicate<T> p, Consumer<T> c) {
        i.forEachRemaining(t -> {
            if (p.test(t)) {
                c.accept(t);
            }
        });
    }

    public static <T> List<T> remove(Collection<T> origin, Collection<T> other) {
        List<T> output = new ArrayList<>(origin);
        for (T element : other) {
            while (output.remove(element)) {
                ;
            }
        }
        return output;
    }

    public static <T> List<T> reduce(Collection<T> in, Predicate<T> p) {
        List<T> out = new ArrayList<>();
        forEach(in, p, t -> {
            out.add(t);
        });
        return out;
    }

    public static <R, E> List<R> collect(List<E> in, Function<E, R> func) {
        List<R> out = new ArrayList<>(in.size());
        for (E i : in) {
            out.add(func.apply(i));
        }
        return out;
    }

}
