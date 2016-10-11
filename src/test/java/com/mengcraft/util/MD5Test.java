package com.mengcraft.util;

import org.junit.Assert;
import org.junit.Test;

public class MD5Test {
    @Test
    public void random() {
        String out = MD5.DEFAULT.random(8);
        Assert.assertTrue(out.length() == 16);
    }
}
