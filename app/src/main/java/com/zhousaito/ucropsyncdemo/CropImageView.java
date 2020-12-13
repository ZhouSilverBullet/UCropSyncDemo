package com.zhousaito.ucropsyncdemo;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Author zhouzhou
 * @Date :12/13/20
 * @Version :1.0
 * @Brief :
 */

public class CropImageView extends TransformImageView {

    protected float mMaxScale;
    protected float mMinScale;
    private float mUsedScale = 10f;

    protected float mRatio;
    private RectF mCropRect = new RectF();


    public CropImageView(@NonNull Context context) {
        this(context, null);
    }

    public CropImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();

        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType);
        } else {
            Log.w(TAG, "setScaleType is Matrix can Edit !!");
        }
    }

    @Override
    protected void layouted() {
        super.layouted();
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        float drawableWidth = drawable.getIntrinsicWidth();
        float drawableHeight = drawable.getIntrinsicHeight();

        //根据缩放倍率显示图片 获取CropRect
        if (mRatio == 0f) {
            mRatio = drawableWidth / drawableHeight;
        }

        float currentHeight = mThisWidth / mRatio;

        if (currentHeight < mThisHeight) { //高度够,宽度为图片展示宽度
            float diffHeight = (mThisHeight - currentHeight) / 2f;
            mCropRect.set(0, diffHeight, mThisWidth, mThisHeight - diffHeight);
        } else {
            float currentWith = mThisWidth * mRatio;
            float diffWidth = (mThisWidth - currentWith) / 2f;
            mCropRect.set(diffWidth, 0, mThisWidth - diffWidth, mThisHeight);
        }

        calcImageScaleBounds(drawableWidth, drawableHeight);
        setImageInitialPosition(drawableWidth, drawableHeight);
    }


    /**
     * 计算缩放界限
     *
     * @param drawableWidth
     * @param drawableHeight
     */
    private void calcImageScaleBounds(float drawableWidth, float drawableHeight) {
        float withScale = Math.min(mCropRect.width() / drawableWidth, mCropRect.width() / drawableHeight);
        float heightScale = Math.min(mCropRect.height() / drawableWidth, mCropRect.height() / drawableHeight);

        mMinScale = Math.min(withScale, heightScale);
        mMaxScale = mMinScale * mUsedScale;
    }


    private void setImageInitialPosition(float drawableWidth, float drawableHeight) {
        float cropWith = mCropRect.width();
        float cropHeight = mCropRect.height();

        //1. 计算好缩放撑满框的最小大小
        float initialMinScale = Math.max(cropWith / drawableWidth, cropHeight / drawableHeight);

        //2. 计算好平移距离
        float tw = (mCropRect.width() - drawableWidth * initialMinScale) / 2f + mCropRect.left;
        float th = (mCropRect.height() - drawableHeight * initialMinScale) / 2f + mCropRect.top;

        mCurrentImageMatrix.reset();
        mCurrentImageMatrix.postScale(initialMinScale, initialMinScale);
        mCurrentImageMatrix.postTranslate(tw, th);

        setImageMatrix(mCurrentImageMatrix);
    }
}
