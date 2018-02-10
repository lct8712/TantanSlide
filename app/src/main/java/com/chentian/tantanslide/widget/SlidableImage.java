package com.chentian.tantanslide.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;

/**
 * @author chentian
 */
public class SlidableImage extends AppCompatImageView {

    public interface StatusListener {

        void onMoveAway(boolean isToRight);

    }

    private static final int MIN_MOVE = 2;
    private static final float MIN_TRANSLATION_TO_NORMAL = 1.1f;
    private static final long FRAME_DELAY_MILLIS = 16L;
    private static final float MIN_SPEED_FOR_GO_AWAY = 2f;
    private static final float MIN_DIRECTION_SPEED = 5f;
    private static final int MIN_BORDER_WIDTH = 250;

    private float lastX;
    private float lastY;
    private boolean isAnimating;
    private boolean isGoAway;
    private Handler mainThreadHandler;
    private VelocityHelper velocityHelper;

    private StatusListener statusListener;

    public SlidableImage(Context context) {
        this(context, null);
    }

    public SlidableImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidableImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mainThreadHandler = new Handler(Looper.getMainLooper());
        velocityHelper = new VelocityHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGoAway) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleEventDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleEventMove(event);
                break;
            case MotionEvent.ACTION_UP:
                handleEventUp(event);
                break;
            default:
                break;
        }
        return true;
    }

    private void handleEventDown(MotionEvent event) {
        lastX = event.getRawX();
        lastY = event.getRawY();
        velocityHelper.reset();
        velocityHelper.record(event.getRawX(), event.getRawY());
        isAnimating = false;
    }

    private void handleEventMove(MotionEvent event) {
        float dx = event.getRawX() - lastX;
        float dy = event.getRawY() - lastY;
        if (Math.abs(dx) >= MIN_MOVE || Math.abs(dy) >= MIN_MOVE) {
            setTranslationX(getTranslationX() + dx);
            setTranslationY(getTranslationY() + dy);
            lastX = event.getRawX();
            lastY = event.getRawY();
            velocityHelper.record(event.getRawX(), event.getRawY());
        }
        isAnimating = false;
    }

    private void handleEventUp(MotionEvent event) {
        isAnimating = true;
        velocityHelper.record(event.getRawX(), event.getRawY());
        if (velocityHelper.computeVelocity() >= MIN_SPEED_FOR_GO_AWAY) {
            Pair<Float, Float> velocity = velocityHelper.computeVelocityWithDirection();
            float multi = 15f;
            float maxDelta = Math.max(Math.abs(velocity.first), Math.abs(velocity.second));
            if (Math.abs(maxDelta) < MIN_DIRECTION_SPEED) {
                multi *= MIN_DIRECTION_SPEED / Math.abs(maxDelta);
            }
            float dx = velocity.first * multi;
            float dy = velocity.second * multi;
            goAway(dx, dy);
            notifyStatus(dx >= 0);
            isGoAway = true;
        } else if (isCloseToBorder()) {

            float dx = 15 * getSign(getTranslationX());
            float dy = 15 * getSign(getTranslationY());
            goAway(dx, dy);
            notifyStatus(dx >= 0);
            isGoAway = true;
        } else {
            resetTranslation();
        }
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private void notifyStatus(boolean isToRight) {
        if (statusListener != null) {
            statusListener.onMoveAway(isToRight);
        }
    }

    private int getSign(float value) {
        return value >= 0 ? 1 : -1;
    }

    private boolean isCloseToBorder() {
        return (getWidth() - Math.abs(getTranslationX()) <= MIN_BORDER_WIDTH) ||
            (getHeight() - Math.abs(getTranslationY()) <= MIN_BORDER_WIDTH);
    }

    private void goAway(final float dx, final float dy) {
        if (!isAnimating) {
            return;
        }

        setTranslationX(getTranslationX() + dx);
        setTranslationY(getTranslationY() + dy);

        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goAway(dx, dy);
            }
        }, FRAME_DELAY_MILLIS);
    }

    private void resetTranslation() {
        if (!isAnimating) {
            return;
        }

        float dx = Math.abs(getTranslationX() * 0.15f) + 1.0f;
        float dy = Math.abs(getTranslationY() * 0.15f) + 1.0f;
        if (dx <= MIN_TRANSLATION_TO_NORMAL && dy <= MIN_TRANSLATION_TO_NORMAL) {
            return;
        }

        if (getTranslationX() < 0) {
            dx *= -1;
        }
        if (getTranslationY() < 0) {
            dy *= -1;
        }
        setTranslationX(getTranslationX() - dx);
        setTranslationY(getTranslationY() - dy);

        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetTranslation();
            }
        }, FRAME_DELAY_MILLIS);
        //Log.d("chentian", Math.round(getTranslationX()) + ", " + Math.round(getTranslationY()));
    }
}
