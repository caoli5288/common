/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mengcraft.util.compiler;

import lombok.val;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MyJavaFileManager implements JavaFileManager {

    private final Map<String, ByteArrayOutputStream> mapping = new HashMap<>();
    private final StandardJavaFileManager sup;

    MyJavaFileManager(StandardJavaFileManager sup) {
        this.sup = sup;
    }

    public ClassLoader getClassLoader(Location location) {
        return sup.getClassLoader(location);
    }

    public Iterable<JavaFileObject> list(Location location, String pkg, Set<Kind> k, boolean recurse) throws IOException {
        return sup.list(location, pkg, k, recurse);
    }

    public String inferBinaryName(Location location, JavaFileObject file) {
        return sup.inferBinaryName(location, file);
    }

    public boolean isSameFile(FileObject a, FileObject b) {
        return sup.isSameFile(a, b);
    }

    public boolean handleOption(String current, Iterator<String> remaining) {
        return sup.handleOption(current, remaining);
    }

    public boolean hasLocation(Location location) {
        return sup.hasLocation(location);
    }

    public JavaFileObject getJavaFileForInput(Location location, String name, Kind kind) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT && mapping.containsKey(name) && kind == Kind.CLASS) {
            final byte[] b = mapping.get(name).toByteArray();
            return new SimpleJavaFileObject(URI.create(name), kind) {

                public InputStream openInputStream() {
                    return new ByteArrayInputStream(b);
                }
            };
        }
        return sup.getJavaFileForInput(location, name, kind);
    }


    public JavaFileObject getJavaFileForOutput(Location location, final String name, Kind kind, FileObject sibling) throws IOException {
        return new SimpleJavaFileObject(URI.create(name), kind) {

            public OutputStream openOutputStream() {
                val out = new ByteArrayOutputStream();
                mapping.put(name, out);
                return out;
            }
        };
    }

    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        return sup.getFileForInput(location, packageName, relativeName);
    }

    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        return sup.getFileForOutput(location, packageName, relativeName, sibling);
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
        sup.close();
    }

    public Set<Map.Entry<String, ByteArrayOutputStream>> getAll() {
        return mapping.entrySet();
    }

    public int isSupportedOption(String option) {
        return sup.isSupportedOption(option);
    }

}
