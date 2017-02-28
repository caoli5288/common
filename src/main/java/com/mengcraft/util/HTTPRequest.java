package com.mengcraft.util;

import java.util.Map;

import static com.mengcraft.util.HTTP.nil;

/**
 * Created on 16-12-5.
 */
public class HTTPRequest {

    private HTTPHeader header = new HTTPHeader();
    private String content;

    private final String address;
    private HTTPMethod method;

    public HTTPHeader getHeader() {
        return header;
    }

    public String getAddress() {
        return address;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public HTTPRequest setMethod(HTTPMethod method) {
        HTTP.valid(!nil(method), "null");
        this.method = method;
        return this;
    }

    public HTTPRequest setHeader(Map<String, String> input) {
        HTTP.valid(!nil(input), "nil");
        header = new HTTPHeader(input);
        return this;
    }

    public HTTPRequest setHeader(String key, String value) {
        HTTP.valid(!nil(key), "null");
        header.put(key, value);
        return this;
    }

    public String getContent() {
        return content;
    }

    public HTTPRequest setContent(String content) {
        this.content = content;
        return this;
    }

    private HTTPRequest(String address, HTTPMethod method) {
        this.address = address;
        this.method = method;
    }

    public static HTTPRequest build(String address) {
        return build(address, HTTPMethod.GET);
    }

    public static HTTPRequest build(String address, HTTPMethod method) {
        HTTP.valid(!(nil(address) || nil(method)), "null");
        return new HTTPRequest(address, method);
    }

}
