package com.mengcraft.util;

import lombok.SneakyThrows;
import lombok.val;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public final class MD5 {

    private static final ThreadLocal<MessageDigest> MD = ThreadLocal.withInitial(MD5::load);

    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'
    };

    @SneakyThrows
    private static MessageDigest load() {
        return MessageDigest.getInstance("MD5");
    }

    public static String digest(String in) {
        if (in == null) {
            throw new NullPointerException();
        }
        return digest(in.getBytes());
    }

    @SneakyThrows
    public static String digest(byte[] in) {
        if (in == null) {
            throw new NullPointerException();
        }
        val md = MD.get();
        md.update(in);
        byte[] out = md.digest();
        return hex(out);
    }

    public static void update(byte[] input) {
        if (input == null) {
            throw new NullPointerException();
        }
        MD.get().update(input);
    }

    public static void update(ByteBuffer input) {
        if (input == null) {
            throw new NullPointerException();
        }
        MD.get().update(input);
    }

    public static String digest() {
        return hex(MD.get().digest());
    }

    public static void reset() {
        MD.get().reset();
    }

    public static String hex(byte[] out) {
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
