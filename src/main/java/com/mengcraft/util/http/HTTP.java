package com.mengcraft.util.http;

import lombok.val;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        if (b) throw new IllegalStateException(message);
    }

    static boolean nil(Object i) {
        return i == null;
    }

    private synchronized static void buildPool() {
        if (nil(pool)) {
            String size = System.getProperty("i5mc.http.pool.size", "-1");
            int i;
            if (nil(size)) i = -1;
            else
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
        valid(nil(pool), "nil");
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
    public static void flush(long time) throws InterruptedException {
        HTTPTask.LATCH.hold(time);
    }

    public static void flush() throws InterruptedException {
        flush(Long.MAX_VALUE);
    }

    public static Future<Integer> open(HTTPRequest request) {
        valid(nil(request), "open " + request);
        initPool();
        HTTPTask.LATCH.incrementAndGet();
        return pool.submit(new HTTPTask(request));
    }

    public static void open(HTTPRequest request, Callback back) {
        valid(nil(request) || nil(back), "open " + request + " " + back);
        initPool();
        request.setCallback(back);
        HTTPTask.LATCH.incrementAndGet();
        pool.execute(() -> {
            val task = new HTTPTask(request);
            try {
                task.call();
            } catch (Exception e) {
                back.call(e, null);
            }
        });// override response handler if present
    }

}
