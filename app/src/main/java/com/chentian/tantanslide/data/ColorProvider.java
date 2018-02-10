package com.chentian.tantanslide.data;

import android.content.Context;

import com.chentian.tantanslide.R;

/**
 * @author chentian
 */
public class ColorProvider {

    private static ColorProvider instance;

    private final int[] colors;
    private int colorIndex;
    private int totalCount;

    public static ColorProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (ColorProvider.class) {
                if (instance == null) {
                    instance = new ColorProvider(context);
                }
            }
        }
        return instance;
    }

    private ColorProvider(Context context) {
        colors = context.getResources().getIntArray(R.array.material_colors);
        if (colors.length == 0) {
            throw new IllegalArgumentException("empty resource array: R.array.material_colors");
        }
    }

    public int getNextColor() {
        int result = colors[colorIndex++];
        colorIndex %= colors.length;
        return result;
    }

    public String getNextId() {
        return String.valueOf(totalCount++);
    }
}
