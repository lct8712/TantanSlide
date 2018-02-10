package com.chentian.tantanslide.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.chentian.tantanslide.R;

/**
 * 自己绘制一个图案，方便 Demo
 *
 * @author chentian
 */
public class TestSlidableImage extends SlidableImage {

    private Paint textPaint;
    private String id;
    private Rect textBounds;

    public TestSlidableImage(Context context) {
        this(context, null);
    }

    public TestSlidableImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestSlidableImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.window_background));
        textBounds = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        textPaint.setTextSize(getMeasuredWidth() / 10);
        textPaint.getTextBounds(id, 0, id.length(), textBounds);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(id, (getWidth() - textBounds.width()) / 2, (getHeight() + textBounds.height()) / 2, textPaint);
    }

    public void setId(String id) {
        this.id = id;
    }
}
