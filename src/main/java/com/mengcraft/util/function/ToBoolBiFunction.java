package com.mengcraft.util.function;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ToBoolBiFunction<T, U>  {

    boolean apply(T t, U u);

    default <R> BiFunction<T, U, R> andThen(Function<Boolean, R> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
