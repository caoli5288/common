package com.mengcraft.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Monad<T> {

    private final static Monad NIL = new Monad<>(null);
    private final T obj;

    public <R> Monad<R> get(Function<T, R> functor) {
        return obj(getObj(functor));
    }

    public <R> R getObj(Function<T, R> functor) {
        if (isEmpty()) {
            return null;
        }
        return functor.apply(obj);
    }

    public T getObj() {
        return obj;
    }

    public boolean isEmpty() {
        return obj == null;
    }

    public static <T> Monad<T> obj(T obj) {
        if (obj == null) {
            return Monad.NIL;
        }
        return new Monad<>(obj);
    }
}
