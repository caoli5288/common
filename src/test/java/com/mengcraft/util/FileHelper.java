package com.mengcraft.util;

import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by on 2017/9/9.
 */
public class FileHelper {

    static void appendSub(List<File> out, File nextFile, LinkedList<String> metaItr) {
        if (metaItr.isEmpty()) {
            out.add(nextFile);
        } else {
            if (nextFile.isDirectory()) {
                out.addAll(listFile(nextFile, new LinkedList<>(metaItr)));
            }
        }
    }

    static void appendWild(StringBuilder out, int i) {
        if (i == '?') {
            out.append('.');
        } else if (i == '*') {
            out.append(".*");
        } else if (i == '.') {
            out.append("\\.");
        } else {
            out.append(((char) i));
        }
    }

    @SneakyThrows
    public static String wild2Pattern(String pattern) {
        val input = new StringReader(pattern);
        val out = new StringBuilder();
        for (int i = input.read(); !(i == -1); i = input.read()) {
            if (i == '\\') {
                int next = input.read();
                if (!(next == -1)) {
                    out.append(((char) next));
                }
            } else {
                appendWild(out, i);
            }
        }
        return out.toString();
    }

    static List<File> listFile(File baseFolder, LinkedList<String> wildItr) {
        val out = new LinkedList<File>();
        val next = wildItr.poll();
        if (next.matches(".*[\\*\\?]+.*")) {
            val pattern = FileHelper.wild2Pattern(next);
            val list = baseFolder.list((dir, name) -> name.matches(pattern));
            if (list == null) return out;
            for (String l : list) {
                File subFile = new File(baseFolder, l);
                appendSub(out, subFile, wildItr);
            }
        } else {
            val nextFile = new File(baseFolder, next);
            if (nextFile.canRead()) {
                appendSub(out, nextFile, wildItr);
            }
        }
        return out;
    }

    @SneakyThrows
    static List<String> split(String wild, char separator) {
        ImmutableList.Builder<String> b = ImmutableList.builder();
        val input = new StringReader(wild);
        StringBuilder str = new StringBuilder();
        for (int i; !((i = input.read()) == -1); ) {
            if (i == separator) {
                b.add(str.toString());
                str = new StringBuilder();
            } else {
                str.append((char) i);
            }
        }
        b.add(str.toString());
        return b.build();
    }

    /**
     * Unix's ls like func.
     *
     * @param baseFolder the base folder
     * @param wild       the wild allow '*' and '?'
     * @return the file list
     */
    public static List<File> listFile(File baseFolder, String wild) {
        return listFile(baseFolder, new LinkedList<>(split(wild, File.separatorChar)));
    }

}
