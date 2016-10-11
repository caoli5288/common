package com.mengcraft.util;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created on 16-2-23.
 */
public class Area implements Iterable<Location> {

    private final Location base;
    private final Location offset;

    private Area(Location base, Location offset) {
        this.base = base;
        this.offset = offset;
    }

    public boolean contains(Location loc) {
        return (check(loc.getX() - base.getX(), offset.getX()) &&
                check(loc.getY() - base.getY(), offset.getY()) &&
                check(loc.getZ() - base.getZ(), offset.getZ())
        );
    }

    private boolean check(double x, double y) {
        return (x == y) || (y > 0) ? (x > 0 && x < y) : (x < 0 && x > y);
    }

    public Area getSub(AreaFace face, int begin, int length) {
        Location base = this.base.clone();
        Location offset = this.offset.clone();
        if (face.equals(AreaFace.BASE)) {
            if (offset.getY() > 0) {
                base.setY(base.getY() + begin);
                offset.setY(length);
            } else {
                offset.setY(offset.getY() + begin);
                base.setY(base.getY() + offset.getY() + length);
            }
        } else {
            if (offset.getY() > 0) {
                offset.setY(offset.getY() - begin);
                base.setY(base.getY() + offset.getY() - length);
            } else {
                base.setY(base.getY() - begin);
                offset.setY(-length);
            }
        }
        return new Area(base, offset);
    }

    public List<Location> toLocationSet(Predicate<Location> p) {
        ArrayList<Location> list = new ArrayList<>();
        iterator().forEachRemaining(loc -> {
            if (p.test(loc)) {
                list.add(loc);
            }
        });
        return list;
    }

    public List<Location> toLocationSet() {
        ArrayList<Location> list = new ArrayList<>();
        iterator().forEachRemaining(loc -> {
            list.add(loc);
        });
        return list;

    }

    public AreaIterator getIterator() {
        return new AreaIterator(base, base.clone().add(offset));
    }

    @Override
    public Iterator<Location> iterator() {
        return getIterator();
    }

    public static Area get(Location base, Location other) {
        return new Area(base.clone(), other.clone().subtract(base));
    }

}
