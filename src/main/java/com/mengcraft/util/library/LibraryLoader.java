package com.mengcraft.util.library;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created on 15-12-13.
 */
public class LibraryLoader {

    private static final Method ADD_URL = _INIT_ADD_URL();

    @SneakyThrows
    private static Method _INIT_ADD_URL() {
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        return addURL;
    }

    @SneakyThrows
    public static void load(JavaPlugin plugin, Library library) {
        if (library.present()) {
            plugin.getLogger().info("Library " + library + " present");
        } else {
            if (!library.isLoadable()) {
                init(plugin, library);
            }

            for (Library sub : library.getSublist()) {
                load(plugin, sub);
            }

            val lib = library.getFile();
            ADD_URL.invoke(plugin.getClass().getClassLoader(), lib.toURI().toURL());

            plugin.getLogger().info("Load library " + lib + " done");
        }
    }

    @SneakyThrows
    static void init(JavaPlugin plugin, Library library) {
        plugin.getLogger().info("Loading library " + library);

        val run = CompletableFuture.runAsync(() -> {
            while (!library.isLoadable()) {
                library.init();
            }
        });

        val lib = library.getFile();

        while (!run.isDone()) {
            try {
                run.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ignore) {
            }
            plugin.getLogger().info((lib.length() / 1024) + "kb");
        }

        if (run.isCompletedExceptionally()) throw new IllegalStateException("init");
    }

}
