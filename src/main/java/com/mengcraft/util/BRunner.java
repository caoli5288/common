package com.mengcraft.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 17-1-18.
 */
public class BRunner {

    public static class Runner extends BukkitRunnable {

        private final IRunner r;

        Runner(IRunner r) {
            this.r = r;
        }

        @Override
        public void run() {
            r.run(this);
        }
    }

    public interface IRunner {

        void run(Runner r);
    }

    public static int run(Plugin plugin, int i, int repeat, IRunner r) {
        Runner out = new Runner(r);
        if (i > 0) {
            if (repeat > 0) {
                out.runTaskLater(plugin, i);
            } else {
                out.runTaskTimer(plugin, i, repeat);
            }
        } else {
            out.runTask(plugin);
        }
        return out.getTaskId();
    }

}
