package com.mengcraft.util;

import lombok.Builder;
import lombok.Data;

/**
 * Created on 16-3-13.
 */
@Builder
@Data
public class TitleEntry {

    private String title;
    private String subtitle;

    private int fadeIn;
    private int fadeOut;
    private int display;
}
