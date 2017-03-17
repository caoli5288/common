package com.mengcraft.util.http;

import org.junit.Test;

/**
 * Created on 16-12-5.
 */
public class HTTPTest {

    @Test
    public void open() throws Exception {
        /*
        HTTPRequest request = HTTPRequest.build("https://www.baidu.com");
        HTTP.open(request);

        return a future<int>, not care exception
         */

        HTTPRequest icon = HTTPRequest.build("http://setting.smartisan.com/app/icon", HTTPMethod.POST)
                .setContentType(HTTPHeader.ContentType.JSON)
                .setContent("[{\"package\":\"com.sina.weibo\"}]");

        HTTP.open(icon, (e, response) -> {
            /*
            logic code here, plz check exception first
             */
            System.out.println(response.getResponse());// 200
            System.out.println(response.getMessage());// any str
        });
        HTTP.flush();// sync wait, for test case or app shutdown
    }

}
