package com.mengcraft.util.http;

import java.util.Map;

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
        HTTP.valid(!HTTP.nil(method), "null");
        this.method = method;
        return this;
    }

    public HTTPRequest setHeader(Map<String, String> input) {
        HTTP.valid(!HTTP.nil(input), "nil");
        header = new HTTPHeader(input);
        return this;
    }

    public HTTPRequest setHeader(String key, Object value) {
        HTTP.valid(!HTTP.nil(key), "null");
        header.add(key, value.toString());
        return this;
    }

    public HTTPRequest setContentType(HTTPHeader.ContentType type) {
        HTTP.valid(!HTTP.nil(type), "nil");
        return setHeader(HTTPHeader.CONTENT_TYPE, type);
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
        HTTP.valid(!(HTTP.nil(address) || HTTP.nil(method)), "null");
        return new HTTPRequest(address, method);
    }

}
