package com.mengcraft.util;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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
        Assert.assertEquals("b", i1.b("b"));
        Assert.assertEquals(111, i1.c());
        Assert.assertEquals(111, i1.c(111));
        Assert.assertEquals("ab", i1.d("a", "b"));
        Assert.assertEquals(3, i1.sum(0, 1, 2));
        Assert.assertEquals(3, i1.sum(1, 2));
        i1.a("hello");
        i1.a("hello", "world");
        i1.a(111);
    }

    @Test
    public void testAsLambda() {
        Method method = MethodUtils.getMatchingMethod(Bob.class, "a");
        Types.lambdaPrivileged(method, Consumer.class).accept(new Bob());
    }

    @Test
    public void testAsLambdaBound() {
        Method method = MethodUtils.getMatchingMethod(Bob.class, "a");
        Method methodB = MethodUtils.getMatchingMethod(Bob.class, "b");
        Types.lambdaPrivileged(new Bob(), method, Runnable.class).run();
        Assert.assertEquals("b", Types.lambdaPrivileged(new Bob(), methodB, Supplier.class).get());
        Types.lambdaPrivileged(new Bob(), method, Runnable.class).run();
        Types.lambdaPrivileged(new Bob(), MethodUtils.getMatchingMethod(Bob.class, "a", int.class), IntConsumer.class).accept(111);
        Assert.assertEquals(111, Types.lambdaPrivileged(new Bob(), MethodUtils.getMatchingMethod(Bob.class, "c", int.class), IntFunction.class).apply(111));
    }

    public interface I1 {

        boolean after(Timestamp timestamp);

        void a();

        void a(int i);

        void a(String a);

        void a(String a, String b);

        int fun(int i);

        String b();

        String b(String b);

        int c();

        int c(int i);

        String d(String a, String b);

        int sum(int i, int i1, int i2);

        int sum(int i, int i1);
    }
}
