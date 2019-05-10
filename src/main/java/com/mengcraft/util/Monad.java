package com.mengcraft.util;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Monad<T> {

    public abstract void then(Consumer<T> consumer);

    public abstract <R> Monad<R> map(Function<T, R> function);

    public static <T> Monad<T> maybe(T obj) {
        if (obj == null) {
            return Nothing.INSTANCE;
        }
        return new Maybe<>(obj);
    }

    public static class Nothing extends Monad {

        private static final Nothing INSTANCE = new Nothing();

        private Nothing() {
        }

        @Override
        public void then(Consumer consumer) {

        }

        @Override
        public Monad map(Function function) {
            return this;
        }
    }

    public static class Maybe<T> extends Monad<T> {

        private final T obj;

        private Maybe(T obj) {
            this.obj = obj;
        }

        @Override
        public void then(Consumer<T> consumer) {
            consumer.accept(obj);
        }

        @Override
        public <R> Monad<R> map(Function<T, R> function) {
            R applied = function.apply(obj);
            if (applied == null) {
                return Nothing.INSTANCE;
            }
            return new Maybe<>(applied);
        }
    }
}
