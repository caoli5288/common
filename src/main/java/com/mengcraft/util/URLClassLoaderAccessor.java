package com.mengcraft.util;

import lombok.SneakyThrows;
import org.apache.commons.lang.math.NumberUtils;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class URLClassLoaderAccessor {

    private static final IAccessor ACCESSOR;

    static {
        float jvm = NumberUtils.toFloat(System.getProperty("java.specification.version", "99.0"));
        if (jvm >= 1.7) {
            ACCESSOR = new Impl2();
        } else {
            ACCESSOR = new Impl();
        }
    }

    public static void addUrl(URLClassLoader cl, URL url) {
        ACCESSOR.addUrl(cl, url);
    }

    public interface IAccessor {

        void addUrl(URLClassLoader cl, URL url);
    }

    private static class Impl implements IAccessor {

        private static final Method HANDLE = setup();

        private static Method setup() {
            try {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                return addURL;
            } catch (Exception e) {
                // noop
            }
            return null;
        }

        @Override
        public void addUrl(URLClassLoader cl, URL url) {
            try {
                HANDLE.invoke(cl, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Impl2 implements IAccessor {

        private final MethodHandle handle = load();

        @SneakyThrows
        private MethodHandle load() {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = (Unsafe) field.get(Unsafe.class);
            field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long offset = unsafe.staticFieldOffset(field);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, offset);
            lookup = lookup.in(URLClassLoader.class);
            return lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
        }

        @Override
        public void addUrl(URLClassLoader cl, URL url) {
            try {
                handle.invokeExact(cl, url);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
