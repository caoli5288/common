package com.mengcraft.util;

/**
 * Created by on 16-4-29.
 */
public class PingResponse {
    private String message;
    private int max;
    private int online;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }
}
