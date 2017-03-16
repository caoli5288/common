package com.mengcraft.util.http;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 16-12-5.
 */
public class HTTPTest {

    @Test
    public void open() throws Exception {
//        HTTPRequest request = HTTPRequest.build("https://www.baidu.com");
//        request.setHeader(HTTPHeader.USER_AGENT, "Mozilla/5.0 I5MC");
//        HashMap<String, String> map = new HashMap<>();
//        ...
//        request.setHeader(map);

//        HTTP.open(request, (e, response) -> {
//            if (e == null) {
//                Assert.assertEquals(response.getResponse(), 200); // 单元测试里面可能会错过回调
//            }
//        });
        HTTPRequest request = HTTPRequest.build("http://setting.smartisan.com/app/icon", HTTPMethod.POST);
        request.setHeader(HTTPHeader.CONTENT_TYPE, HTTPHeader.ContentType.JSON);
        request.setContent("[{\"package\":\"com.sina.weibo\"}]");
        HTTPResponse response = HTTP.open(request).get();
//        System.out.println(response.getContent());
        Assert.assertEquals(response.getResponse(), 200);
        Assert.assertTrue(!response.getContent().isEmpty());
    }

}
