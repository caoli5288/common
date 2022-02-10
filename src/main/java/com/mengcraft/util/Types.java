package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class Types {

    private static final Map<Class<?>, Desc> METHODS_DESCRIPTORS = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

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

    static Function<Method, IAccessor> ofAccessor() {
        return method -> {
            Class<?>[] types = method.getParameterTypes();
            if (method.getReturnType() == void.class) {
                switch (types.length) {
                    case 0:
                        Consumer<Object> consumer = asLambda(method, Consumer.class);
                        return (obj, params) -> {
                            consumer.accept(obj);
                            return null;
                        };
                    case 1:
                        BiConsumer<Object, Object> bc = asLambda(method, BiConsumer.class);
                        return (obj, params) -> {
                            bc.accept(obj, params[0]);
                            return null;
                        };
                    case 2:
                        Consumer3 consumer3 = asLambda(method, Consumer3.class);
                        return (obj, params) -> {
                            consumer3.accept(obj, params[0], params[1]);
                            return null;
                        };
                    case 3:
                        Consumer4 consumer4 = asLambda(method, Consumer4.class);
                        return (obj, params) -> {
                            consumer4.accept(obj, params[0], params[1], params[2]);
                            return null;
                        };
                }
            } else {
                switch (types.length) {
                    case 0:
                        Function<Object, Object> function = asLambda(method, Function.class);
                        return (obj, params) -> function.apply(obj);
                    case 1:
                        BiFunction<Object, Object, Object> bf = asLambda(method, BiFunction.class);
                        return (obj, params) -> bf.apply(obj, params[0]);
                    case 2:
                        Function3 function3 = asLambda(method, Function3.class);
                        return (obj, params) -> function3.apply(obj, params[0], params[1]);
                    case 3:
                        Function4 function4 = asLambda(method, Function4.class);
                        return (obj, params) -> function4.apply(obj, params[0], params[1], params[2]);
                }
            }
            return new SimpleAccessor(method);
        };
    }

    @SneakyThrows
    public static <T> T asLambda(Method method, Class<T> cls) {
        Preconditions.checkState(cls.isInterface());
        MethodHandle mh = LOOKUP.unreflect(method);
        Method sam = sam(cls);
        Objects.requireNonNull(sam, "Class is not SAM class. " + cls);
        CallSite ct = LambdaMetafactory.metafactory(LOOKUP,
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

    interface IAccessor {

        Object access(Object obj, Object[] params) throws Exception;
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
            IAccessor accessor = desc.map.get(method);
            if (accessor == null) {
                Method m = lookup(desc.objCls, method.getName(), method.getParameterTypes());
                checkNotNull(m, String.format("%s: [%s] not mapped", desc.objCls, method));
                accessor = desc.mapStrict(m, method);
            }
            return accessor.access(obj, params);
        }
    }

    public static class Desc {

        private final Class<?> objCls;
        private final Map<Method, IAccessor> map = Maps.newHashMap();
        private final Map<Method, IAccessor> accessors = Maps.newHashMap();

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

        IAccessor mapStrict(Method from, Method to) {
            IAccessor accessor = accessors.computeIfAbsent(from, ofAccessor());
            map.put(to, accessor);
            return accessor;
        }
    }

    @RequiredArgsConstructor
    static class SimpleAccessor implements IAccessor {

        private final Method method;

        @Override
        public Object access(Object obj, Object[] params) throws Exception {
            return method.invoke(obj, params);
        }
    }

    interface Consumer3 {
        void accept(Object o, Object o1, Object o2);
    }

    interface Function3 {
        Object apply(Object o, Object o1, Object o2);
    }

    interface Consumer4 {
        void accept(Object o, Object o1, Object o2, Object o3);
    }

    interface Function4 {
        Object apply(Object o, Object o1, Object o2, Object o3);
    }
}
