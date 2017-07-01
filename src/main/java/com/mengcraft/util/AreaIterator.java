package com.mengcraft.util;

import org.bukkit.Location;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
        if (!start.getWorld().equals(end.getWorld()) || start.equals(end)) {
            throw new IllegalArgumentException();
        }
        this.start = clone(start);
        this.end = clone(end);
        current = clone(start);
        x = end.getX() > start.getX() ? 1 : -1;
        y = end.getY() > start.getY() ? 1 : -1;
        z = end.getZ() > start.getZ() ? 1 : -1;
    }

    private static Location clone(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public Location next() {
        if (current.equals(end)) {
            throw new NoSuchElementException("next");
        } else {
            if (current.getX() == end.getX()) {
                shift();
            } else {
                current.setX(current.getX() + x);
            }
        }
        return current;
    }

    private void shift() {
        current.setX(start.getX());
        if (current.getY() == end.getY()) {
            current.setY(start.getY());
            if (current.getZ() == end.getZ()) {
                current.setZ(start.getZ());
            } else {
                current.setZ(current.getZ() + z);
            }
        } else {
            current.setY(current.getY() + y);
        }
    }

}
