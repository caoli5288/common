package com.mengcraft.util;

import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Iterator;

/**
 * Created on 16-1-3.
 */
public class WorldIterator implements Iterator<Chunk> {

    private final World world;

    private int x;
    private int y;

    private int i; // 转向幅度
    private int j; // 当前进度

    private boolean b; // 是增或减
    private boolean c; // 是叉或歪

    public WorldIterator(World world) {
        Preconditions.checkNotNull(world);
        this.world = world;
    }

    @Override
    public Chunk next() {
        // Cache current x, y position.
        int x = this.x;
        int y = this.y;

        if (j == 0) {
            if (c) {
                c = false;
                j = i;
            } else {
                c = true;
                b = !b;
                j = ++i;
            }
        }
        if (c) {
            if (b) ++this.x;
            else --this.x;
        } else {
            if (b) ++this.y;
            else --this.y;
        }
        --j;

        return world.getChunkAt(x, y);
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("hasNext");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
