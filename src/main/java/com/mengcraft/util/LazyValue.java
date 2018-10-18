package com.mengcraft.util;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class LazyValue<T> {

    private T value;

    protected abstract T compute();

    public T get() {
        if (value == null) {
            value = compute();
        }
        return value;
    }

    public static <T> LazyValue<T> of(Supplier<T> supplier) {
        return new FunctionalLazyValue<>(supplier);
    }

    public static <T> LazyValue<T> of(T value) {
        return new FunctionalLazyValue<>(() -> value);
    }

    @RequiredArgsConstructor
    public static final class FunctionalLazyValue<T> extends LazyValue<T> {

        private final Supplier<T> supplier;

        @Override
        protected T compute() {
            return Objects.requireNonNull(supplier.get(), "compute result");
        }
    }
}
