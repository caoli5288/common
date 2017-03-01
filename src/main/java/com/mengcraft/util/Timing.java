package com.mengcraft.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 17-1-16.
 */
@RequiredArgsConstructor
@Log
public class Timing {

    private static class MXTiming extends HashMap<String, Timing> {

        private final static MXTiming TIMING = new MXTiming();// Lazy load by sub-hold

        private Timing look(String key) {
            return computeIfAbsent(key, k -> new Timing(k));
        }
    }

    private final String key;
    private long total;
    private long max;
    private long i;
    private long latest;

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
        Timing timing = MXTiming.TIMING.look(key);
        timing.add(time);
        return timing;
    }

    public static Timing reset(String key) {
        return MXTiming.TIMING.remove(key);
    }

    public static Timing get(String key) {
        return MXTiming.TIMING.get(key);
    }

    public static void visit(BiConsumer<String, Timing> con) {
        MXTiming.TIMING.forEach(con);
    }

    /*
     * Timing.timing("1", Any::func).log(logger);
     */
    public void log(Logger log) {
        log.log(Level.INFO, "Timing " + this);
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

}
