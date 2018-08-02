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

import static com.mengcraft.util.http.HTTP.nil;

/**
 * Created on 17-3-1.
 */
@EqualsAndHashCode(of = "request")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class HTTPCall implements Callable<Integer> {

    private static final Pattern PROTOCOL = Pattern.compile("^http(s)?$");
    private static final int TIMEOUT = 60000;
    private final HTTPRequest request;
    private final HTTP.Callback callback;
    private HttpURLConnection conn;

    private void valid(String protocol) throws IOException {
        if (!PROTOCOL.matcher(protocol).matches()) {
            throw new IOException(protocol);
        }
    }

    @SneakyThrows
    private int conn() {
        val link = new URL(request.getAddress());
        valid(link.getProtocol());

        conn = (HttpURLConnection) link.openConnection();
        val method = request.getMethod();
        conn.setRequestMethod(method.name());

        val header = request.getHeader();
        if (!nil(header)) {
            for (val node : header.entrySet()) {
                conn.addRequestProperty(node.getKey(), node.getValue());
            }
        }

        if (!nil(request.getContent())) {
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
        if (!nil(callback)) {
            try (val inputStr = conn.getInputStream()) {
                val response = new HTTP.Response(request.getAddress(),
                        request.getMethod(),
                        result,
                        inputStr
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
            if (!nil(callback)) callback.call(e, null);
            if (!nil(conn)) {
                conn.disconnect();
            }
        }
        return result;
    }

}
