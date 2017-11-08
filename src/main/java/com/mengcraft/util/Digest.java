package com.mengcraft.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Digest {

    public static final String MD5 = "MD5";

    private final MessageDigest digest;

    public void update(byte[] buf) {
        digest.update(buf);
    }

    public void update(byte b) {
        digest.update(b);
    }

    public void update(ByteBuffer buf) {
        digest.update(buf);
    }

    public byte[] result() {
        return digest.digest();
    }

    @SneakyThrows
    public static String hash(String ctx) {
        Digest digest = build(MD5);
        digest.update(ctx.getBytes("utf-8"));
        return Hex.hex(digest.result());
    }

    @SneakyThrows
    public static Digest build(String signing) {
        return new Digest(MessageDigest.getInstance("MD5"));
    }
}
