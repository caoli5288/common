package com.mengcraft.util;

import com.google.common.base.Preconditions;

import java.util.Random;
import java.util.TreeMap;

public class RandomObj<T> extends Random {

    private final TreeMap<Integer, T> elements = new TreeMap<>();
    private int _key;

    public T nextObj() {
        Preconditions.checkState(!elements.isEmpty(), "no elements configured");
        return elements.tailMap(nextInt(_key), false).firstEntry().getValue();
    }

    public void registerObj(int value, T obj) {
        _key = _key + value;
        elements.put(_key, obj);
    }

}
