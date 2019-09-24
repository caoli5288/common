package com.mengcraft.util;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;

@Data
public class Coord {

    private final int x;
    private final int y;
    private final int z;

    private Coord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord withX(int x) {
        return new Coord(x, y, z);
    }

    public Coord withY(int y) {
        return new Coord(x, y, z);
    }

    public Coord withZ(int z) {
        return new Coord(x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public static Coord valueOf(Location location) {
        return new Coord(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Coord valueOf(int x, int y, int z) {
        return new Coord(x, y, z);
    }
}
