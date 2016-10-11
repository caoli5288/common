package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Iterator;

import static org.bukkit.util.NumberConversions.toFloat;

/**
 * Created on 16-7-18.
 */
public class FakeLocation {
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public FakeLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public FakeLocation() {
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Location toLocation(Server server) {
        return toLocation(this, server);
    }

    public void save(ConfigurationSection j, String path) {
        save(this, j, path);
    }

    public String encode() {
        return world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
    }

    public static FakeLocation decode(String data) {
        String[] split = data.split(",");
        Preconditions.checkArgument(split.length == 6);
        Iterator<String> it = ImmutableList.of(split).iterator();
        FakeLocation location = new FakeLocation();
        location.world = it.next();
        location.x = Double.parseDouble(it.next());
        location.y = Double.parseDouble(it.next());
        location.z = Double.parseDouble(it.next());
        location.yaw = Float.parseFloat(it.next());
        location.pitch = Float.parseFloat(it.next());
        return location;
    }

    public static void save(FakeLocation i, ConfigurationSection j, String path) {
        j.set(path + ".world", i.world);
        j.set(path + ".x", i.x);
        j.set(path + ".y", i.y);
        j.set(path + ".z", i.z);
        j.set(path + ".yaw", i.yaw);
        j.set(path + ".pitch", i.pitch);
    }

    public static FakeLocation load(ConfigurationSection j, String path) {
        return new FakeLocation(
                j.getString(path + ".world"),
                j.getDouble(path + ".x"),
                j.getDouble(path + ".y"),
                j.getDouble(path + ".z"),
                toFloat(j.getDouble(path + ".yaw")),
                toFloat(j.getDouble(path + ".pitch"))
        );
    }

    public static Location toLocation(FakeLocation i, Server server) {
        return new Location(server.getWorld(i.world), i.x, i.y, i.z, i.yaw, i.pitch);
    }

    public static FakeLocation of(Location i) {
        return new FakeLocation(i.getWorld().getName(), i.getX(), i.getY(), i.getZ(), i.getYaw(), i.getPitch());
    }
}
