package com.mengcraft.util.http;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created on 17-3-1.
 */
public class HTTPTask implements Callable<Integer> {

    private final HTTPRequest request;

    HTTPTask(HTTPRequest request) {
        this.request = request;
    }

    private void init(HttpURLConnection conn) throws ProtocolException {
        val method = request.getMethod();
        conn.setRequestMethod(method.name());

        val header = request.getHeader();
        if (!HTTP.nil(header)) {
            for (val node : header.entrySet()) {
                conn.addRequestProperty(node.getKey(), node.getValue());
            }
        }
    }

    @Override
    public Integer call() throws IOException {
        try {
            val link = new URL(request.getAddress());
            val open = link.openConnection();

            if (!(open instanceof HttpURLConnection)) throw new IOException("protocol");

            val conn = (HttpURLConnection) open;
            init(conn);

            byte[] content = request.getRawContent();

            if (!HTTP.nil(content)) {
                conn.setDoOutput(true);
            }
            conn.connect();

            if (conn.getDoOutput()) {
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(content);
                    out.flush();
                }
            }

            val callback = request.getCallback();
            int result = conn.getResponseCode();

            if (!HTTP.nil(callback)) {
                try (InputStream input = conn.getInputStream()) {
                    callback.call(null, new Response(result, input));
                }
            }

            HTTPTask.LATCH.down();
            return result;
        } catch (IOException e) {
            HTTPTask.LATCH.down();
            throw e;
        }
    }

    static final Latch LATCH = new Latch();

}
