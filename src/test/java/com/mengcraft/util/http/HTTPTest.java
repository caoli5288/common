package com.mengcraft.util.http;

import org.junit.Test;

import java.util.concurrent.Future;

/**
 * Created on 16-12-5.
 */
public class HTTPTest {

    @Test
    public void open() throws Exception {
        HTTPRequest request = HTTPRequest.build("https://www.baidu.com");
        Future<Integer> bai = HTTP.open(request);

        HTTPRequest icon = HTTPRequest.build("http://setting.smartisan.com/app/icon", HTTPRequest.Method.POST)
                .setContentType(HTTPHeader.CONTENT_TYPE_JSON)
                .setMessageContent("[{\"package\":\"com.sina.weibo\"}]");

        HTTP.open(icon, (e, response) -> {
            /*
            logic code here, plz check exception first
             */
            System.out.println(response.getResponse());// 200
            System.out.println(response.getMessage());// any str
        });
        HTTP.flush();// sync wait all task done

        System.out.println(bai.get());
    }

}
