package com.mengcraft.util.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class FunctionTypeRegistry<R> {

    private final Map<Class<?>, Function<Object, R>> all;

    public FunctionTypeRegistry() {
        this(new HashMap<>());
    }

    public FunctionTypeRegistry(Map<Class<?>, Function<Object, R>> all) {
        this.all = all;
    }

    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> key, Function<T, R> function) {
        all.put(key, (Function<Object, R>) function);
    }

    public <T> R handle(T input) {
        Class<?> clz = input.getClass();
        if (all.containsKey(clz)) {
            return all.get(clz).apply(input);
        }
        return null;
    }

    public Set<Class<?>> getKeys() {
        return all.keySet();
    }
}
