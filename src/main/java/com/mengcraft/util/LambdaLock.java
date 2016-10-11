package com.mengcraft.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Created on 16-2-25.
 */
public class LambdaLock {

    private final Lock readLock;
    private final Lock writeLock;

    public LambdaLock() {
        this(new ReentrantReadWriteLock());
    }

    public LambdaLock(ReadWriteLock readLock) {
        this.readLock = readLock.readLock();
        this.writeLock = readLock.writeLock();
    }

    public <T> T read(Supplier<T> block) {
        readLock.lock();
        try {
            return block.get();
        } finally {
            readLock.unlock();
        }
    }

    public void read(Runnable block) {
        readLock.lock();
        try {
            block.run();
        } finally {
            readLock.unlock();
        }
    }

    public <T> T write(Supplier<T> block) {
        writeLock.lock();
        try {
            return block.get();
        } finally {
            writeLock.unlock();
        }
    }

    public void write(Runnable block) {
        writeLock.lock();
        try {
            block.run();
        } finally {
            writeLock.unlock();
        }
    }

}
