package com.mengcraft.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Function;

public class FunctionRegistry<T, R> {

    private final Map<String, Function<T, R>> all;

    public FunctionRegistry(Map<String, Function<T, R>> all) {
        this.all = all;
    }

    public FunctionRegistry() {
        all = Maps.newHashMap();
    }

    public void register(String key, Function<T, R> function) {
        all.put(key, function);
    }

    public R handle(String key, T input) {
        if (all.containsKey(key)) {
            return all.get(key).apply(input);
        }
        return null;
    }
}
