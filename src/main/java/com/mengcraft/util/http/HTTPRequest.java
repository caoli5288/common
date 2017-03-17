package com.mengcraft.util.http;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created on 16-12-5.
 */
public class HTTPRequest {

    private HTTPHeader header = new HTTPHeader();

    private final String address;
    private HTTPMethod method;

    private byte[] content;
    private Callback callback;

    HTTPRequest(String address, HTTPMethod method) {
        this.address = address;
        this.method = method;
    }

    HTTPHeader getHeader() {
        return header;
    }

    String getAddress() {
        return address;
    }

    HTTPMethod getMethod() {
        return method;
    }

    public HTTPRequest setHeader(Map<String, String> input) {
        HTTP.valid(HTTP.nil(input), "null");
        header = new HTTPHeader(input);
        return this;
    }

    public HTTPRequest setHeader(String key, Object value) {
        HTTP.valid(HTTP.nil(key), "null");
        header.add(key, value.toString());
        return this;
    }

    public HTTPRequest setContentType(HTTPHeader.ContentType type) {
        HTTP.valid(HTTP.nil(type), "null");
        return setHeader(HTTPHeader.CONTENT_TYPE, type);
    }

    byte[] getRawContent() {
        return content;
    }

    public HTTPRequest setRawContent(byte[] content) {
        this.content = content;
        return this;
    }

    public HTTPRequest setContent(String content) {
        this.content = content.getBytes(Charset.forName("UTF-8"));
        return this;
    }

    Callback getCallback() {
        return callback;
    }

    void setCallback(Callback callback) {
        this.callback = callback;
    }

    public static HTTPRequest build(String address) {
        return build(address, HTTPMethod.GET);
    }

    public static HTTPRequest build(String address, HTTPMethod method) {
        HTTP.valid(HTTP.nil(address) || HTTP.nil(method), "null");
        return new HTTPRequest(address, method);
    }

}
