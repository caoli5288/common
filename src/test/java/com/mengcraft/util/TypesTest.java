package com.mengcraft.util;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.concurrent.Callable;

public class TypesTest {

    @Test
    public void testSimple() {
        I1 i1 = Types.asType(new Timestamp(1), I1.class);
        Assert.assertTrue(i1.after(new Timestamp(0)));
    }

    @Test
    public void testMap() throws Exception {
        Types.desc(String.class).map("toString", Callable.class, "call");
        Callable callable = Types.asType("abc", Callable.class);
        Assert.assertEquals("abc", callable.call());
    }

    public interface I1 {

        boolean after(Timestamp timestamp);
    }
}
