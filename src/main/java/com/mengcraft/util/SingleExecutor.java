package com.mengcraft.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 16-10-31.
 */
public class SingleExecutor extends ThreadPoolExecutor {

    private static final AtomicInteger COUNT = new AtomicInteger();

    public SingleExecutor() {
        this(new LinkedBlockingQueue<>());
    }

    public SingleExecutor(BlockingQueue<Runnable> queue) {
        super(1, 1, 0, TimeUnit.MILLISECONDS, queue, r -> new Thread(r, "single-executor-" + COUNT.incrementAndGet()));
    }

}
