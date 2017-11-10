package com.mengcraft.util.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 16-12-5.
 */
@Data
@EqualsAndHashCode(of = "id")
public class HTTPRequest {

    private final UUID id = UUID.randomUUID();
    private final String address;
    private HTTPMethod method;
    private HTTPHeader header = new HTTPHeader();

    private byte[] content;

    private HTTPRequest(String address, HTTPMethod method) {
        this.address = address;
        this.method = method;
    }

    public HTTPRequest setHeader(Map<String, String> input) {
        HTTP.thr(HTTP.nil(input), "null");
        header = new HTTPHeader(input);
        return this;
    }

    public HTTPRequest setHeader(String key, Object value) {
        HTTP.thr(HTTP.nil(key), "null");
        header.add(key, value.toString());
        return this;
    }

    public HTTPRequest setContentType(HTTPHeader.ContentType type) {
        HTTP.thr(HTTP.nil(type), "null");
        return setHeader(HTTPHeader.CONTENT_TYPE, type);
    }

    public HTTPRequest setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public HTTPRequest setMessageContent(String content) {
        this.content = content.getBytes(Charset.forName("UTF-8"));
        return this;
    }

    public static HTTPRequest build(String address) {
        return build(address, HTTPMethod.GET);
    }

    public static HTTPRequest build(String address, HTTPMethod method) {
        HTTP.thr(HTTP.nil(address) || HTTP.nil(method), "null");
        return new HTTPRequest(address, method);
    }

}
