package com.zhousaito.ucropsyncdemo;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @Author zhouzhou
 * @Date :12/13/20
 * @Version :1.0
 * @Brief :
 */

public class TransformImageView extends AppCompatImageView {
    protected String TAG = getClass().getName();

    //当前在变换的Matrix，用户看到的
    protected Matrix mCurrentImageMatrix = new Matrix();
    protected float mThisWidth;
    protected float mThisHeight;

    protected float[] mValues = new float[9];


    public TransformImageView(@NonNull Context context) {
        this(context, null);
    }

    public TransformImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransformImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    protected void init() {

    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        mCurrentImageMatrix.set(matrix);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {

            mThisWidth = right - left;
            mThisHeight = bottom - top;

            layouted();
        }
    }

    protected void layouted() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

    }

    protected void postTranslate(float translateX, float translateY) {
        mCurrentImageMatrix.postTranslate(translateX, translateY);
        setImageMatrix(mCurrentImageMatrix);
    }

    protected void postScale(float scaleFactor, float px, float py) {
        mCurrentImageMatrix.postScale(scaleFactor, scaleFactor, px, py);
        setImageMatrix(mCurrentImageMatrix);
    }


    protected float getCurrentScale() {
        return getCurrentScale(mCurrentImageMatrix);
    }

    private float getCurrentScale(Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValues(matrix, Matrix.MSCALE_X), 2) +
                Math.pow(getMatrixValues(matrix, Matrix.MSKEW_Y), 2));
    }

    private double getMatrixValues(Matrix matrix, int index) {
        matrix.getValues(mValues);
        return mValues[index];
    }
}
