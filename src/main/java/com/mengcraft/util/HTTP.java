package com.mengcraft.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 16-12-5.
 */
public class HTTP {

    public static final String SEPARATOR = System.getProperty("line.separator");
    private static ExecutorService pool;

    static void valid(boolean b, String message) {
        if (!b) throw new RuntimeException(message);
    }

    static boolean nil(Object i) {
        return i == null;
    }

    private synchronized static void buildPool() {
        if (pool == null) {
            String size = System.getProperty("i5mc.http.pool.size", "-1");
            int i;
            try {
                i = Integer.parseInt(size);
            } catch (NumberFormatException e) {
                i = -1;
            }
            pool = (i < 1
                    ? new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new SynchronousQueue<>())
                    : new ThreadPoolExecutor(0, i, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>()));
        }
    }

    private static void initPool() {
        if (pool == null) {
            buildPool();
        }
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
            HTTPCallable call = new HTTPCallable(request);
            try {
                HTTPResponse response = call.call();
                callback.done(null, response);
            } catch (Exception e) {
                callback.done(e, null);
            }
        });
    }

}
