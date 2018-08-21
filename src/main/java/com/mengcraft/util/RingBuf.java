package com.mengcraft.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class RingBuf<T> {

    private final Object[] buf;
    private final int capacity;
    private final int mod;
    private int next;
    private int first;

    public RingBuf(int power) {
        capacity = 1 << power;
        mod = capacity - 1;
        buf = new Object[capacity];
    }

    public int add(T add) throws IndexOutOfBoundsException {
        if (isFull()) {
            throw new IndexOutOfBoundsException("add");
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

    public int capacity() {
        return capacity;
    }

    public T head() throws IndexOutOfBoundsException {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("head");
        }
        return get(first);
    }

    public T get(int get) throws IndexOutOfBoundsException {
        if (!contains(get)) {
            throw new IndexOutOfBoundsException("get");
        }
        return (T) buf[get & mod];
    }

    public T tail() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("tail");
        }
        return get(next - 1);
    }

    public T remove() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException("remove");
        }
        return (T) buf[first++ & mod];
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public void walk(IWalker<T> walker) {
        _walk(first, next, walker);
    }

    private void _walk(int start, int bound, IWalker<T> walker) {
        for (int _i = start; _i < bound; _i++) {
            walker.walk((T) buf[_i & mod]);
        }
    }

    public void walk(int start, IWalker<T> walker) throws IndexOutOfBoundsException {
        if (!contains(start)) {
            throw new IndexOutOfBoundsException("get start");
        }
        _walk(start, next, walker);
    }

    public boolean contains(int id) {
        int delta = delta(id, next);
        return delta > 0 && delta <= length();
    }

    public void walk(int start, int len, IWalker<T> walker) throws IndexOutOfBoundsException {
        if (!contains(start)) {
            throw new IndexOutOfBoundsException("get start");
        }
        int bound = start + len;
        if (!contains(bound - 1)) {
            throw new IndexOutOfBoundsException("get len");
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

        void walk(T ele);
    }
}
