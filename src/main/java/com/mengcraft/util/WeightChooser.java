package com.mengcraft.util;

import java.util.Random;

/**
 * Created on 16-10-12.
 */
public class WeightChooser<T> extends Chooser<T> {

    public WeightChooser(Random random) {
        super(random);
    }

    public WeightChooser() {
        super();
    }

    @Override
    protected boolean valid(T object, double chance) {
        return !(object == null) && chance > 0;
    }

    @Override
    protected boolean valid(double value) {
        return value < Double.MAX_VALUE;
    }

    @Override
    protected double roll() {
        double border = border();
        int i = (int) border;
        if (border > i) {
            return getRandom().nextInt(i) + nextDouble(border - i);
        } else {
            return getRandom().nextInt(i);
        }
    }

    private double nextDouble(double border) {
        double rolled = getRandom().nextDouble();
        while (rolled > border) rolled -= border;
        return rolled;
    }

}
