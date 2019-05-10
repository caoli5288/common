package com.mengcraft.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Monad<T> {

    public abstract void then(Consumer<T> consumer);

    public abstract <R> Monad<R> map(Function<T, R> function);

    public abstract Monad<T> filter(Predicate<T> predicate);

    public static <T> Monad<T> maybe(T obj) {
        if (obj == null) {
            return Nothing.INSTANCE;
        }
        return new Just<>(obj);
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

        @Override
        public Monad filter(Predicate predicate) {
            return this;
        }
    }

    public static class Just<T> extends Monad<T> {

        private final T obj;

        private Just(T obj) {
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
            return new Just<>(applied);
        }

        @Override
        public Monad<T> filter(Predicate<T> predicate) {
            return predicate.test(obj) ? this : Nothing.INSTANCE;
        }
    }
}
