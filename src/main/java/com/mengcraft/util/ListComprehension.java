package com.mengcraft.util;

import lombok.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ListComprehension<T> implements Iterator<T> {

    private final Function<T, T> f;
    private T next;

    ListComprehension(@NonNull T seed, @NonNull Function<T, T> f) {
        this.f = f;
        next = seed;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        if (hasNext()) {
            T current = next;
            next = f.apply(current);
            return current;
        }
        throw new NoSuchElementException("next");
    }

    public static <T> ListComprehension<T> of(T seed, Function<T, T> f) {
        return new ListComprehension<>(seed, f);
    }
}
