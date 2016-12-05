package com.mengcraft.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 16-12-5.
 */
public class HTTPRequest {

    private Map<String, String> header;
    private String content;

    private final String address;
    private HTTPMethod method;

    public HTTPRequest(String address) {
        this(address, HTTPMethod.GET);
    }

    public HTTPRequest(String address, HTTPMethod method) {
        this.address = address;
        this.method = method;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getAddress() {
        return address;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public HTTPRequest setMethod(HTTPMethod method) {
        if (method == null) throw new NullPointerException("method");
        this.method = method;
        return this;
    }

    public HTTPRequest setHeader(Map<String, String> header) {
        this.header = header;
        return this;
    }

    public HTTPRequest addHeader(String key, String value) {
        if (header == null) header = new HashMap<>();
        header.put(key, value);
        return this;
    }

    public String getContent() {
        return content;
    }

    public HTTPRequest setContent(String content) {
        if (content == null) throw new NullPointerException("content");
        this.content = content;
        return this;
    }

}
