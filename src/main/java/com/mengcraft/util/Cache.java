package com.mengcraft.util;

import java.util.function.Supplier;

/**
 * Created on 16-5-23.
 */
public class Cache<T> {

    private final Supplier<T> fetcher;
    private final long expire;

    private T obj;
    private long last;

    public Cache(Supplier<T> fetcher) {
        this(fetcher, 0);
    }

    public Cache(Supplier<T> fetcher, long expire) {
        this.fetcher = fetcher;
        this.expire = expire;
    }

    public T get() {
        return get(false);
    }

    public T get(boolean force) {
        if (force || hasOutdated()) {
            obj = fetcher.get();
            last = System.currentTimeMillis();
        }
        return obj;
    }

    public void outdated() {
        last = -1;
    }

    public boolean hasOutdated() {
        return last + expire < System.currentTimeMillis();
    }

    public long getExpire() {
        return expire;
    }

}
