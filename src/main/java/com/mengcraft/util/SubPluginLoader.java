package com.mengcraft.util;

import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created on 17-6-26.
 */
public class SubPluginLoader implements PluginLoader {

    private enum Fun {

        INSTANCE;

        private final List<SubPlugin> loaded;
        private final Map<String, Plugin> map;
        private final List<Plugin> all;

        @SneakyThrows
        Fun() {
            val manager = Bukkit.getPluginManager();
            Field field = SimplePluginManager.class.getDeclaredField("plugins");
            field.setAccessible(true);
            all = (List<Plugin>) field.get(manager);
            field = SimplePluginManager.class.getDeclaredField("lookupNames");
            field.setAccessible(true);
            map = (Map<String, Plugin>) field.get(manager);
            loaded = new ArrayList<>();
        }

        private static void unload(SubPlugin sub) {
            val fun = INSTANCE;
            if (fun.loaded.remove(sub)) {
                fun.all.remove(sub);
                fun.map.remove(sub.getName(), sub);
            }
        }

        private static void load(SubPlugin sub) {
            val fun = INSTANCE;
            fun.loaded.add(sub);
            fun.all.add(sub);
            fun.map.put(sub.getName(), sub);
        }
    }

    private final JavaPlugin plugin;

    private SubPluginLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        throw new UnsupportedOperationException("Not supported yet");
    }

    public Plugin loadPlugin(SubPlugin sub, PluginDescriptionFile description) throws InvalidPluginException {
        valid(sub, description);

        sub.setLoader(this);
        sub.setDescription(description);
        sub.setParent(plugin);

        val logger = plugin.getLogger();
        val log = "[Sub|" + description.getName() + "] ";

        sub.setLogger(new Logger(sub.getClass().getCanonicalName(), null) {
            public void log(LogRecord record) {
                record.setMessage(log + record.getMessage());
                logger.log(record);
            }
        });
        Fun.load(sub);
        Bukkit.getPluginManager().enablePlugin(sub);

        return sub;
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        valid(plugin);
        if (!plugin.isEnabled()) {
            plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());
            SubPlugin sub = (SubPlugin) plugin;
            try {
                sub.setEnabled(true);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
            }
            Bukkit.getPluginManager().callEvent(new PluginEnableEvent(plugin));
        }
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        valid(plugin);
        if (plugin.isEnabled()) {
            plugin.getLogger().info("Disabling " + plugin.getDescription().getFullName());
            SubPlugin sub = (SubPlugin) plugin;
            try {
                sub.setEnabled(false);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
            }
            Fun.unload(sub);
            Bukkit.getPluginManager().callEvent(new PluginDisableEvent(plugin));
        }
    }

    public void unloadAll() {
        unloadAll(plugin);
    }

    public static void unloadAll(JavaPlugin parent) {
        for (SubPlugin plugin : Fun.INSTANCE.loaded) {
            if (plugin.getParent() == parent) {
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }

    public static void valid(Plugin plugin) {
        if (!(plugin instanceof SubPlugin)) {
            throw new IllegalArgumentException("Plugin is not associated with this loader");
        }
    }

    public static void valid(SubPlugin sub, PluginDescriptionFile description) throws InvalidPluginException {
        if (Fun.INSTANCE.loaded.contains(sub)) {
            throw new InvalidPluginException("Already loaded by sub-plugin loader");
        } else if (Fun.INSTANCE.map.containsKey(description.getName())) {
            throw new InvalidPluginException("Already loaded by origin");
        }
        for (String depend : description.getDepend()) {
            val load = Fun.INSTANCE.map.get(depend);
            if (load == null || !load.isEnabled()) {
                throw new UnknownDependencyException("Depend plugin " + depend + " not found or enabled");
            }
        }
    }

    public static <T> T getPlugin(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        if (!Plugin.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz + " does not extend " + Plugin.class);
        }
        Plugin plugin = null;
        val itr = Fun.INSTANCE.all.iterator();
        while (plugin == null && itr.hasNext()) {
            val next = itr.next();
            if (clazz == next.getClass()) {
                plugin = next;
            }
        }
        if (plugin == null) {
            throw new IllegalStateException("Cannot get plugin for " + clazz + " from a static initializer");
        }
        return clazz.cast(plugin);
    }

    public static SubPluginLoader of(JavaPlugin parent) {
        if (parent == null) throw new NullPointerException("parent");
        return new SubPluginLoader(parent);
    }

}
