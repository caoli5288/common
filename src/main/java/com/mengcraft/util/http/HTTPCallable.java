package com.mengcraft.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created on 17-3-1.
 */
public class HTTPCallable implements Callable<HTTPResponse> {

    private final HTTPRequest request;

    HTTPCallable(HTTPRequest request) {
        this.request = request;
    }

    @Override
    public HTTPResponse call() throws IOException {
        URL url = new URL(request.getAddress());
        URLConnection open = url.openConnection();

        if (!(open instanceof HttpURLConnection)) throw new IOException("protocol");

        HttpURLConnection conn = (HttpURLConnection) open;
        init(conn);

        String content = request.getContent();

        if (!(HTTP.nil(content) || content.isEmpty())) conn.setDoOutput(true);

        conn.connect();

        if (conn.getDoOutput()) {
            Writer writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(content);
            writer.flush();
            writer.close();
        }

        return read(conn);
    }

    private HTTPResponse read(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder b = new StringBuilder();
        String line;
        while (!HTTP.nil(line = reader.readLine())) {
            if (b.length() > 0) b.append(HTTP.SEPARATOR);
            b.append(line);
        }
        reader.close();

        return new HTTPResponse(request, code, b.toString());
    }

    private void init(HttpURLConnection connection) throws ProtocolException {
        HTTPMethod method = request.getMethod();
        connection.setRequestMethod(method.name());

        Map<String, String> header = request.getHeader();
        if (!HTTP.nil(header)) {
            header.forEach((key, value) -> connection.addRequestProperty(key, value));
        }
    }

}
