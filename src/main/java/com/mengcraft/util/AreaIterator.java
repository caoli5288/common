package com.mengcraft.util;

import org.bukkit.Location;

import java.util.Iterator;

/**
 * Created on 16-2-23.
 */
public class AreaIterator implements Iterator<Location> {

    private Location current;
    private Location end;
    private Location start;

    private int x;
    private int y;
    private int z;

    public AreaIterator(Location start, Location end) {
        this.current = convert(start);
        this.end = convert(end);
        this.start = convert(start);
        this.x = end.getX() > start.getX() ? 1 : -1;
        this.y = end.getY() > start.getY() ? 1 : -1;
        this.z = end.getZ() > start.getZ() ? 1 : -1;
    }

    private static Location convert(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public Location next() {
        Location current = this.current.clone();
        if (current.equals(end)) {
            this.current = null;
        } else if (this.current.getX() == end.getX()) {
            shift();
        } else {
            this.current.setX(this.current.getX() + x);
        }
        return current;
    }

    private void shift() {
        this.current.setX(start.getX());
        if (this.current.getY() == end.getY()) {
            this.current.setY(start.getY());
            if (this.current.getZ() == end.getZ()) {
                this.current.setZ(start.getZ());
            } else {
                this.current.setZ(this.current.getZ() + z);
            }
        } else {
            this.current.setY(this.current.getY() + y);
        }
    }

}
