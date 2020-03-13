package com.mengcraft.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class Promise<T> {

    private final ListenableFuture<T> future;

    public Promise(ListenableFuture<T> future) {
        this.future = future;
    }

    public ListenableFuture<T> future() {
        return future;
    }

    public Promise<T> sync() {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("exception occurred while sync promise state", e);
        }
        return this;
    }

    public Promise<T> addCallback(ICallback<T> callback) {
        return addCallback(callback, MoreExecutors.directExecutor());
    }

    public Promise<T> addCallback(ICallback<T> callback, Executor executor) {
        Futures.addCallback(future, new FutureCallback<T>() {
            public void onSuccess(@Nullable T result) {
                callback.done(null, result);
            }

            public void onFailure(Throwable err) {
                callback.done(err, null);
            }
        }, executor);
        return this;
    }

    public static <T> Promise<T> create(T obj) {
        return new Promise<>(Futures.immediateFuture(obj));
    }

    public static <T> Promise<T> create(ListenableFuture<T> future) {
        return new Promise<>(future);
    }

    public interface ICallback<T> {

        void done(Throwable err, T obj);
    }

}
