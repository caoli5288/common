package com.mengcraft.util;

/**
 * Created on 16-3-13.
 */
public class TitleEntry {

    public final String title;
    public final String sub;

    public int fadeIn;
    public int fadeOut;
    public int display;

    public TitleEntry(String title, String sub, int fadeIn, int display, int fadeOut) {
        this.title = title;
        this.sub = sub;
        this.fadeIn = fadeIn;
        this.display = display;
        this.fadeOut = fadeOut;
    }

    public TitleEntry(String title, String sub) {
        this.title = title;
        this.sub = sub;
    }

    public TitleEntry setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public TitleEntry setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public TitleEntry setDisplay(int display) {
        this.display = display;
        return this;
    }

}
