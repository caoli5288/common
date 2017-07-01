package com.mengcraft.util.library;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

/**
 * Created on 15-12-13.
 */
public abstract class Library {

    public List<Library> getSublist() {
        return ImmutableList.of();
    }

    public boolean isLoadable() {
        return getFile().isFile();
    }

    public void init() {
        throw new UnsupportedOperationException("init");
    }

    public abstract File getFile();
}
