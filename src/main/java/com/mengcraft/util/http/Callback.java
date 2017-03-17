package com.mengcraft.util.http;

/**
 * Created on 17-3-18.
 */
public interface Callback {

    void call(Exception e, Response response);
}
