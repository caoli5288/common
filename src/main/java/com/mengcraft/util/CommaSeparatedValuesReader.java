package com.mengcraft.util;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * https://tools.ietf.org/html/rfc4180
 */
public class CommaSeparatedValuesReader extends InputStreamReader {

    public static final int INIT_VALUES_LENGTH = 8;
    private int maxValues = INIT_VALUES_LENGTH;
    private int lines;

    public CommaSeparatedValuesReader(InputStream stream) {
        super(stream);
    }

    public CommaSeparatedValuesReader(String text) {
        this(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    public String[] readSeparatedValues() throws IOException {
        Values values = new Values();
        for (; ; ) {
            int reads = read();
            if (reads == -1) {
                lines++;
                return values.endValues();
            }
            switch (reads) {
                case '\r':
                    /*
                     * We simply ignore it.
                     */
                    break;
                case '\n':
                    if (values.quota != 1) {
                        lines++;
                        return values.endValues();
                    }
                    values.append('\n');
                    break;
                case '"':
                    values.appendQuota();
                    break;
                case ',':
                    if (values.quota == 1) {
                        values.append(',');
                    } else {
                        values.newValue();
                    }
                    break;
                default:
                    if (values.quota == 2) {
                        throw new IOException(String.format("Exception occurred after reads %s line.", lines));
                    }
                    values.append((char) reads);
                    break;
            }
        }
    }

    public JSONObject readSeparatedValues(@NonNull String[] keys) throws IOException {
        Preconditions.checkArgument(keys.length != 0, "Keys must not nil.");
        JSONObject obj = new JSONObject();
        String[] values = readSeparatedValues();
        if (keys.length != values.length) {
            throw new IOException("Keys length must same as values");
        }
        for (int i = 0; i < keys.length; i++) {
            obj.put(keys[i], values[i]);
        }
        return obj;
    }

    private class Values {

        private String[] buf = new String[maxValues];
        private int length;
        private StringBuilder value = new StringBuilder();
        private int quota;

        void append(char v) {
            value.append(v);
        }

        void endValue() {
            if (buf.length == length) {
                buf = Arrays.copyOf(buf, buf.length << 1);
            }
            quota = 0;
            buf[length++] = value.toString();
        }

        void newValue() {
            endValue();
            value = new StringBuilder();

        }

        String[] endValues() {
            endValue();
            if (maxValues < length) {
                maxValues = length;
            }
            return Arrays.copyOf(buf, length);
        }

        void appendQuota() throws IOException {
            if (quota == 0) {
                if (value.length() != 0) {
                    throw new IOException(String.format("Exception occurred after reads %s line.", lines));
                }
                quota = 1;
            } else if (quota == 1) {
                quota = 2;
            } else {// When quota == 2.
                quota = 1;
                value.append('"');
            }
        }
    }

}
