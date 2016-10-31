package com.mengcraft.util;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 16-10-31.
 */
public class SingleExecutor extends AbstractExecutorService {

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private volatile Thread handler;
    private volatile boolean shutdown;

    @Override
    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            if (!(handler == null)) {
                queue.add(() -> handler = null);
            }
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (!shutdown) {
            shutdown = true;
            if (handler == null) return ImmutableList.of();
            if (queue.isEmpty()) {
                handler = null;
                return ImmutableList.of();
            }
            ImmutableList.Builder<Runnable> b = ImmutableList.builder();
            while (!queue.isEmpty()) {
                b.add(queue.poll());
            }
            queue.add(() -> handler = null);
            return b.build();
        }
        return ImmutableList.of();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && handler == null;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (!shutdown) return false;
        if (handler == null) return true;
        long l = unit.toMillis(timeout) + System.currentTimeMillis();
        while (!(handler == null) && System.currentTimeMillis() < l) {
            TimeUnit.MILLISECONDS.sleep(10);
        }
        return handler == null;
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) throw new NullPointerException();
        if (shutdown) throw new RejectedExecutionException();
        valid();
        queue.offer(command);
    }

    private final void valid() {
        if (handler == null) {
            synchronized (this) {
                if (handler == null) {
                    buildHandler();
                }
            }
        }
    }

    private void buildHandler() {
        handler = new Thread(() -> {
            while (!(handler == null)) {
                try {
                    queue.take().run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, "thread-executor-" + COUNT.incrementAndGet());
        handler.start();
    }

    private static final AtomicInteger COUNT = new AtomicInteger();

}
