package com.huweiqiang.circleprogressbar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
    private static final String SUFFIX = "%";
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_PROGRESS = 0;

    private float radius;
    private float centerX;
    private float centerY;

    private Paint circlePaint;
    private Paint textPaint;
    private RectF arcRectF;
    private GestureDetector gestureDetector;

    private int max = DEFAULT_MAX;
    private int progress = DEFAULT_PROGRESS;
    private int color = DEFAULT_COLOR;
    private float textSize = DEFAULT_TEXT_SIZE;
    private float circleWidth = DEFAULT_CIRCLE_WIDTH;

    private double oldRatio;
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
        if (progress < 0) {
            progress = 0;
        }
        if (progress > max) {
            progress = max;
        }
        this.progress = progress;
        oldRatio = computeRatio(this.progress, max);
        postInvalidate();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);

        max = typedArray.getInteger(R.styleable.CircleProgressBar_max, DEFAULT_MAX);
        if (max < 0) {
            max = 0;
        }

        setProgress(typedArray.getInteger(R.styleable.CircleProgressBar_progress, DEFAULT_PROGRESS));
        color = typedArray.getColor(R.styleable.CircleProgressBar_color, DEFAULT_COLOR);
        textSize = typedArray.getDimensionPixelSize(R.styleable.CircleProgressBar_text_size, DEFAULT_TEXT_SIZE);
        circleWidth = typedArray.getDimensionPixelSize(R.styleable.CircleProgressBar_circle_width, DEFAULT_CIRCLE_WIDTH);
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

        drawCircle(canvas, circleWidth, 270, computeRatio(progress, max) * 360 / 100, circlePaint);

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
        float suffixWidth = textPaint.measureText(SUFFIX);

        textPaint.setTextSize(textSize);
        String text = String.valueOf(computeRatio(progress, max));
        float textWidth = textPaint.measureText(text);

        float x = centerX - (textWidth + suffixWidth) / 2;
        float y = centerY - (textPaint.ascent() + textPaint.descent()) / 2;
        canvas.drawText(text, x, y, textPaint);

        textPaint.setTextSize(textSize / 2);
        canvas.drawText(SUFFIX, x + textWidth + Utils.dp2px(2), y, textPaint);
    }

    /**
     * 1.快速滑动时校正 （快速滑动时有可能不能到达 100 和 0）
     * 2.到达 100 之后不能再顺时针滑动增加
     * 3.到达 0 之后不能逆时针减少
     *
     * @param startX startX
     * @param startY startY
     * @param endX   endX
     * @param endY   endY
     */
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

        boolean isClockwise = Utils.isClockwise(startX, startY, endX, endY, centerX, centerY);

        if (oldRatio >= 75 && isClockwise && degree < 90) {
            degree = 360;
        }

        if (oldRatio <= 25 && !isClockwise && degree > 270) {
            degree = 0;
        }

        if ((oldRatio >= 75 && isClockwise && degree >= 0 && degree < 90) ||
                (oldRatio == 100 && isClockwise) ||
                (oldRatio <= 25 && !isClockwise && degree > 270 && degree <= 360) ||
                (oldRatio == 0 && !isClockwise) ||
                (oldRatio == 100 && degree >= 0 && degree < 270) ||
                (oldRatio == 0 && degree >= 90)) {
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

    private int computeRatio(int progress, int max) {
        return (int) ((double) progress / (double) max * 100);
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
