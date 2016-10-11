package com.mengcraft.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created on 15-9-26.
 */
public class SetPicker<T> implements Iterator<T> {

    private final List<T> set;
    private final Random random;

    public static <T> SetPicker<T> of(Collection<T> collection, Random random) {
        return new SetPicker<>(collection, random);
    }

    public static <T> SetPicker<T> of(T[] array, Random random) {
        return new SetPicker<>(array, random);
    }

    private SetPicker(Collection<T> set, Random random) {
        this.set = new ArrayList<>(set);
        this.random = random;
    }

    private SetPicker(T[] array, Random random) {
        this(new ArrayList<T>(Arrays.asList(array)), random);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public T next() {
        return set.remove(random.nextInt(set.size()));
    }

    @Override
    public boolean hasNext() {
        return set.size() != 0;
    }
}
