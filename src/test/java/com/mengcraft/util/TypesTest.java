package com.mengcraft.util;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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

    @Test
    public void testLambda() {
        I1 i1 = Types.asType(new Bob(), I1.class);
        i1.a();
        Assert.assertEquals("b", i1.b());
        Assert.assertEquals(111, i1.c());
        Assert.assertEquals("ab", i1.d("a", "b"));
        Assert.assertEquals(3, i1.sum(0, 1, 2));
    }

    @Test
    public void testAsLambda() {
        Method method = MethodUtils.getMatchingMethod(Bob.class, "a");
        Types.asLambda(method, Consumer.class).accept(new Bob());
    }

    public interface I1 {

        boolean after(Timestamp timestamp);

        void a();

        String b();

        int c();

        String d(String a, String b);

        int sum(int i, int i1, int i2);
    }
}
