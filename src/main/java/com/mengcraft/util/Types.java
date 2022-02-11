package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Types {

    private static final Map<Class<?>, Desc> METHODS_DESCRIPTORS = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = lookup();

    @SneakyThrows
    static MethodHandles.Lookup lookup() {
        Field f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        f.setAccessible(true);
        return (MethodHandles.Lookup) f.get(MethodHandles.Lookup.class);
    }

    Types() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T asType(Object obj, Class<T> cls) {
        Preconditions.checkState(cls.isInterface(), "Class is not interface. " + cls);
        obj = unpack(obj);
        if (cls.isInstance(obj)) {//  Who's so boring
            return (T) obj;
        }
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new Handle(desc(obj.getClass()), obj));
    }

    private static Object unpack(Object obj) {
        if (Proxy.isProxyClass(obj.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(obj);
            checkState(handler instanceof Handle, "cannot apply to unknown proxy instance");
            return ((Handle) handler).obj;
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

    private static Method lookup(Class<?> cls, String methodName, Class<?>... types) {
        Method method = MethodUtils.getMatchingMethod(cls, methodName, types);
        if (method != null && !method.isAccessible()) {
            method.setAccessible(true);
        }
        return method;
    }

    @SneakyThrows
    public static <T> T asLambda(Method method, Class<T> cls) {
        Preconditions.checkState(cls.isInterface());
        Method sam = sam(cls);
        Objects.requireNonNull(sam, "Class is not SAM class. " + cls);
        // Workaround for private accessor
        MethodHandles.Lookup lookup = LOOKUP.in(method.getDeclaringClass());
        MethodHandle mh = lookup.unreflect(method);
        CallSite ct = LambdaMetafactory.metafactory(lookup,
                sam.getName(),
                MethodType.methodType(cls),
                MethodType.methodType(sam.getReturnType(), sam.getParameterTypes()),
                mh,
                MethodType.methodType(sam.getReturnType(), mh.type().parameterArray()));
        return (T) ct.getTarget().invoke();
    }

    static Method sam(Class<?> cls) {
        Method sam = null;
        for (Method method : cls.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                if (sam != null) {
                    return null;
                }
                sam = method;
            }
        }
        return sam;
    }

    static class Handle implements InvocationHandler {

        private final Desc desc;
        private final Object obj;

        Handle(Desc desc, Object obj) {
            this.desc = desc;
            this.obj = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Exception {
            Method accessor = desc.map.get(method);
            if (accessor == null) {
                accessor = lookup(desc.objCls, method.getName(), method.getParameterTypes());
                checkNotNull(accessor, String.format("%s: [%s] not mapped", desc.objCls, method));
                desc.mapStrict(accessor, method);
            }
            return accessor.invoke(obj, params);
        }
    }

    public static class Desc {

        private final Class<?> objCls;
        private final Map<Method, Method> map = Maps.newHashMap();

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

        void mapStrict(Method from, Method to) {
            map.put(to, from);
        }
    }
}
