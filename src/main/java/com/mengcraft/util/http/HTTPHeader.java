package com.mengcraft.util.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 16-12-6.
 */
public class HTTPHeader extends HashMap<String, String> {

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Entry<String, String> e : entrySet()) {
            if (b.length() > 0) b.append(System.getProperty("line.separator"));
            b.append(e.getKey());
            b.append(": ");
            b.append(e.getValue());
        }
        return b.toString();
    }

    public String add(String key, String value) {
        return HTTP.nil(value) ? remove(key) : put(key, value);
    }

    HTTPHeader() {
        super();
        put(USER_AGENT, "Mozilla/5.0 I5MC");
        put(CONTENT_TYPE, CONTENT_TYPE_TEXT);
    }

    HTTPHeader(Map<String, String> handle) {
        this();
        putAll(handle);
    }

    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String COOKIE = "Cookie";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String USER_AGENT = "User-Agent";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT = "text/html";
}
