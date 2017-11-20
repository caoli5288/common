package com.mengcraft.util.compiler;

import lombok.SneakyThrows;
import lombok.val;

import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by on 2017/9/11.
 */
public enum MyJava {

    INSTANCE;

    private final Map<String, Class<?>> all = new HashMap<>();
    private final Method define;

    @SneakyThrows
    MyJava() {
        define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        define.setAccessible(true);
    }

    @SneakyThrows
    public static Class<?> compile(ClassLoader ctx, String name, String java) {
        val tool = ToolProvider.getSystemJavaCompiler();
        val man = new MyJavaFileManager(tool.getStandardFileManager(null, null, null));
        val i = Collections.singletonList(new MyJavaFile(name, java));
        val compileTask = tool.getTask(null, man, null, null, null, i);
        if (compileTask.call()) {
            val all = man.getAll();
            for (val l : all) {
                byte[] b = l.getValue().toByteArray();
                val key = l.getKey();
                INSTANCE.all.put(key, (Class<?>) INSTANCE.define.invoke(ctx, key, b, 0, b.length));
            }
            return INSTANCE.all.get(name);
        }
        throw new IllegalStateException("compile");
    }

    public static Class<?> compile(String name, String java) {
        return compile(MyJava.class.getClassLoader(), name, java);
    }

    public static Class<?> compiled(String name) {
        return INSTANCE.all.get(name);
    }

}
