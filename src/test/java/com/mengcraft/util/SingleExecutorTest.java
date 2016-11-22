package com.mengcraft.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 16-10-31.
 */
public class SingleExecutorTest {

    @Test
    public void execute() throws Exception {
        ExecutorService pool = new SingleExecutor();
        AtomicBoolean b = new AtomicBoolean();
        pool.execute(() -> b.set(true));
        pool.shutdown();
        Assert.assertTrue(pool.isShutdown());
        pool.awaitTermination(100, TimeUnit.SECONDS);
        Assert.assertTrue(b.get());
        Assert.assertTrue(pool.isTerminated());
    }

}