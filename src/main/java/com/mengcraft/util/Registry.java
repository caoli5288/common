package com.mengcraft.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Registry<T> {

    private final Map<Object, Consumer<T>> all;

    public Registry(Map<Object, Consumer<T>> all) {
        this.all = all;
    }

    public Registry() {
        all = new HashMap<>();
    }

    public void register(Object key, Consumer<T> function) {
        all.put(key, function);
    }

    public void handle(Object key, T input) {
        if (all.containsKey(key)) {
            all.get(key).accept(input);
        }
    }

    public Set<Object> getKeys() {
        return all.keySet();
    }
}
