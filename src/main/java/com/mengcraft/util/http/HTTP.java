package com.mengcraft.util.http;

import lombok.val;

import java.util.concurrent.Future;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created on 16-12-5.
 */
public final class HTTP {

    static final String SEPARATOR = System.getProperty("line.separator");

    private HTTP() {// utility class
        throw new IllegalStateException("utility class");
    }

    static void thr(boolean b, String message) {
        if (b) throw new IllegalStateException(message);
    }

    static boolean nil(Object i) {
        return i == null;
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
        thr(nil(request), "open " + request);
        HTTPTask.LATCH.incrementAndGet();
        return supplyAsync(() -> {
            val task = new HTTPTask(request);
            try {
                return task.call();
            } finally {
                HTTPTask.LATCH.decrementAndNotify();
            }
        });
    }

    public static void open(HTTPRequest request, Callback back) {
        thr(nil(request) || nil(back), "open " + request + " " + back);
        HTTPTask.LATCH.incrementAndGet();
        runAsync(() -> {
            request.setCallback(back);
            val task = new HTTPTask(request);
            try {
                task.call();
            } catch (Exception e) {
                back.call(e, null);
            } finally {
                HTTPTask.LATCH.decrementAndNotify();
            }
        });
    }

}
