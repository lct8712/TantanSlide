package com.chentian.tantanslide.widget;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡片的容器
 *
 * @author chentian
 */
public class SlideCardContainer extends FrameLayout {

    private static final int IMAGE_COUNT_PER_PAGE = 10;
    private static final int IMAGE_MARGIN = 48;

    private List<SlidableCard> cardList;

    public SlideCardContainer(Context context) {
        super(context);
    }

    public SlideCardContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideCardContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return childCount - i - 1;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void loadData() {
        cardList = new ArrayList<>();
        setChildrenDrawingOrderEnabled(true);
        loadMore();
    }

    private void loadMore() {
        for (int i = 0; i < IMAGE_COUNT_PER_PAGE; i++) {
            generateSlidableCard();
        }
    }

    private void generateSlidableCard() {
        final SlidableCard slidableCard = new SlidableCard(getContext());
        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(IMAGE_MARGIN, IMAGE_MARGIN, IMAGE_MARGIN, IMAGE_MARGIN);
        slidableCard.setStackPosition(cardList.size());
        addView(slidableCard, layoutParams);
        cardList.add(slidableCard);

        addImageContentToCard(slidableCard);

        slidableCard.setStatusListener(new SlidableCard.StatusListener() {
            @Override
            public void onMove(float translationX, float translationY) {
                int index = cardList.indexOf(slidableCard);
                for (int i = index + 1; i <= index + 3 && index < cardList.size(); i++) {
                    cardList.get(i).handlePassiveMove(translationX, translationY);
                }
            }

            @Override
            public void onGoAway(boolean isToRight) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeView(slidableCard);
                    }
                }, 3 * DateUtils.SECOND_IN_MILLIS);

                int index = cardList.indexOf(slidableCard);
                updateInitialPosition(index + 1);

                cardList.remove(slidableCard);
                slidableCard.destroy();
                if (cardList.size() < IMAGE_COUNT_PER_PAGE / 2) {
                    loadMore();
                }
            }
        });
    }

    private void addImageContentToCard(SlidableCard slidableCard) {
        TestImageContent imageContent = new TestImageContent(getContext());
        FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        slidableCard.addView(imageContent, layoutParams);
    }

    private void updateInitialPosition(int startIndex) {
        for (int i = startIndex; i < cardList.size(); i++) {
            cardList.get(i).setStackPosition(i - startIndex);
        }
    }
}
