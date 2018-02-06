package com.chentian.tantanslide.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author chentian
 */
public class SlideImageContainer extends FrameLayout {

    public SlideImageContainer(Context context) {
        super(context);
    }

    public SlideImageContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideImageContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void loadData() {
        SlidableImage slidableImage = new SlidableImage(getContext());
        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(32, 32, 32, 32);
        addView(slidableImage, layoutParams);
    }
}
