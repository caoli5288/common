package com.mengcraft.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created on 16-1-6.
 */
public class Base64Test {

    @Test
    public void testEncode() throws Exception {
        char[] encoded = Base64.encode("This is a test!".getBytes());
        assertEquals("VGhpcyBpcyBhIHRlc3Qh", String.valueOf(encoded));
    }

    @Test
    public void testDecode() throws Exception {
        byte[] decoded = Base64.decode("VGhpcyBpcyBhIHRlc3Qh".toCharArray());
        assertEquals("This is a test!", new String(decoded));
    }

}