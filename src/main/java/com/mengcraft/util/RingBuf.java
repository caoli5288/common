package com.mengcraft.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RingBuf<T> implements Iterable<T> {

    private final Object[] buf;
    private final int capacity;
    private final int mod;
    private int next;
    private int first;

    /**
     * @param power capacity = 1 << power
     */
    public RingBuf(int power) {
        capacity = 1 << power;
        mod = capacity - 1;
        buf = new Object[capacity];
    }

    RingBuf(int power, int start) {// Test only
        this(power);
        first = next = start;
    }

    public int add(T add) throws IndexOutOfBoundsException {
        return add(add, false);
    }

    /**
     * @return sequence id of added element
     * @throws IndexOutOfBoundsException if buf is full and overwrite flag false
     */
    public int add(T add, boolean overwrite) {
        if (isFull()) {
            if (overwrite) {
                first++;
            } else {
                throw new IndexOutOfBoundsException("full buf");
            }
        }
        int ret = next++;
        buf[ret & mod] = add;
        return ret;
    }

    public boolean isFull() {
        return length() == capacity;
    }

    public int length() {
        return delta(first, next);
    }

    /**
     * We use delta everywhere to prevent int overflow.
     */
    private static int delta(int min, int max) {
        return max - min;
    }

    private static int mod(int value, int mod) {
        return value + mod;
    }

    public int capacity() {
        return capacity;
    }

    /**
     * @return the head, or null if buf is empty
     */
    public T head() {
        if (isEmpty()) {
            return null;
        }
        return _look(first);
    }

    /**
     * @return element with given sequence id
     * @throws IndexOutOfBoundsException if sequence id out of buf
     */
    public T get(int id) throws IndexOutOfBoundsException {
        if (!contains(id)) {
            throw new IndexOutOfBoundsException(String.format("get=%s,buf_first=%s,buf_next=%s", id, first, next));
        }
        return _look(id);
    }

    public T _look(int id) {
        return (T) buf[id & mod];
    }

    /**
     * @return the tail, or null if buf is empty
     */
    public T tail() {
        if (isEmpty()) {
            return null;
        }
        return _look(mod(next, -1));
    }

    /**
     * @return the removed first element
     * @throws NoSuchElementException is buf is empty
     */
    public T remove() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException("remove");
        }
        T out = _look(first);
        first++;
        return out;
    }

    /**
     * @return true if first index shift to right
     */
    public boolean shift() {
        if (isEmpty()) {
            return false;
        }
        first++;
        return true;
    }

    public boolean isEmpty() {
        return first == next;
    }

    public boolean contains(int id) {
        if (isEmpty()) {
            return false;
        }
        return delta(first, id) > -1 && delta(id, next) >= 1;
    }

    @Override
    public Range<T> iterator() {
        return new Range<>(this, first, next);
    }

    public Range<T> iterator(int begin) {
        return iterator(begin, next);
    }

    public Range<T> iterator(int begin, int bound) {
        if (delta(first, begin) <= -1 || delta(bound, next) <= -1) {
            throw new IndexOutOfBoundsException(String.format("begin=%s,bound=%s,buf_first=%s,buf_bound=%s", begin, bound, first, next));
        }
        if (delta(begin, bound) <= -1) {
            throw new IndexOutOfBoundsException(String.format("begin=%s,bound=%s", begin, bound));
        }
        return new Range<>(this, begin, bound);
    }

    public int next() {
        return next;
    }

    public int first() {
        return first;
    }

    public void resetFully() {
        reset();
        Arrays.fill(buf, null);
    }

    public void reset() {
        next = first = 0;
    }

    public int remaining() {
        return capacity - length();
    }

    public static class Range<T> implements Iterator<T>, Iterable<T> {

        private final RingBuf<T> _buf;
        private final int bound;

        private int id;
        private int inext;

        private Range(RingBuf<T> buf, int begin, int bound) {
            _buf = buf;
            this.bound = bound;
            inext = begin;
        }

        @Override
        public boolean hasNext() {
            return delta(inext, bound) >= 1;
        }

        @Override
        public T next() {
            id = inext;
            inext = mod(inext, 1);
            return _buf._look(id);
        }

        public int id() {
            return id;
        }

        @Override
        public Iterator<T> iterator() {
            return this;
        }

    }

}
