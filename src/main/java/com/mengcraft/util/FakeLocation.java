package com.mengcraft.util;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

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

    public Location toLocation() {
        return toLocation(this);
    }

    public void save(FileConfiguration i, String path) {
        save(this, i, path);
    }

    public String encode() {
        return world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
    }

    @Override
    public String toString() {
        return "FakeLocation(" + encode() + ')';
    }

    public static FakeLocation decode(String data) {
        String[] split = data.split(",");
        Preconditions.checkArgument(split.length == 6);
        FakeLocation location = new FakeLocation();
        location.world = split[0];
        location.x = Double.parseDouble(split[1]);
        location.y = Double.parseDouble(split[2]);
        location.z = Double.parseDouble(split[3]);
        location.yaw = Float.parseFloat(split[4]);
        location.pitch = Float.parseFloat(split[5]);
        return location;
    }

    public static void save(FakeLocation l, FileConfiguration i, String path) {
        i.set(path + ".world", l.world);
        i.set(path + ".x", l.x);
        i.set(path + ".y", l.y);
        i.set(path + ".z", l.z);
        i.set(path + ".yaw", l.yaw);
        i.set(path + ".pitch", l.pitch);
    }

    public static FakeLocation load(FileConfiguration i, String path) {
        return new FakeLocation(
                i.getString(path + ".world"),
                i.getDouble(path + ".x"),
                i.getDouble(path + ".y"),
                i.getDouble(path + ".z"),
                toFloat(i.getDouble(path + ".yaw")),
                toFloat(i.getDouble(path + ".pitch"))
        );
    }

    public static Location toLocation(FakeLocation i) {
        return new Location(Bukkit.getWorld(i.world), i.x, i.y, i.z, i.yaw, i.pitch);
    }

    public static FakeLocation of(Location i) {
        return new FakeLocation(i.getWorld().getName(), i.getX(), i.getY(), i.getZ(), i.getYaw(), i.getPitch());
    }
}
