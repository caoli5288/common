package com.mengcraft.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created on 17-1-16.
 */
@RequiredArgsConstructor
@Log
public class Timing {

    private static class MXTiming extends HashMap<String, Timing> {

        private Timing look(String key) {
            return computeIfAbsent(key, k -> new Timing(key));
        }
    }

    private void add(long t) {
        total = total + t;
        i++;
        max = Math.max(max, t);
        latest = t;
    }

    public static Timing timing(String key, Runnable run) {
        long time = System.nanoTime();
        try {
            run.run();
        } catch (Exception e) {
            log.log(Level.SEVERE, "timing", e);
        }
        time = System.nanoTime() - time;
        Timing timing = TIMING.look(key);
        timing.add(time);
        return timing;
    }

    @Override
    public String toString() {
        return (key + " " +
                "(" +
                "latest:" + latest +
                ",total:" + total +
                ",avg:" + (total / i) +
                ",max:" + max +
                ",count:" + i +
                ")");
    }

    private final String key;
    private long total;
    private long max;
    private long i;
    private long latest;

    private final static MXTiming TIMING = new MXTiming();

}
