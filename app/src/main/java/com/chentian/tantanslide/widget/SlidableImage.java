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
 * 可被左滑右划的 ImageView
 *
 * @author chentian
 */
public class SlidableImage extends AppCompatImageView {

    public interface StatusListener {

        /**
         * 被滑动时触发，用来让第二个图片联动
         */
        void onMove(float translationX, float translationY);

        /**
         * 被划出
         *
         * @param isToRight 划出的方向是向左还是向右
         */
        void onGoAway(boolean isToRight);
    }

    private static final long FRAME_DELAY_MILLIS = 16L;

    private float lastX;
    private float lastY;
    private float eventDownY;

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

    public void handlePassiveMove(float translationX, float translationY) {
        setTranslationX(-translationX / 15);
        setTranslationY(-translationY / 40);
    }

    public void resetPositionWithAnmi() {
        isAnimating = true;
        backToCenter();
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void destroy() {
        this.statusListener = null;
    }

    private void handleEventDown(MotionEvent event) {
        isAnimating = false;
        lastX = event.getRawX();
        lastY = event.getRawY();
        eventDownY = event.getY();
        velocityHelper.reset();
        velocityHelper.record(event.getRawX(), event.getRawY());
    }

    private void handleEventMove(MotionEvent event) {
        isAnimating = false;
        float dx = event.getRawX() - lastX;
        float dy = event.getRawY() - lastY;
        final int minMove = 2;
        if (Math.abs(dx) < minMove && Math.abs(dy) < minMove) {
            return;
        }

        // translation
        setTranslationX(getTranslationX() + dx);
        setTranslationY(getTranslationY() + dy);
        lastX = event.getRawX();
        lastY = event.getRawY();
        velocityHelper.record(event.getRawX(), event.getRawY());
        notifyOnMove();

        // rotate
        updateRotation();
    }

    private void handleEventUp(MotionEvent event) {
        isAnimating = true;
        velocityHelper.record(event.getRawX(), event.getRawY());
        final float minSpeedForGoAway = 2f;
        if (velocityHelper.computeVelocity() >= minSpeedForGoAway) {
            Pair<Float, Float> velocity = velocityHelper.computeVelocityWithDirection();
            float multi = 15f;
            float maxDelta = Math.max(Math.abs(velocity.first), Math.abs(velocity.second));
            final float minDirectionSpeed = 5f;
            if (Math.abs(maxDelta) < minDirectionSpeed) {
                multi *= minDirectionSpeed / Math.abs(maxDelta);
            }
            float dx = velocity.first * multi;
            float dy = velocity.second * multi;
            goAway(dx, dy);
            notifyGoAway(dx >= 0);
            isGoAway = true;

        } else if (isCloseToBorder()) {
            float dx = 15 * getSign(getTranslationX());
            float dy = 15 * getSign(getTranslationY());
            goAway(dx, dy);
            notifyGoAway(dx >= 0);
            isGoAway = true;

        } else {
            backToCenter();
        }
    }

    private int getSign(float value) {
        return value >= 0 ? 1 : -1;
    }

    private boolean isCloseToBorder() {
        final int minBorderWidth = 250;
        return (getWidth() - Math.abs(getTranslationX()) <= minBorderWidth) ||
            (getHeight() - Math.abs(getTranslationY()) <= minBorderWidth);
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

    private void backToCenter() {
        if (!isAnimating) {
            return;
        }

        float dx = Math.abs(getTranslationX() * 0.15f) + 1.0f;
        float dy = Math.abs(getTranslationY() * 0.15f) + 1.0f;
        final float minTranslationToNormal = 1.1f;
        if (dx <= minTranslationToNormal && dy <= minTranslationToNormal) {
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

        updateRotation();
        notifyOnMove();

        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backToCenter();
            }
        }, FRAME_DELAY_MILLIS);
    }

    private void updateRotation() {
        if (Math.abs(getTranslationX()) <= 0) {
            return;
        }

        float rotation = getTranslationX() / getWidth() / 2 * 30;
        if (eventDownY > getHeight() / 2) {
            rotation *= -1;
        }
        setRotation(rotation);
    }

    private void notifyOnMove() {
        if (statusListener != null) {
            statusListener.onMove(getTranslationX(), getTranslationY());
        }
    }

    private void notifyGoAway(boolean isToRight) {
        if (statusListener != null) {
            statusListener.onGoAway(isToRight);
        }
    }
}
