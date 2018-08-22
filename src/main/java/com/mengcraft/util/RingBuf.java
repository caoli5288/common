package com.mengcraft.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class RingBuf<T> {

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

    public void walk(IWalker<T> walker) {
        if (isEmpty()) {
            return;
        }
        _walk(first, next, walker);
    }

    private void _walk(int start, int bound, IWalker<T> walker) {
        for (int i = start; delta(i, bound) >= 1; i++) {
            walker.walk(i, _look(i));
        }
    }

    public void walk(int start, IWalker<T> walker) throws IndexOutOfBoundsException {
        walk(start, next, walker);
    }

    public boolean contains(int id) {
        if (isEmpty()) {
            return false;
        }
        return delta(first, id) > -1 && delta(id, next) >= 1;
    }

    public void walk(int start, int bound, IWalker<T> walker) throws IndexOutOfBoundsException {
        if (delta(first, start) <= -1 || delta(bound, next) <= -1) {
            throw new IndexOutOfBoundsException(String.format("start=%s,bound=%s,buf_first=%s,buf_bound=%s", start, bound, first, next));
        }
        if (delta(start, bound) <= -1) {
            throw new IndexOutOfBoundsException(String.format("start=%s,bound=%s", start, bound));
        }
        _walk(start, bound, walker);
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

    public interface IWalker<T> {

        void walk(int id, T ele);
    }
}
