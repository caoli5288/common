package com.mengcraft.util.http;

import com.mengcraft.util.Latch;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Created on 17-3-1.
 */
@EqualsAndHashCode(of = "request")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class HTTPTask implements Callable<Integer> {

    private static final Pattern PROTOCOL = Pattern.compile("^http(s)?$");
    private static final int TIMEOUT = 60000;

    private final HTTPRequest request;
    private final Callback callback;

    private void valid(String protocol) throws IOException {
        if (!PROTOCOL.matcher(protocol).matches()) {
            throw new IOException(protocol);
        }
    }

    @SneakyThrows
    private int conn() {
        val link = new URL(request.getAddress());
        valid(link.getProtocol());

        val conn = (HttpURLConnection) link.openConnection();
        val method = request.getMethod();
        conn.setRequestMethod(method.name());

        val header = request.getHeader();
        if (!HTTP.nil(header)) {
            for (val node : header.entrySet()) {
                conn.addRequestProperty(node.getKey(), node.getValue());
            }
        }

        if (!HTTP.nil(request.getContent())) {
            conn.setDoOutput(true);
        }

        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.connect();

        if (conn.getDoOutput()) {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(request.getContent());
                out.flush();
            }
        }

        int result = conn.getResponseCode();// May exception here
        if (!HTTP.nil(callback)) {
            try (val dataInput = conn.getInputStream()) {
                val response = new Response(request.getAddress(),
                        request.getMethod(),
                        result,
                        dataInput
                );
                callback.call(null, response);
            }
        }

        conn.disconnect();

        return result;
    }

    @Override
    public Integer call() {
        int result = -1;
        try {
            result = conn();
        } catch (Exception e) {
            if (!HTTP.nil(callback)) callback.call(e, null);
        }
        return result;
    }

    static final Latch LATCH = new Latch();

}
