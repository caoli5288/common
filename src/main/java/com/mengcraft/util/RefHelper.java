package com.mengcraft.util;

import lombok.SneakyThrows;
import lombok.val;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by on 2017/7/3.
 */
public enum RefHelper {

    MAPPING;

    private final Map<Type, Map> f = new HashMap<>();
    private final Map<Type, Map> m = new HashMap<>();


    @SneakyThrows
    static Field b(Class<?> type, String name) {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @SneakyThrows
    static Method b(Class<?> type, String name, Class<?>[] p) {
        try {
            val method = type.getDeclaredMethod(name, p);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            if (!(type == Object.class)) {
                return getMethodRef(type.getSuperclass(), name, p);
            } else {
                throw e;
            }
        }
    }

    @SneakyThrows
    static Field getFieldRef(Class<?> type, String name) {
        Map<String, Field> map = MAPPING.f.computeIfAbsent(type, t -> new HashMap<>());
        return map.computeIfAbsent(name, n -> b(type, name));
    }

    @SneakyThrows
    static Method getMethodRef(Class<?> type, String name, Class<?>[] p) {
        Map<String, Method> map = MAPPING.m.computeIfAbsent(type, t -> new HashMap<>());
        return map.computeIfAbsent(name + "|" + Arrays.toString(p), n -> b(type, name, p));
    }

    @SneakyThrows
    public static <T> T invoke(Object any, String method, Object... input) {
        Class<?>[] p = new Class[input.length];
        for (int i = 0; i < input.length; i++) {
            p[i] = input[i].getClass();
        }
        val invokable = getMethodRef(any.getClass(), method, p);
        return (T) invokable.invoke(any, input);
    }

    @SneakyThrows
    public static <T> T getField(Object any, String field) {
        Field ref = getFieldRef(any.getClass(), field);
        return (T) ref.get(any);
    }
}
