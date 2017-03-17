package com.mengcraft.util.http;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Created on 17-3-1.
 */
public class HTTPTask implements Callable<Integer> {

    private static final Pattern PROTOCOL = Pattern.compile("^http(s)?$");
    private static final int TIMEOUT = 60000;

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

    private void valid(String protocol) throws IOException {
        if (PROTOCOL.matcher(protocol).matches()) return;
        throw new IOException(protocol);
    }

    private int conn() throws IOException {
        val link = new URL(request.getAddress());
        valid(link.getProtocol());

        val conn = (HttpURLConnection) link.openConnection();
        init(conn);

        if (!HTTP.nil(request.getRawContent())) {
            conn.setDoOutput(true);
        }

        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.connect();

        if (conn.getDoOutput()) {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(request.getRawContent());
                out.flush();
            }
        }

        int response = conn.getResponseCode();

        val callback = request.getCallback();
        if (!HTTP.nil(callback)) {
            try (InputStream input = conn.getInputStream()) {
                callback.call(null, new Response(response, input));
            }
        }

        conn.disconnect();
        return response;
    }

    @Override
    public Integer call() throws IOException {
        try {
            return conn();
        } finally {
            HTTPTask.LATCH.down();
        }
    }

    static final Latch LATCH = new Latch();

}
