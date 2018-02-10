package com.chentian.tantanslide.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.chentian.tantanslide.R;
import com.chentian.tantanslide.data.ColorProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chentian
 */
public class SlideImageContainer extends FrameLayout {

    private static final int IMAGE_COUNT_PER_PAGE = 10;

    private List<SlidableImage> imageList;

    public SlideImageContainer(Context context) {
        super(context);
    }

    public SlideImageContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideImageContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return childCount - i - 1;
    }

    public void loadData() {
        imageList = new ArrayList<>();
        setChildrenDrawingOrderEnabled(true);
        loadMore();
    }

    private void loadMore() {
        Log.d("chentian", "SlideImageContainer load more, child view count: " + getChildCount());
        for (int i = 0; i < IMAGE_COUNT_PER_PAGE; i++) {
            generateSlidableImage();
        }
    }

    private void generateSlidableImage() {
        final TestSlidableImage slidableImage = new TestSlidableImage(getContext());
        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(32, 32, 32, 32);

        slidableImage.setBackgroundResource(R.drawable.photo_with_frame);
        GradientDrawable shapeDrawable = (GradientDrawable) slidableImage.getBackground();
        shapeDrawable.setColor(ColorProvider.getInstance(getContext()).getNextColor());
        slidableImage.setId(ColorProvider.getInstance(getContext()).getNextId());

        addView(slidableImage, layoutParams);
        imageList.add(slidableImage);

        slidableImage.setStatusListener(new SlidableImage.StatusListener() {
            @Override
            public void onMoveAway(boolean isToRight) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeView(slidableImage);
                    }
                }, 3 * DateUtils.SECOND_IN_MILLIS);

                imageList.remove(slidableImage);
                if (imageList.size() < IMAGE_COUNT_PER_PAGE / 2) {
                    loadMore();
                }
                Log.d("chentian", "image go away: " + isToRight);
            }
        });
    }
}
