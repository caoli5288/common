package com.mengcraft.util;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class HandledCallable<T> extends BukkitRunnable implements Callable<T> {

    private final Consumer<HandledCallable<T>> handle;
    private T value;

    public HandledCallable(Consumer<HandledCallable<T>> handle) {
        this.handle = handle;
    }

    @Override
    public void run() {
        handle.accept(this);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public T call() throws Exception {
        run();
        return getValue();
    }
}
