package com.mengcraft.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Created on 16-1-17.
 */
public final class ReadWriteUtil {

    public static BufferedWriter toBuffered(OutputStream stream) {
        return toBuffered(toWriter(stream));
    }

    public static Writer toWriter(OutputStream stream) {
        return new OutputStreamWriter(stream);
    }

    public static BufferedWriter toBuffered(Writer writer) {
        return new BufferedWriter(writer);
    }

    public static BufferedReader toBuffered(InputStream stream) {
        return toBuffered(toReader(stream));
    }

    public static Reader toReader(InputStream stream) {
        return new InputStreamReader(stream);
    }

    public static Reader toReader(byte[] buf, int off, int len) {
        return toReader(new ByteArrayInputStream(buf, off, len));
    }


    public static BufferedReader toBuffered(Reader reader) {
        return new BufferedReader(reader);
    }

    public static DataInput toDataInput(byte[] data) {
        return toDataInput(new ByteArrayInputStream(data));
    }

    public static DataInput toDataInput(InputStream in) {
        return new DataInputStream(in);
    }

    public static DataOutput toDataOutput(OutputStream out) {
        return new DataOutputStream(out);
    }

}
