package com.mengcraft.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Types {

    private static final Map<Class<?>, Desc> METHODS_DESCRIPTORS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T asType(Object obj, Class<T> cls) {
        obj = unpack(obj);
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new Invoker(desc(obj.getClass()), obj));
    }

    private static Object unpack(Object obj) {
        if (Proxy.isProxyClass(obj.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(obj);
            checkState(handler instanceof Invoker, "cannot apply to unknown proxy instance");
            return ((Invoker) handler).obj;
        }
        return obj;
    }

    public static Desc desc(Class<?> cls) {
        Desc desc = METHODS_DESCRIPTORS.get(cls);
        if (desc == null) {
            desc = new Desc(cls);
            METHODS_DESCRIPTORS.put(desc.objCls, desc);
        }
        return desc;
    }

    private static Method lookup(Class<?> cls, String methodName, Class<?>... paramsTypes) {
        try {
            return cls.getDeclaredMethod(methodName, paramsTypes);
        } catch (Exception e) {
            // TODO still buggy here
            Class<?> sCls = cls.getSuperclass();
            if (sCls != null) {
                Method method = lookup(sCls, methodName, paramsTypes);
                if (method != null) {
                    return method;
                }
            }
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

        private final Desc desc;
        private final Object obj;

        Invoker(Desc desc, Object obj) {
            this.desc = desc;
            this.obj = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Exception {
            Method res = desc.map.get(method);
            if (res == null) {
                res = lookup(desc.objCls, method.getName(), method.getParameterTypes());
                checkNotNull(res, String.format("%s: [%s] not mapped", desc.objCls, method));
                desc.mapStrict(res, method);
            }
            return res.invoke(obj, params);
        }
    }

    public static class Desc {

        private final Class<?> objCls;
        private final Map<Method, Method> map = new HashMap<>();

        Desc(Class<?> objCls) {
            this.objCls = objCls;
        }

        public Desc map(String from, Class<?> cls, String to) {
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

        public Desc mapStrict(Method from, Method to) {
            if (!from.isAccessible()) {
                from.setAccessible(true);
            }
            map.put(to, from);
            return this;
        }
    }
}
