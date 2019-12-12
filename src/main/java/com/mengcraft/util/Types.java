package com.mengcraft.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Types {

    public static final Map<Class<?>, MethodsDesc> METHODS_DESCRIPTORS = new HashMap<>();

    public static <T> T asType(Object obj, Class<T> cls) {
        obj = unpack(obj);
        MethodsDesc desc = ofMethodsDesc(obj);
        return cls.cast(Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new Invoker(desc, obj)));
    }

    private static Object unpack(Object obj) {
        if (Proxy.isProxyClass(obj.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(obj);
            validate(handler instanceof Invoker, "cannot apply to unknown proxy instance");
            return ((Invoker) handler).obj;
        }
        return obj;
    }

    public static MethodsDesc ofMethodsDesc(Object obj) {
        obj = unpack(obj);
        return METHODS_DESCRIPTORS.computeIfAbsent(obj.getClass(), cls -> new MethodsDesc(cls));
    }

    private static void validate(boolean b, String msg) {
        if (!b) throw new IllegalStateException(msg);
    }

    private static class Invoker implements InvocationHandler {

        private final MethodsDesc desc;
        private final Object obj;

        Invoker(MethodsDesc desc, Object obj) {
            this.desc = desc;
            this.obj = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Exception {
            Method mapped = desc.mappings.get(method);
            if (mapped == null) {
                mapped = desc.objCls.getMethod(method.getName(), method.getParameterTypes());
                desc.mapStrict(mapped, method);
            }
            return mapped.invoke(obj, params);
        }
    }

    public static class MethodsDesc {

        private final Class<?> objCls;
        private final Map<Method, Method> mappings = new HashMap<>();

        MethodsDesc(Class<?> objCls) {
            this.objCls = objCls;
        }

        public void map(String from, Class<?> cls, String to) throws NoSuchMethodException {
            validate(cls.isInterface(), String.format("cls %s not interface", cls.getName()));
            for (Method method : cls.getMethods()) {
                if (method.getName().equals(to)) {
                    try {
                        Method mapped = objCls.getDeclaredMethod(from, method.getParameterTypes());
                        mapStrict(mapped, method);
                        return;
                    } catch (Exception ignore) {
                    }
                }
            }
            throw new NoSuchMethodException("methods matches failed");
        }

        public void mapStrict(Method from, Method to) {
            if (!from.isAccessible()) {
                from.setAccessible(true);
            }
            mappings.put(to, from);
        }

        public void mapClass(Class<?> cls) {
            validate(cls.isInterface(), String.format("cls %s not interface", cls.getName()));
            for (Method method : cls.getMethods()) {
                if (!mappings.containsKey(method)) {
                    try {
                        Method mapped = objCls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        mapStrict(mapped, method);
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }
}
