package com.mengcraft.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 16-12-6.
 */
public class HTTPHeader extends HashMap<String, String> {

    @Override
    public String toString() {
        String separator = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        for (Entry<String, String> e : entrySet()) {
            if (b.length() > 0) b.append(separator);
            b.append(e.getKey());
            b.append(": ");
            b.append(e.getValue());
        }
        return b.toString();
    }

    HTTPHeader() {
        super();
        put(USER_AGENT, "Mozilla/5.0 I5MC");
    }

    HTTPHeader(Map<String, String> m) {
        this();
        if (!HTTP.nil(m)) putAll(m);
    }

    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String COOKIE = "Cookie";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String USER_AGENT = "User-Agent";

}
