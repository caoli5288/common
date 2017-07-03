package com.mengcraft.util.http;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;

/**
 * Created on 17-3-18.
 */

@Data
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Response {

    private final UUID id = UUID.randomUUID();
    private final String request;
    private final HTTPMethod requestMethod;
    private final int response;
    private final InputStream dataInput;

    private String message;

    public String getMessage() {
        if (!HTTP.nil(message)) return message;

        Reader reader = new InputStreamReader(dataInput);
        StringBuilder b = new StringBuilder();
        char[] buf = new char[8192];
        try {
            for (int i; (i = reader.read(buf)) > -1; ) {
                b.append(buf, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (message = b.toString());
    }

}
