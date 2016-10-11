package com.mengcraft.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * Created by on 16-5-13.
 */
public class Chooser<T> {

    private final List<Option<T>> set = new ArrayList<>();
    private final Random random;

    public Chooser(Random random) {
        this.random = random;
    }

    public Chooser() {
        this(current());
    }

    public void put(T object, double chance) {
        if (!valid(object, chance)) {
            throw new IllegalArgumentException();
        }
        add(object, chance);
    }

    protected boolean valid(T object, double chance) {
        return !(object == null) && chance > 0 && chance < 1;
    }

    public final double border() {
        return set.get(0).chance;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (set.isEmpty()) {
            throw new ArrayIndexOutOfBoundsException("get");
        }
        return get(roll());
    }

    protected double roll() {
        return random.nextDouble();
    }

    private T get(double rolled) {
        T result = null;
        for (Option<T> option : set) {
            if (rolled < option.chance) {
                result = option.object;
            } else {
                return result;
            }
        }
        return result;
    }

    private void add(T object, double chance) {
        for (Option option : set) {
            if (!valid(option.chance + chance)) {
                throw new ArrayIndexOutOfBoundsException("put");
            }
            option.chance += chance;
        }
        set.add(new Option<>(object, chance));
    }

    protected boolean valid(double value) {
        return !(value > 1);
    }

    public Random getRandom() {
        return random;
    }

    private static class Option<T> {
        private final T object;
        private double chance;

        private Option(T object, double chance) {
            this.object = object;
            this.chance = chance;
        }
    }

}
