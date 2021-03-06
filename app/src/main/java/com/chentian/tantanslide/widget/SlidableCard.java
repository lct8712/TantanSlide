package com.chentian.tantanslide.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * 可被左滑右划的卡片
 *
 * @author chentian
 */
public class SlidableCard extends FrameLayout {

    private static final float[] STACK_SCALE_LIST = new float[] { 1f, 0.95f, 0.925f, 0.9f };
    private static final float[] STACK_TRANSLATE_LIST = new float[] { 0f, 4f, 8f, 12f };
    private static final long FRAME_DELAY_MILLIS = 16L;

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

    private float lastX;
    private float lastY;
    private float eventDownY;

    private int stackPosition;
    private int originHeight;
    private boolean isAnimating;
    private boolean isGoAway;
    private Handler mainThreadHandler;
    private VelocityHelper velocityHelper;

    private StatusListener statusListener;

    public SlidableCard(Context context) {
        this(context, null);
    }

    public SlidableCard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidableCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mainThreadHandler = new Handler(Looper.getMainLooper());
        velocityHelper = new VelocityHelper();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        originHeight = MeasureSpec.getSize(heightMeasureSpec);
        updateStackPosition(0f);
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

    /**
     * 处理被动的移动
     * 即当前卡片不在栈顶时，栈顶卡片移动之后的联动
     */
    public void handlePassiveMove(float translationX, float translationY) {
        float translation = (float) Math.sqrt(translationX * translationX + translationY * translationY);
        updateStackPosition(translation);
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void destroy() {
        this.statusListener = null;
    }

    /**
     * 设置在栈的第几个位置
     * 0 表示栈顶
     */
    public void setStackPosition(int stackPosition) {
        this.stackPosition = stackPosition;
    }

    private void updateStackPosition(float translation) {
        if (isAnimating || originHeight <= 0) {
            return;
        }

        float percent = Math.abs(translation) * 2f / originHeight;
        stackPosition = Math.min(stackPosition, STACK_SCALE_LIST.length - 1);
        float scale = STACK_SCALE_LIST[stackPosition];
        scale = Math.min(1.0f, scale + percent / 20);
        scale = Math.min(scale, stackPosition > 0 ? STACK_SCALE_LIST[stackPosition - 1] : 1f);
        setTranslationY(originHeight * (1f - scale) / 2f + STACK_TRANSLATE_LIST[stackPosition]);
        setScaleX(scale);
        setScaleY(scale);
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
            // 正常的划走
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
            // 靠近边缘后，即使停住，也会划走
            float dx = 15f * getSign(getTranslationX());
            float dy = 15f * getSign(getTranslationY());
            goAway(dx, dy);
            notifyGoAway(dx >= 0);
            isGoAway = true;

        } else {
            // 回弹到原位
            backToCenter(0f, 0f);
        }
    }

    private int getSign(float value) {
        return value >= 0 ? 1 : -1;
    }

    private boolean isCloseToBorder() {
        final float mainContentPercent = 0.7f;
        return (Math.abs(getTranslationX()) / getWidth() > mainContentPercent) ||
            (Math.abs(getTranslationY() / getHeight()) > mainContentPercent);
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

    private void backToCenter(final float vx, final float vy) {
        if (!isAnimating) {
            return;
        }

        final float minTranslation = 1.1f;
        final float minVelocity = 1.5f;
        if (Math.abs(getTranslationX()) < minTranslation && Math.abs(getTranslationY()) < minTranslation &&
            Math.abs(vx) < minVelocity && Math.abs(vy) < minVelocity) {
            return;
        }

        setTranslationX(getTranslationX() + vx);
        setTranslationY(getTranslationY() + vy);

        updateRotation();
        notifyOnMove();

        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // acceleratedFactor 越小，速度越快；frictionFactor 越小，阻力越大
                final float acceleratedFactor = 18f;
                final float frictionFactor = 0.75f;
                float ax = -getTranslationX() / acceleratedFactor;
                float ay = -getTranslationY() / acceleratedFactor;
                backToCenter((vx + ax) * frictionFactor, (vy + ay) * frictionFactor);
            }
        }, FRAME_DELAY_MILLIS);
    }

    private void updateRotation() {
        if (Math.abs(getTranslationX()) <= 0) {
            return;
        }

        float rotation = getTranslationX() / getWidth() / 2 * 10;
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
