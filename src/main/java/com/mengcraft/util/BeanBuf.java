package com.mengcraft.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BeanBuf<T> {

    private final Cache<String, T> cache;
    private final List<BeanDescriptor> descriptors = new ArrayList<>();

    public BeanBuf() {
        cache = CacheBuilder.newBuilder().build();
    }

    public BeanBuf(CacheBuilder<Object, Object> b) {
        cache = b.build();
    }

    public void descriptor(String key, Function<T, Object> function) {
        descriptors.add(new BeanDescriptor(key, function));
    }

    public T put(T obj) {
        for (BeanDescriptor descriptor : descriptors) {
            cache.asMap().put(descriptor.cacheKey(obj), obj);
        }
        return obj;
    }

    public void remove(T obj) {
        for (BeanDescriptor descriptor : descriptors) {
            cache.asMap().remove(descriptor.cacheKey(obj), obj);
        }
    }

    public T get(BeanDescription description) {
        return cache.asMap().get(description.cacheKey());
    }

    public boolean containsKey(BeanDescription description) {
        return cache.asMap().containsKey(description.cacheKey());
    }

    public T get(BeanDescription description, Supplier<T> supplier) {
        String cacheKey = description.cacheKey();
        if (cache.asMap().containsKey(cacheKey)) {
            return cache.asMap().get(cacheKey);
        }
        T obj = supplier.get();
        return obj == null ? null : put(obj);
    }

    @RequiredArgsConstructor
    public static class BeanDescription {

        private final String key;
        private final Object value;

        String cacheKey() {
            return key + "=" + value;
        }

        public static BeanDescription of(String key, Object value) {
            return new BeanDescription(key, value);
        }

    }

    @RequiredArgsConstructor
    public class BeanDescriptor {

        private final String key;
        private final Function<T, Object> provider;

        String cacheKey(T obj) {
            return key + "=" + provider.apply(obj);
        }

    }

}
