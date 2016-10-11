package com.mengcraft.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created on 16-5-24.
 */
public class CacheTest {

    @Test
    public void get() throws Exception {
        Cache<Long> cache = new Cache<>(System::currentTimeMillis, 1);
        Long l1 = cache.get();
        Assert.assertEquals(l1, cache.get());
        TimeUnit.MILLISECONDS.sleep(2);
        Assert.assertNotEquals(l1, cache.get());
    }

}