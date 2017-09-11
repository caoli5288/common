package com.mengcraft.util.compiler;

import org.junit.Test;

/**
 * Created by on 2017/9/11.
 */
public class MyJavaTest {

    @Test
    public void compile() throws Exception {
        Class<?> clz = MyJava.compile("com.mengcraft.util.compiler.T1", "" +
                "package com.mengcraft.util.compiler;\n" +
                "public class T1 implements Runnable {\n" +
                "    public void run() {\n" +
                "        System.out.println(\"dyn java class ok\");\n" +
                "    }\n" +
                "}");
        Runnable r = (Runnable) clz.newInstance();
        r.run();
    }

}
