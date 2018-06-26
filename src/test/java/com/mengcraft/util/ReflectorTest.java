package com.mengcraft.util;

import org.junit.Test;

import static com.mengcraft.util.Tuple.tuple;
import static org.junit.Assert.assertEquals;

public class ReflectorTest {

    public static class Foo {

        private int i;

        public String bar() {
            return "foobar";
        }

        public static String foo() {
            return "foobar";
        }

        public Foo() {
        }

        public Foo(int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }
    }

    public static class AnoFoo extends Foo {

        public String foobar() {
            return "foobar";
        }
    }

    @Test
    public void invoke() {
        assertEquals(Reflector.invoke(new Foo(), "bar"), new Foo().bar());// method
        assertEquals(Reflector.invoke(Foo.class, "foo"), Foo.foo());// static method
        assertEquals(Reflector.invoke(new AnoFoo(), "bar"), new AnoFoo().bar());// super method
    }

    @Test
    public void getField() {
    }

    @Test
    public void setField() {
    }

    @Test
    public void object() {
        assertEquals(Reflector.object(Foo.class, tuple(int.class, 123)).getI(), 123);
    }

    @Test
    public void getRef() {
    }
}