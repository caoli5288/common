package com.mengcraft.util;

/**
 * Created on 16-12-5.
 */
public class HTTPResponse {

    private final int responseCode;
    private final String content;

    HTTPResponse(int responseCode, String content) {
        this.responseCode = responseCode;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public int getResponseCode() {
        return responseCode;
    }

}
