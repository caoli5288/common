package com.mengcraft.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Hmac {

    public static final String SHA256 = "HmacSHA256";

    private final Mac mac;

    public void update(byte[] ctx) {
        mac.update(ctx);
    }

    public void update(ByteBuffer buf) {
        mac.update(buf);
    }

    public byte[] result() {
        return mac.doFinal();
    }

    @SneakyThrows
    public static String hash(String signing, String key, String ctx) {
        Hmac hmac = build(signing, key.getBytes("utf-8"));
        hmac.update(ctx.getBytes("utf-8"));
        return Hex.hex(hmac.result());
    }

    @SneakyThrows
    public static Hmac build(@NonNull String signing, @NonNull byte[] key) {
        Mac mac = Mac.getInstance(signing);
        mac.init(new SecretKeySpec(key, signing));
        return new Hmac(mac);
    }

}
