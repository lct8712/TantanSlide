package com.chentian.tantanslide.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.chentian.tantanslide.R;
import com.chentian.tantanslide.data.ColorProvider;

/**
 * 自己绘制一个图案，方便 Demo
 *
 * @author chentian
 */
public class TestImageContent extends AppCompatImageView {

    private Paint textPaint;
    private String id;
    private Rect textBounds;

    public TestImageContent(Context context) {
        this(context, null);
    }

    public TestImageContent(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestImageContent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initDrawable();

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.window_background));
        textBounds = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, (int) (size * 1.4));

        textPaint.setTextSize(getMeasuredWidth() / 10);
        textPaint.getTextBounds(id, 0, id.length(), textBounds);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(id, (getWidth() - textBounds.width()) / 2, (getHeight() + textBounds.height()) / 2, textPaint);
    }

    private void initDrawable() {
        setBackgroundResource(R.drawable.photo_with_frame);
        GradientDrawable shapeDrawable = (GradientDrawable) getBackground();
        shapeDrawable.setColor(ColorProvider.getInstance(getContext()).getNextColor());
        shapeDrawable.setSize(500, 500);

        id = ColorProvider.getInstance(getContext()).getNextId();
    }
}
