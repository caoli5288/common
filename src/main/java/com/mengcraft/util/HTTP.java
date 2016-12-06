package com.mengcraft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
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

    private synchronized static void buildPool() {
        if (pool == null) {
            String size = System.getProperty("i5mc.http.pool.size", "-1");
            int i;
            try {
                i = Integer.parseInt(size);
            } catch (NumberFormatException e) {
                i = -1;
            }
            pool = (i < 1
                    ? new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new SynchronousQueue<>())
                    : new ThreadPoolExecutor(0, i, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>()));
        }
    }

    public static class Task implements Callable<HTTPResponse> {

        private final HTTPRequest request;

        private Task(HTTPRequest request) {
            this.request = request;
        }

        @Override
        public HTTPResponse call() throws Exception {
            URL url = new URL(request.getAddress());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            init(connection);

            String content = request.getContent();

            if (!(nil(content) || content.isEmpty())) {
                connection.setDoOutput(true);
            }

            connection.connect();

            if (connection.getDoOutput()) {
                Writer writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(content);
                writer.flush();
                writer.close();
            }

            return read(connection);
        }

        private HTTPResponse read(HttpURLConnection connection) throws IOException {
            int code = connection.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String separator = System.getProperty("line.separator");
            StringBuilder b = new StringBuilder();
            String line;
            while (!nil(line = reader.readLine())) {
                if (b.length() > 0) b.append(separator);
                b.append(line);
            }
            reader.close();

            return new HTTPResponse(request, code, b.toString());
        }

        private void init(HttpURLConnection connection) throws ProtocolException {
            HTTPMethod method = request.getMethod();
            connection.setRequestMethod(method.name());

            Map<String, String> header = request.getHeader();
            if (!nil(header)) {
                header.forEach((key, value) -> connection.addRequestProperty(key, value));
            }
        }

    }

    static void valid(boolean b, String message) {
        if (!b) throw new RuntimeException(message);
    }

    static boolean nil(Object i) {
        return i == null;
    }

    private static void initPool() {
        if (pool == null) {
            buildPool();
        }
    }

    public static Future<HTTPResponse> open(HTTPRequest request) {
        valid(!nil(request), "open " + request);
        initPool();
        return pool.submit(new Task(request));
    }

    public static void open(HTTPRequest request, HTTPCallback callback) {
        valid(!(nil(request) || nil(callback)), "open " + request + " " + callback);
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
