package com.mengcraft.util;

import org.junit.Test;

/**
 * Created on 16-10-12.
 */
public class ChooserTest {

    @Test
    public void get() throws Exception {
        Chooser<Object> chooser = new Chooser<>();
        chooser.put("0.1", 0.1);
        chooser.put("0.5", 0.5);
        for (int i = 0; i < 10; i++) {
            chooser.get();// 10% 0.1, 50% 0.5 and 40% null
        }
    }

}