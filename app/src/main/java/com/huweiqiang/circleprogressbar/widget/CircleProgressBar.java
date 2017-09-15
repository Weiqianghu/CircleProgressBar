package com.huweiqiang.circleprogressbar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.huweiqiang.circleprogressbar.R;

/**
 * Created by huweiqiang on 2017/9/15.
 */

public class CircleProgressBar extends View implements GestureDetector.OnGestureListener {
    private static final int DEFAULT_HEIGHT = (int) Utils.dp2px(100);
    private static final int DEFAULT_WIDTH = (int) Utils.dp2px(100);
    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_TEXT_SIZE = (int) Utils.sp2px(20);
    private static final int DEFAULT_CIRCLE_WIDTH = (int) Utils.dp2px(6);

    private String suffix = "%";

    private float radius = 0;
    private float centerX;
    private float centerY;

    private Paint circlePaint;
    private Paint textPaint;
    private RectF arcRectF;
    private GestureDetector gestureDetector;

    private int max = 100;
    private int progress = 68;
    private int color = DEFAULT_COLOR;
    private float textSize = DEFAULT_TEXT_SIZE;
    private float circleWidth = DEFAULT_CIRCLE_WIDTH;

    private double oldDegree = 0;
    private float oldX;
    private float oldY;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        oldDegree = progress * 360 / max;
        postInvalidate();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);

        max = typedArray.getInteger(R.styleable.CircleProgressBar_max, 100);
        if (max < 0) {
            max = 0;
        }

        progress = typedArray.getInteger(R.styleable.CircleProgressBar_progress, 0);
        if (progress < 0) {
            progress = 0;
        }
        if (progress > max) {
            progress = max;
        }
        setProgress(progress);
        color = typedArray.getColor(R.styleable.CircleProgressBar_color, DEFAULT_COLOR);
        textSize = typedArray.getDimensionPixelSize(R.styleable.CircleProgressBar_textSize, DEFAULT_TEXT_SIZE);
        circleWidth = typedArray.getDimensionPixelSize(R.styleable.CircleProgressBar_circleWidth, DEFAULT_CIRCLE_WIDTH);
        typedArray.recycle();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        circlePaint.setStrokeWidth(circleWidth);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circlePaint.setColor(color);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);

        arcRectF = new RectF();

        gestureDetector = new GestureDetector(getContext(), this);
        gestureDetector.setIsLongpressEnabled(false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultWidthMeasureSpec = widthMeasureSpec;
        int resultHeightMeasureSpec = heightMeasureSpec;

        if (widthMode == MeasureSpec.AT_MOST) {
            resultWidthMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_WIDTH, MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            resultHeightMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT, MeasureSpec.EXACTLY);
        }

        super.onMeasure(resultWidthMeasureSpec, resultHeightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int weight, int height, int oldWeight, int oldHeight) {
        super.onSizeChanged(weight, height, oldWeight, oldHeight);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        radius = Math.min(height - paddingTop - paddingBottom, weight - paddingLeft - paddingRight) / 2 - circleWidth;

        centerX = weight / 2;
        centerY = height / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBgCircle(canvas);

        drawCircle(canvas, circleWidth, 270, progress * 360 / max, circlePaint);

        drawText(canvas);
    }

    private void drawBgCircle(Canvas canvas) {
        drawCircle(canvas, 2, 0, 360, circlePaint);
    }

    private void drawCircle(Canvas canvas, float circleWidth, int startAngle, float sweepAngle, Paint circlePaint) {
        circlePaint.setStrokeWidth(circleWidth);
        arcRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(arcRectF, startAngle, sweepAngle, false, circlePaint);
    }

    private void drawText(Canvas canvas) {
        textPaint.setTextSize(textSize / 2);
        float suffixWidth = textPaint.measureText(suffix);

        textPaint.setTextSize(textSize);
        String text = String.valueOf((int) (((double) progress / (double) max) * 100));
        float textWidth = textPaint.measureText(text);

        float x = centerX - (textWidth + suffixWidth) / 2;
        float y = centerY - (textPaint.ascent() + textPaint.descent()) / 2;
        canvas.drawText(text, x, y, textPaint);

        textPaint.setTextSize(textSize / 2);
        canvas.drawText(suffix, x + textWidth + Utils.dp2px(2), y, textPaint);
    }

    private void move(float startX, float startY, float endX, float endY) {
        double degree = -1;

        if (endX >= centerX && endY < centerY) {
            degree = computeDegree(endX - centerX, centerY - endY);
        } else if (endX >= centerX && endY > centerY) {
            degree = computeDegree(endY - centerY, endX - centerX) + 90;
        } else if (endX <= centerX && endY > centerY) {
            degree = computeDegree(centerX - endX, endY - centerY) + 180;
        } else if (endX <= centerX && endY < centerY) {
            degree = computeDegree(centerY - endY, centerX - endX) + 270;
        }

        if (oldDegree > 270 && Math.abs(endX - centerX) < 1 && endY < centerY) {
            degree = 360;
        }

        boolean isClockwise = Utils.isClockwise(startX, startY, endX, endY, centerX, centerY);
        if ((oldDegree == 360 && isClockwise) || (oldDegree == 0 && !isClockwise)
                || (oldDegree == 360 && degree >= 0 && degree < 270 && !isClockwise)
                || (oldDegree == 0 && degree >= 90 && isClockwise)) {
            return;
        }

        if (degree == -1) {
            return;
        }
        setProgress((int) (degree * max / 360));
    }

    private double computeDegree(float opposite, float adjacent) {
        return Math.atan(opposite / adjacent) * 180 / Math.PI;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        oldX = e.getX();
        oldY = e.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        move(oldX, oldY, e2.getX(), e2.getY());

        oldX = e2.getX();
        oldY = e2.getY();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
