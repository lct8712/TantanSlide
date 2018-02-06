package com.chentian.tantanslide.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.chentian.tantanslide.R;

/**
 * @author chentian
 */
public class SlidableImage extends AppCompatImageView {

    private float lastX;
    private float lastY;

    public SlidableImage(Context context) {
        this(context, null);
    }

    public SlidableImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidableImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundResource(R.drawable.photo_with_frame);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("chentian", "x: " + event.getX());
        Log.d("chentian", "y: " + event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                setTranslationX(getTranslationX() + event.getX() - lastX);
                setTranslationY(getTranslationY() + event.getY() - lastY);
                lastX = event.getX();
                lastY = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }
}
