package com.mengcraft.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Created on 16-12-7.
 */
public class RollingPool<E> {

    private final LinkedList<M<E>> pool = new LinkedList<>();
    private final Random random;

    public RollingPool(Random random) {
        this.random = random;
    }

    static class M<E> {
        E ele;
        int i;
    }

    /**
     * @return the selected element
     * @throws NoSuchElementException if pool is empty
     */
    public E roll() {
        if (pool.isEmpty()) throw new NoSuchElementException();
        Iterator<M<E>> i = pool.iterator();
        int l = random.nextInt(pool.getLast().i);
        M<E> n;
        while (i.hasNext()) {
            n = i.next();
            if (l < n.i) return n.ele;
        }
        throw new NoSuchElementException();
    }

    /**
     * @param element the element
     * @param weight  the weight
     * @throws IndexOutOfBoundsException if total weight greater than {@link Integer#MAX_VALUE},
     *                                   or weight less than 0
     */
    public void put(E element, int weight) {
        if (weight < 1) throw new IndexOutOfBoundsException();
        M<E> m = new M<>();
        m.ele = element;
        m.i = weight;
        if (!pool.isEmpty()) {
            int last = pool.getLast().i;
            m.i = m.i + last;
            if (m.i < last) throw new IndexOutOfBoundsException();
        }
        pool.addLast(m);
    }

}
