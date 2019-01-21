package com.mengcraft.util.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class TypeRegistry {

    private final Map<Class<?>, Consumer<Object>> all;

    public TypeRegistry() {
        this(new HashMap<>());
    }

    public TypeRegistry(Map<Class<?>, Consumer<Object>> all) {
        this.all = all;
    }

    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> key, Consumer<T> function) {
        all.put(key, (Consumer<Object>) function);
    }

    public void handle(Object input) {
        Class<?> clz = input.getClass();
        if (all.containsKey(clz)) {
            all.get(clz).accept(input);
        }
    }

    public Set<Class<?>> getKeys() {
        return all.keySet();
    }
}
