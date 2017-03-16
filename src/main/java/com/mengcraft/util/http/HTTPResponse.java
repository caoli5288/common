package com.mengcraft.util.http;

/**
 * Created on 16-12-5.
 */
public class HTTPResponse {

    private final HTTPRequest request;
    private final int response;
    private final String content;

    HTTPResponse(HTTPRequest request, int response, String content) {
        this.request = request;
        this.response = response;
        this.content = content;
    }

    public HTTPRequest getRequest() {
        return request;
    }

    public String getContent() {
        return content;
    }

    public int getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return Integer.toString(response) + " " + content;
    }

}
