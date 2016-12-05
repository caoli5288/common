package com.mengcraft.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 16-12-5.
 */
public class HTTP {

    private static ExecutorService pool;

    private static void initPool() {
        if (pool == null) {
            synchronized (HTTP.class) {
                if (pool == null) {
                    buildPool();
                }
            }
        }
    }

    private static void buildPool() {
        String size = System.getProperty("i5mc.http.pool.size", "-1");
        int i;
        try {
            i = Integer.parseInt(size);
        } catch (NumberFormatException e) {
            i = -1;
        }
        pool = (i < 1 ? new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new SynchronousQueue<>())
                : new ThreadPoolExecutor(0, i, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>()));
    }

    public static class Task implements Callable<HTTPResponse> {

        private final HTTPRequest request;

        private Task(HTTPRequest request) {
            this.request = request;
        }

        @Override
        public HTTPResponse call() throws Exception {
            URL url = new URL(request.getAddress());
            HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());

            HTTPMethod method = request.getMethod();
            connection.setRequestMethod(method.name());

            Map<String, String> header = request.getHeader();
            if (!nil(header)) {
                header.forEach((key, value) -> connection.addRequestProperty(key, value));
            }

            String content = request.getContent();
            if (!(nil(content) || content.isEmpty())) {
                connection.setDoOutput(true);
            }

            connection.connect();

            if (connection.getDoOutput()) {
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(content);
                writer.flush();
                writer.close();
            }

            int code = connection.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            List<String> response = new ArrayList<>();
            String line;
            while (!nil(line = reader.readLine())) {
                response.add(line);
            }
            reader.close();

            return new HTTPResponse(code, SimpleList.join(response, System.getProperty("line.separator")));
        }

    }

    private static void valid(boolean b, String message) {
        if (!b) throw new RuntimeException(message);
    }

    private static boolean nil(Object i) {
        return i == null;
    }

    public static Future<HTTPResponse> open(HTTPRequest request) {
        valid(!nil(request), "null");
        initPool();
        return pool.submit(new Task(request));
    }

    public static void open(HTTPRequest request, HTTPCallback callback) {
        valid(!(nil(request) || nil(callback)), "null");
        initPool();
        pool.execute(() -> {
            Task task = new Task(request);
            try {
                callback.done(null, task.call());
            } catch (Exception e) {
                callback.done(e, null);
            }
        });
    }

}
