package com.mengcraft.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Created on 16-12-7.
 */
public class RollingPoolTest {

    @Test
    public void roll() throws Exception {
        RollingPool<String> pool = new RollingPool<>(new Random());
        pool.put("1", 1);
        pool.put("2", 1);
        Assert.assertTrue(pool.roll().matches("[12]"));
    }

}