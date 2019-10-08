package com.mengcraft.util;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Iterator;

@Data
public class Cuboid implements Iterable<Coord> {

    private final Coord least;
    private final Coord most;

    private Cuboid(Coord least, Coord most) {
        this.least = Coord.valueOf(Math.min(least.getX(), most.getX()), Math.min(least.getY(), most.getY()), Math.min(least.getZ(), most.getZ()));
        this.most = Coord.valueOf(Math.max(least.getX(), most.getX()), Math.max(least.getY(), most.getY()), Math.max(least.getZ(), most.getZ()));
    }

    public Collection<Cuboid> getLayers() {
        return ImmutableSet.of(
                valueOf(least, most.withX(least.getX())),
                valueOf(least, most.withY(least.getY())),
                valueOf(least, most.withZ(least.getZ())),
                valueOf(most, least.withX(most.getX())),
                valueOf(most, least.withY(most.getY())),
                valueOf(most, least.withZ(most.getZ()))
        );
    }

    public Collection<Cuboid> getEdges() {
        ImmutableSet.Builder<Cuboid> b = ImmutableSet.builder();
        for (Coord coord : getEndpoints()) {
            if (coord.getX() != least.getX()) {
                b.add(valueOf(coord, Coord.valueOf(least.getX(), coord.getY(), coord.getZ())));
            }
            if (coord.getX() != most.getX()) {
                b.add(valueOf(coord, Coord.valueOf(most.getX(), coord.getY(), coord.getZ())));
            }
            if (coord.getY() != least.getY()) {
                b.add(valueOf(coord, Coord.valueOf(coord.getX(), least.getY(), coord.getZ())));
            }
            if (coord.getY() != most.getY()) {
                b.add(valueOf(coord, Coord.valueOf(coord.getX(), most.getY(), coord.getZ())));
            }
            if (coord.getZ() != least.getZ()) {
                b.add(valueOf(coord, Coord.valueOf(coord.getX(), coord.getY(), least.getZ())));
            }
            if (coord.getZ() != most.getZ()) {
                b.add(valueOf(coord, Coord.valueOf(coord.getX(), coord.getY(), most.getZ())));
            }
        }
        return b.build();
    }

    public Collection<Coord> getEndpoints() {
        return ImmutableSet.of(most,
                most.withX(least.getX()),
                most.withY(least.getY()),
                most.withZ(least.getZ()),
                least,
                least.withX(most.getX()),
                least.withY(most.getY()),
                least.withZ(most.getZ())
        );
    }

    @Override
    public Iterator<Coord> iterator() {
        return ListComprehension.of(least, coord -> {
            if (coord.getX() != most.getX()) {
                return coord.withX(coord.getX() + 1);
            }
            if (coord.getY() != most.getY()) {
                return Coord.valueOf(least.getX(), coord.getY() + 1, coord.getZ());
            }
            if (coord.getZ() != most.getZ()) {
                return Coord.valueOf(least.getX(), least.getY(), coord.getZ() + 1);
            }
            return null;
        });
    }

    public int getVolume() {
        int x = most.getX() - least.getX() + 1;
        int y = most.getY() - least.getY() + 1;
        int z = most.getZ() - least.getZ() + 1;
        return x * y * z;
    }

    public Vector toVector() {
        return new Vector(most.getX() - least.getX(), most.getY() - least.getY(), most.getZ() - least.getZ());
    }

    public static Cuboid valueOf(Coord least, Coord most) {
        return new Cuboid(least, most);
    }
}
