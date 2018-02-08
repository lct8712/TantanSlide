package com.chentian.tantanslide.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.chentian.tantanslide.R;

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
        int[] colors = getResources().getIntArray(R.array.material_colors);
        for (int color : colors) {
            generateSliableImage(color);
        }
    }

    private void generateSliableImage(int colorInt) {
        SlidableImage slidableImage = new SlidableImage(getContext());
        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(32, 32, 32, 32);

        slidableImage.setBackgroundResource(R.drawable.photo_with_frame);
        GradientDrawable shapeDrawable = (GradientDrawable) slidableImage.getBackground();
        shapeDrawable.setColor(colorInt);

        addView(slidableImage, layoutParams);
    }
}
