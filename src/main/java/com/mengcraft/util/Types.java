package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

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
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Types {

    private static final MethodHandles.Lookup LOOKUP = lookup();

    public static MethodHandles.Lookup lookupPrivileged(Class<?> cls) {
        return lookupPrivileged().in(cls);
    }

    public static MethodHandles.Lookup lookupPrivileged() {
        return LOOKUP;
    }

    @SneakyThrows
    static MethodHandles.Lookup lookup() {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(Unsafe.class);
        field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long offset = unsafe.staticFieldOffset(field);
        return (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, offset);
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
        Desc desc = Desc.METHODS_DESCRIPTORS.get(cls);
        if (desc == null) {
            desc = new Desc(cls);
            Desc.METHODS_DESCRIPTORS.put(desc.objCls, desc);
        }
        return desc;
    }

    @Nullable
    private static Method lookup(Class<?> cls, String methodName, Class<?>... types) {
        return MethodUtils.getMatchingMethod(cls, methodName, types);
    }

    @SneakyThrows
    public static <T> T lambdaPrivileged(Method method, Class<T> cls) {
        Preconditions.checkState(cls.isInterface());
        Method sam = sam(cls);
        Objects.requireNonNull(sam, "Class is not SAM class. " + cls);
        // Workaround for private accessor
        MethodHandles.Lookup lookup = lookupPrivileged(method.getDeclaringClass());
        return (T) ofCallTarget(lookup, sam, method).invoke();
    }

    private static MethodHandle ofCallTarget(MethodHandles.Lookup lookup, Method sam, Method implMethod) throws Exception {
        MethodHandle handle = Desc.CALL_TARGETS.get(sam, implMethod);
        if (handle == null) {
            MethodHandle impl = lookup.unreflect(implMethod);
            CallSite ct = LambdaMetafactory.metafactory(lookup,
                    sam.getName(),
                    MethodType.methodType(sam.getDeclaringClass()),
                    MethodType.methodType(sam.getReturnType(), sam.getParameterTypes()),
                    impl,
                    impl.type());
            Desc.CALL_TARGETS.put(sam, implMethod, handle = ct.getTarget());
        }
        return handle;
    }

    @SneakyThrows
    public static <T> T lambdaPrivileged(Object obj, Method method, Class<T> cls) {
        Preconditions.checkState(cls.isInterface());
        MethodHandles.Lookup lookup = lookupPrivileged(method.getDeclaringClass());
        Method sam = sam(cls);
        Objects.requireNonNull(sam, "Class is not SAM. " + cls);
        return (T) ofBoundCallTarget(lookup, sam, method).invoke(obj);
    }

    private static MethodHandle ofBoundCallTarget(MethodHandles.Lookup lookup, Method sam, Method implMethod) throws Exception {
        MethodHandle handle = Desc.BOUND_CALL_TARGETS.get(sam, implMethod);
        if (handle == null) {
            MethodHandle impl = lookup.unreflect(implMethod);
            CallSite ct = LambdaMetafactory.metafactory(lookup,
                    sam.getName(),
                    MethodType.methodType(sam.getDeclaringClass(), impl.type().parameterType(0)),
                    MethodType.methodType(sam.getReturnType(), sam.getParameterTypes()),
                    impl,
                    impl.type().dropParameterTypes(0, 1));
            Desc.BOUND_CALL_TARGETS.put(sam, implMethod, handle = ct.getTarget());
        }
        return handle;
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

    interface MethodHandleCaller {

        Object call(Object[] allArgs) throws Throwable;
    }

    static class Handle implements InvocationHandler {

        private final Map<Method, MethodHandleCaller> handles = Maps.newHashMap();
        private final Desc desc;
        private final Object obj;

        Handle(Desc desc, Object obj) {
            this.desc = desc;
            this.obj = obj;
        }

        @Override
        @SneakyThrows
        public Object invoke(Object proxy, Method method, Object[] allArgs) {
            MethodHandleCaller handle = handles.computeIfAbsent(method, __ -> lookupBinding(method));
            return handle.call(allArgs);
        }

        private @NotNull MethodHandleCaller lookupBinding(Method method) {
            Map.Entry<Method, MethodHandle> lookupHandle = desc.lookupHandle(method);
            MethodHandle handle = lookupHandle.getValue();
            switch (method.getParameterCount()) {
                case 0: {
                    if (method.getReturnType() == void.class) {
                        Runnable l = lambdaPrivileged(obj, lookupHandle.getKey(), Runnable.class);
                        return __ -> {
                            l.run();
                            return null;
                        };
                    } else {
                        Supplier<?> l = lambdaPrivileged(obj, lookupHandle.getKey(), Supplier.class);
                        return __ -> l.get();
                    }
                }
                case 1: {
                    MethodHandle impl = handle.bindTo(obj)
                            .asType(MethodType.methodType(Object.class, Object.class));
                    return l -> impl.invokeExact(l[0]);
                }
                case 2: {
                    MethodHandle impl = handle.bindTo(obj)
                            .asType(MethodType.methodType(Object.class, Object.class, Object.class));
                    return l -> impl.invokeExact(l[0], l[1]);
                }
                case 3: {
                    MethodHandle impl = handle.bindTo(obj)
                            .asType(MethodType.methodType(Object.class, Object.class, Object.class, Object.class));
                    return l -> impl.invokeExact(l[0], l[1], l[2]);
                }
                case 4: {
                    MethodHandle impl = handle.bindTo(obj)
                            .asType(MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class));
                    return l -> impl.invokeExact(l[0], l[1], l[2], l[3]);
                }
                case 5: {
                    MethodHandle impl = handle.bindTo(obj)
                            .asType(MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
                    return l -> impl.invokeExact(l[0], l[1], l[2], l[3], l[4]);
                }
                default: {
                    MethodHandle impl = handle.bindTo(obj)
                            .asSpreader(Object[].class, method.getParameterCount())
                            .asType(MethodType.methodType(Object.class, Object[].class));
                    return impl::invokeExact;
                }
            }
        }
    }

    public static class Desc {

        private static final Map<Class<?>, Desc> METHODS_DESCRIPTORS = Maps.newHashMap();
        private static final Table<Method, Method, MethodHandle> BOUND_CALL_TARGETS = HashBasedTable.create();
        private static final Table<Method, Method, MethodHandle> CALL_TARGETS = HashBasedTable.create();
        private final Map<Method, Map.Entry<Method, MethodHandle>> handles = Maps.newHashMap();
        private final Class<?> objCls;
        private final MethodHandles.Lookup lookup;

        Desc(Class<?> objCls) {
            this.objCls = objCls;
            lookup = lookupPrivileged(objCls);
        }

        @SneakyThrows
        public Desc map(String from, Class<?> cls, String to) {
            checkState(cls.isInterface(), String.format("class %s is not an interface", cls.getName()));
            for (Method method : cls.getMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) && method.getName().equals(to)) {
                    Method call = lookup(objCls, from, method.getParameterTypes());
                    if (call != null) {
                        mapStrict(Maps.immutableEntry(call, lookup.unreflect(call)), method);
                    }
                }
            }
            return this;
        }

        Map.Entry<Method, MethodHandle> lookupHandle(Method call) {
            return handles.computeIfAbsent(call, __ -> lookupHandle0(call));
        }

        @SneakyThrows
        private Map.Entry<Method, MethodHandle> lookupHandle0(Method call) {
            Method impl = lookup(objCls, call.getName(), call.getParameterTypes());
            checkNotNull(impl, String.format("%s: [%s] not mapped", objCls, call));
            return Maps.immutableEntry(impl, lookup.unreflect(impl));
        }

        void mapStrict(Map.Entry<Method, MethodHandle> handle, Method call) {
            handles.put(call, handle);
        }
    }
}
