package com.mengcraft.util;

import com.avaje.ebean.EbeanServer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created on 17-6-26.
 */
public abstract class SubPlugin extends PluginBase {

    private JavaPlugin parent;
    private PluginDescriptionFile description;
    private PluginLoader loader;
    private Logger logger;
    private boolean enabled;

    public JavaPlugin getParent() {
        return parent;
    }

    void setParent(JavaPlugin parent) {
        this.parent = parent;
    }

    void setDescription(PluginDescriptionFile description) {
        this.description = description;
    }

    void setLoader(PluginLoader loader) {
        this.loader = loader;
    }

    @Override
    public File getDataFolder() {
        return parent.getDataFolder();
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public FileConfiguration getConfig() {
        return parent.getConfig();
    }

    @Override
    public InputStream getResource(String name) {
        return parent.getResource(name);
    }

    @Override
    public void saveConfig() {
        parent.saveConfig();
    }

    @Override
    public void saveDefaultConfig() {
        parent.saveDefaultConfig();
    }

    @Override
    public void saveResource(String path, boolean force) {
        parent.saveResource(path, force);
    }

    @Override
    public void reloadConfig() {
        parent.reloadConfig();
    }

    @Override
    public PluginLoader getPluginLoader() {
        return loader;
    }

    @Override
    public Server getServer() {
        return parent.getServer();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    protected void setEnabled(boolean b) {
        if (enabled ^ b) {
            enabled = b;
            if (b) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public boolean isNaggable() {
        return parent.isNaggable();
    }

    @Override
    public void setNaggable(boolean b) {
        parent.setNaggable(b);
    }

    @Override
    public EbeanServer getDatabase() {
        return parent.getDatabase();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String name, String id) {
        return parent.getDefaultWorldGenerator(name, id);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender who, Command command, String label, String[] input) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender who, Command command, String label, String[] input) {
        return null;
    }

    public static <T> T getPlugin(Class<T> clazz) {
        return SubPluginLoader.getPlugin(clazz);
    }

    public static JavaPlugin getProvidingPlugin(Class<?> clazz) {
        return JavaPlugin.getProvidingPlugin(clazz);
    }

}
