package com.mengcraft.util.http;

import lombok.val;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Created on 16-12-5.
 */
public final class HTTP {

    static final String SEPARATOR = System.getProperty("line.separator");
    static ThreadPoolExecutor pool;

    HTTP() {// static class
        throw new IllegalStateException("static class");
    }

    static void valid(boolean b, String message) {
        if (!b) throw new RuntimeException(message);
    }

    static boolean nil(Object i) {
        return i == null;
    }

    private synchronized static void buildPool() {
        if (nil(pool)) {
            String size = System.getProperty("i5mc.http.pool.size", "-1");
            int i;
            try {
                i = Integer.parseInt(size);
            } catch (NumberFormatException e) {
                i = -1;
            }
            pool = (i < 1
                    ? new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>())
                    : new ThreadPoolExecutor(0, i, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>()));
        }
    }

    private static void initPool() {
        if (nil(pool)) {
            buildPool();
        }
    }

    public static ThreadPoolExecutor setPool(ThreadPoolExecutor pool) {
        valid(!nil(pool), "nil");
        val b = HTTP.pool;
        HTTP.pool = pool;
        return b;
    }

    /**
     * Blocking until pool flushed.
     *
     * @param time wait time
     * @return {@code true} if pool flushed without wait timeout
     * @throws InterruptedException thrown if interrupted
     */
    public static boolean flush(long time) throws InterruptedException {
        val pool = HTTP.pool;
        if (nil(pool) || pool.getQueue().isEmpty()) return true;
        for (long i = 1; i < time; i++) {
            sleep(1);
            if (pool.getQueue().isEmpty()) return true;
        }
        return false;// 在考虑用阻塞实现是不是友好一些
    }

    public static boolean flush() throws InterruptedException {
        return flush(TimeUnit.MINUTES.toMillis(1));
    }

    public static Future<HTTPResponse> open(HTTPRequest request) {
        valid(!nil(request), "open " + request);
        initPool();
        return pool.submit(new HTTPCallable(request));
    }

    public static void open(HTTPRequest request, HTTPCallback callback) {
        valid(!(nil(request) || nil(callback)), "open " + request + " " + callback);
        initPool();
        pool.execute(() -> {
            val run = new HTTPCallable(request);
            try {
                val response = run.call();
                callback.done(null, response);
            } catch (Exception e) {
                callback.done(e, null);
            }
        });
    }

}
