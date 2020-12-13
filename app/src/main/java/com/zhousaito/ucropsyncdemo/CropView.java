package com.zhousaito.ucropsyncdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @Author zhouzhou
 * @Date :12/11/20
 * @Version :1.0
 * @Brief :
 */

public class CropView extends View {

    private final Paint mPaint;
    private int mBorderWidth = 2;
    private RectF mCropRect = new RectF();
    private int mBorderColor = Color.parseColor("#FFFFFF"); //边框的颜色
    private int mBgdColor = Color.parseColor("#99111111");  //周边区域的颜色

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mBgdColor);
        mPaint.setStyle(Paint.Style.FILL);
        //左边
        canvas.drawRect(0, 0, mCropRect.left, getHeight(), mPaint);
        //右边
        canvas.drawRect(mCropRect.right, 0, getWidth(), getHeight(), mPaint);
        //上边
        canvas.drawRect(mCropRect.left, 0, mCropRect.right, mCropRect.top, mPaint);
        //下边
        canvas.drawRect(mCropRect.left, mCropRect.bottom, mCropRect.right, getHeight(), mPaint);

        //画线
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        canvas.drawRect(mCropRect, mPaint);
    }

    public void setCropRectF(RectF rectF) {
        mCropRect.set(rectF.left, rectF.top, rectF.right, rectF.bottom);
        invalidate();
    }
}
