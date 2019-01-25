package com.mengcraft.util.http;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created on 16-12-5.
 */
public final class HTTP {

    private final static AtomicInteger FLUSHING = new AtomicInteger();

    private HTTP() {// utility class
        throw new IllegalStateException("utility class");
    }

    static void thr(boolean b, String message) {
        if (b) throw new IllegalStateException(message);
    }

    static boolean nil(Object i) {
        return i == null;
    }

    public static Future<Integer> open(@NonNull HTTPRequest request) {
        return supplyAsync(() -> {
            val task = new HTTPCall(request, null);
            return task.call();
        });
    }

    public static Future<Integer> open(@NonNull HTTPRequest request, boolean async) {
        if (async) {
            return open(request);
        }
        return CompletableFuture.completedFuture(new HTTPCall(request, null).call());
    }

    public static void open(@NonNull HTTPRequest request, @NonNull Callback callback) {
        runAsync(() -> {
            val task = new HTTPCall(request, callback);
            task.call();
        });
    }

    public static void open(@NonNull HTTPRequest request, @NonNull Callback callback, boolean async) {
        if (async) {
            open(request, callback);
            return;
        }
        new HTTPCall(request, callback).call();
    }

    public interface Callback {

        void call(Exception e, Response response);
    }

    @Data
    @EqualsAndHashCode(of = "id")
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Response {

        private final UUID id = UUID.randomUUID();
        private final String request;
        private final HTTPRequest.Method requestMethod;
        private final int response;
        private final InputStream dataInput;

        private String message;

        public String getMessage() {
            if (!HTTP.nil(message)) return message;

            Reader reader = new InputStreamReader(dataInput);
            StringBuilder b = new StringBuilder();
            char[] buf = new char[8192];
            try {
                for (int i; (i = reader.read(buf)) > -1; ) {
                    b.append(buf, 0, i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return (message = b.toString());
        }
    }

}
