package com.mengcraft.util.http;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 17-3-18.
 */
public class Latch extends AtomicInteger {

    public void hold(long time) {
        int i = get();
        if (i > 0) {
            synchronized (this) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public void down() {
        int i = decrementAndGet();
        if (i < 1) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
