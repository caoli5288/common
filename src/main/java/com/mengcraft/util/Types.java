package com.mengcraft.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Types {

    private static final Map<Class<?>, Methods> METHODS_DESCRIPTORS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T asType(Object obj, Class<T> cls) {
        obj = unpack(obj);
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new Invoker(getMethods(obj), obj));
    }

    private static Object unpack(Object obj) {
        if (Proxy.isProxyClass(obj.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(obj);
            checkState(handler instanceof Invoker, "cannot apply to unknown proxy instance");
            return ((Invoker) handler).obj;
        }
        return obj;
    }

    public static Methods getMethods(Object obj) {
        obj = unpack(obj);
        Methods desc = METHODS_DESCRIPTORS.get(obj.getClass());
        if (desc == null) {
            desc = new Methods(obj.getClass());
            METHODS_DESCRIPTORS.put(desc.objCls, desc);
        }
        return desc;
    }

    private static Method lookup(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        try {
            return cls.getDeclaredMethod(methodName, paramsTypes);
        } catch (Exception e) {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == paramsTypes.length && matches(method.getParameterTypes(), paramsTypes)) {
                    return method;
                }
            }
            return null;
        }
    }

    private static boolean matches(Class<?>[] parameterTypes, Class<?>[] paramsTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = paramsTypes[i];
            if (paramType != Object.class && !parameterTypes[i].isAssignableFrom(paramType)) {// guess (paramType == Object) always generic parameter
                return false;
            }
        }
        return true;
    }

    private static class Invoker implements InvocationHandler {

        private final Methods desc;
        private final Object obj;

        Invoker(Methods desc, Object obj) {
            this.desc = desc;
            this.obj = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Exception {
            Method res = desc.mappings.get(method);
            if (res == null) {
                res = lookup(desc.objCls, method.getName(), method.getParameterTypes());
                checkNotNull(res, String.format("%s: [%s] not mapped", desc.objCls, method));
                desc.mapStrict(res, method);
            }
            return res.invoke(obj, params);
        }
    }

    public static class Methods {

        private final Class<?> objCls;
        private final Map<Method, Method> mappings = new HashMap<>();

        Methods(Class<?> objCls) {
            this.objCls = objCls;
        }

        public Methods map(String from, Class<?> cls, String to) {
            checkState(cls.isInterface(), String.format("class %s is not an interface", cls.getName()));
            for (Method method : cls.getMethods()) {
                if (method.getName().equals(to)) {
                    Method res = lookup(objCls, from, method.getParameterTypes());
                    if (res != null) {
                        mapStrict(res, method);
                    }
                }
            }
            return this;
        }

        public Methods mapStrict(Method from, Method to) {
            if (!from.isAccessible()) {
                from.setAccessible(true);
            }
            mappings.put(to, from);
            return this;
        }

        public Methods mapInterface(Class<?> cls) {
            checkState(cls.isInterface(), String.format("class %s is not an interface", cls.getName()));
            for (Method method : cls.getMethods()) {
                Method res = lookup(objCls, method.getName(), method.getParameterTypes());
                if (res != null) {
                    mapStrict(res, method);
                }
            }
            return this;
        }
    }
}
