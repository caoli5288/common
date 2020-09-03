package com.mengcraft.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://tools.ietf.org/html/rfc4180
 */
public class CommaSeparatedValuesReader extends BufferedReader {

    private static final int INIT_VALUES_LENGTH = 8;
    private int maxValues = INIT_VALUES_LENGTH;
    private int lines;

    public CommaSeparatedValuesReader(InputStream stream) {
        super(new InputStreamReader(stream));
    }

    public CommaSeparatedValuesReader(Reader in) {
        super(in);
    }

    public CommaSeparatedValuesReader(String text) {
        this(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    public List<String> readSeparatedValues() throws IOException {
        int n = read();
        if (n == -1) {// EOF
            return null;
        }
        Values values = new Values();
        for (; ; n = read()) {
            if (n == -1 || reads(values, n)) {
                lines++;
                return values.endValues();
            }
        }
    }

    /**
     * @param values
     * @param reads
     * @return {@code true} only if values end
     */
    private boolean reads(Values values, int reads) throws IOException {
        switch (reads) {
            case '\r':
                /*
                 * We simply ignore it.
                 */
                break;
            case '\n':
                if (values.quota != 1) {
                    lines++;
                    return true;
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
        return false;
    }

    private class Values {

        private final List<String> values = new ArrayList<>(maxValues);
        private StringBuilder sb = new StringBuilder();
        private int quota;

        void append(char v) {
            sb.append(v);
        }

        void endValue() {
            quota = 0;
            values.add(sb.toString());
        }

        void newValue() {
            endValue();
            sb = new StringBuilder();
        }

        List<String> endValues() {
            endValue();
            if (maxValues < values.size()) {
                maxValues = values.size();
            }
            return values;
        }

        void appendQuota() throws IOException {
            if (quota == 0) {
                if (sb.length() != 0) {
                    throw new IOException(String.format("Exception occurred after reads %s line.", lines));
                }
                quota = 1;
            } else if (quota == 1) {
                quota = 2;
            } else {// When quota == 2.
                quota = 1;
                sb.append('"');
            }
        }
    }

}
