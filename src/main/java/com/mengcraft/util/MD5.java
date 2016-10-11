package com.mengcraft.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

public final class MD5 extends SecureRandom {

    public static final MD5 DEFAULT = new MD5();

    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    public String digest(String in) throws Exception {
        if (in == null) {
            throw new NullPointerException();
        }
        return digest(in.getBytes());
    }

    public String digest(byte[] in) throws Exception {
        if (in == null) {
            throw new NullPointerException();
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(in);
        byte[] out = md.digest();
        return hex(out);
    }

    public String random(int size) {
        byte[] input = new byte[size];
        nextBytes(input);
        return hex(input);
    }

    public String hex(byte[] out) {
        if (out == null) {
            throw new NullPointerException();
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : out) {
            buf.append(HEX[b >>> 4 & 0xf]);
            buf.append(HEX[b & 0xf]);
        }
        return buf.toString();
    }

}
