package com.mengcraft.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created on 16-12-5.
 */
public class HTTPTest {

    @Test
    public void open() throws Exception {
        HTTPRequest request = new HTTPRequest("http://www.baidu.com");
//        request.addHeader("User-Agent", "Mozilla/5.0");
//        request.addHeader(...)
//        HashMap<String, String> map = new HashMap<>();
//        ...
//        request.setHeader(map);

        HTTP.open(request, (e, response) -> {
//            ...
        });

        HTTPResponse response = HTTP.open(request).get();
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertTrue(!response.getContent().isEmpty());
    }

}