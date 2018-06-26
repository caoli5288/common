package com.mengcraft.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReflectorTest {

    public static class Foo {

        public String bar() {
            return "foobar";
        }

        public static String foo() {
            return "foobar";
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
    }

    @Test
    public void getRef() {
    }
}