package com.chentian.tantanslide.widget;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡片的容器
 *
 * @author chentian
 */
public class SlideImageContainer extends FrameLayout {

    private static final int IMAGE_COUNT_PER_PAGE = 10;
    private static final int IMAGE_MARGIN = 48;

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
        for (int i = 0; i < IMAGE_COUNT_PER_PAGE; i++) {
            generateSlidableImage();
        }
    }

    private void generateSlidableImage() {
        final TestSlidableImage slidableImage = new TestSlidableImage(getContext());
        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(IMAGE_MARGIN, IMAGE_MARGIN, IMAGE_MARGIN, IMAGE_MARGIN);

        slidableImage.setStackPosition(imageList.size());
        addView(slidableImage, layoutParams);
        imageList.add(slidableImage);

        slidableImage.setStatusListener(new SlidableImage.StatusListener() {
            @Override
            public void onMove(float translationX, float translationY) {
                int index = imageList.indexOf(slidableImage);
                for (int i = index + 1; i <= index + 3 && index < imageList.size(); i++) {
                    imageList.get(i).handlePassiveMove(translationX, translationY);
                }
            }

            @Override
            public void onGoAway(boolean isToRight) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeView(slidableImage);
                    }
                }, 3 * DateUtils.SECOND_IN_MILLIS);

                int index = imageList.indexOf(slidableImage);
                updateInitialPosition(index + 1);

                imageList.remove(slidableImage);
                slidableImage.destroy();
                if (imageList.size() < IMAGE_COUNT_PER_PAGE / 2) {
                    loadMore();
                }
            }
        });
    }

    private void updateInitialPosition(int startIndex) {
        for (int i = startIndex; i < imageList.size(); i++) {
            imageList.get(i).setStackPosition(i - startIndex);
        }
    }
}
