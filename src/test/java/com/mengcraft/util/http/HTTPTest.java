package com.mengcraft.util.http;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 16-12-5.
 */
public class HTTPTest {

    @Test
    public void open() throws Exception {
        HTTPRequest icon = HTTPRequest.build("http://setting.smartisan.com/app/icon", HTTPMethod.POST);
        icon.setContentType(HTTPHeader.ContentType.JSON);
        icon.setContent("[{\"package\":\"com.sina.weibo\"}]");
        HTTP.open(icon, (e, response) -> {
            Assert.assertNull(e);
            Assert.assertNotNull(response);
        });
        HTTP.flush();// 因为单元测试执行时间较短所以等待回调

        HTTPRequest request = HTTPRequest.build("https://www.baidu.com");
        HTTPResponse du = HTTP.open(request).get();
        Assert.assertTrue(du.getResponse() == 200);
        Assert.assertNotNull(du.getContent());
    }

}
