package com.mengcraft.util;

import org.junit.Test;

/**
 * Created on 16-10-12.
 */
public class WeightChooserTest {

    @Test
    public void get() throws Exception {
        Chooser<Object> chooser = new WeightChooser<>();
        chooser.put("1", 10);
        chooser.put("2", 90);
        for (int i = 0; i < 10; i++) {
            chooser.get();// 10% 1, 90% 2 and non null
        }
    }

}