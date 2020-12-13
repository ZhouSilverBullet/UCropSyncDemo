package com.zhousaito.ucropsyncdemo;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;

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

    private Matrix mTempMatrix = new Matrix();

    private ZoomInImageRunnable mZoomInImageRunnable;


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

    public void setCropRect(RectF cropRect) {
        mRatio = cropRect.width() / cropRect.height();
        mCropRect.set(cropRect.left - getPaddingLeft(), cropRect.top - getPaddingTop(),
                cropRect.right - getPaddingRight(), cropRect.bottom - getPaddingBottom());
        
        Drawable drawable = getDrawable();
        if (drawable != null) {
            calcImageScaleBounds(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        setImageToWrapCropBounds();
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

    /**
     * 点击放大，放大动画使用这个
     *
     * @param px
     * @param py
     */
    protected void zoomImageToPosition(float scale, float px, float py, int duration) {
        if (scale > mMaxScale) {
            scale = mMaxScale;
        }
        float oldScale = getCurrentScale();
        float deltaScale = scale - oldScale;

        mZoomInImageRunnable = new ZoomInImageRunnable(this, duration,
                deltaScale, oldScale, px, py);
        post(mZoomInImageRunnable);
    }

    public void zoomInImage(float scale, float px, float py) {
        if (scale < getMaxScale()) {
            postScale(scale / getCurrentScale(), px, py);
        }
    }

    protected void cancelAllAnimations() {
        removeCallbacks(mZoomInImageRunnable);
    }

    protected void setImageToWrapCropBounds() {
        setImageToWrapCropBounds(true);
    }

    /**
     * 这里要进行计算，
     * 开始的坐标  ---转换---> 符合标准的坐标
     *
     * @param isAnimation
     */
    protected void setImageToWrapCropBounds(boolean isAnimation) {


//        //假设在偏移在右边
//        float dx = mCropRect.centerX() - mCurrentImageCenter[0];
//        float dy = mCropRect.centerY() - mCurrentImageCenter[1];

//        mTempMatrix.reset();
//
//        Log.i(TAG, "dx: " + dx + ", dy: " + dy + ", scale: " + getCurrentScale());
//        postTranslate(dx, dy);

        float currentX = mCurrentImageCenter[0];
        float currentY = mCurrentImageCenter[1];
        float currentScale = getCurrentScale();

        float deltaX = mCropRect.centerX() - currentX;
        float deltaY = mCropRect.centerY() - currentY;
        float deltaScale = 0f;

        //计算好，移动后当前的Rect是否包含在初始化Rect的里面
        mTempMatrix.reset();
        mTempMatrix.postTranslate(deltaX, deltaY);
        //复制一份当前的值
        float[] mTempImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
        mTempMatrix.mapPoints(mTempImageCorners);

        boolean isWrapperInCropBounds = isWrapperInCropBounds(mTempImageCorners);
        Log.i(TAG, "isWrapperInCropBounds: " + isWrapperInCropBounds);
        if (isWrapperInCropBounds) { //这个不用涉及到缩放的时候，只要平移就能解决问题的
            float[] floats = calcOtherArea();
            deltaX = -(floats[0] + floats[2]);
            deltaY = -(floats[1] + floats[3]);
        } else {
            RectF tempCropRect = new RectF(mCropRect);

//            mTempMatrix.reset();
//            mTempMatrix.mapRect(tempCropRect);
            float with = mCurrentImageCorners[2] - mCurrentImageCorners[0];
            float height = mCurrentImageCorners[5] - mCurrentImageCorners[1];

            deltaScale = Math.max(tempCropRect.width() / with, tempCropRect.height() / height);
            deltaScale = deltaScale * currentScale - currentScale;
        }

        if (isAnimation) {
            post(new ImageWrapCropBoundsRunnable(this, 500,
                    currentScale, deltaScale, currentX, currentY, deltaX, deltaY, isWrapperInCropBounds));
        } else {
            postTranslate(deltaX, deltaY);
            if (!isWrapperInCropBounds) {
                zoomInImage(currentScale + deltaScale, mCropRect.centerX(), mCropRect.centerY());
            }
        }
    }

    private float[] calcOtherArea() {
        mTempMatrix.reset();

        float[] currentImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
        float[] corpImageCorners = RectUtils.createCorners(mCropRect);

        mTempMatrix.mapPoints(currentImageCorners);
        mTempMatrix.mapPoints(corpImageCorners);

        RectF currentRect = RectUtils.array2Rect(currentImageCorners);
        RectF cropRect = RectUtils.array2Rect(corpImageCorners);

        float deltaLeft = currentRect.left - cropRect.left;
        float deltaTop = currentRect.top - cropRect.top;
        float deltaRight = currentRect.right - cropRect.right;
        float deltaBottom = currentRect.bottom - cropRect.bottom;

        float[] floats = new float[4];
        floats[0] = deltaLeft > 0 ? deltaLeft : 0;
        floats[1] = deltaTop > 0 ? deltaTop : 0;
        floats[2] = deltaRight < 0 ? deltaRight : 0;
        floats[3] = deltaBottom < 0 ? deltaBottom : 0;

        Log.i(TAG, "floats before: " + Arrays.toString(floats));

//        mTempMatrix.reset();
//        mTempMatrix.mapPoints(floats);
//        Log.i(TAG, "floats end: "+ Arrays.toString(floats));

        return floats;
    }

    public boolean isImageWrapCropBounds() {
        return isWrapperInCropBounds(mCurrentImageCorners);
    }

    private boolean isWrapperInCropBounds(float[] imageCorners) {
//        float[] corners = RectUtils.createCorners(mCropRect);
        mTempMatrix.reset();

        float[] tempImageCorners = Arrays.copyOf(imageCorners, imageCorners.length);
        mTempMatrix.mapPoints(tempImageCorners);
        float[] corpImageCorners = RectUtils.createCorners(mCropRect);
        mTempMatrix.mapPoints(corpImageCorners);

        RectF tempRect = RectUtils.trapToRect(tempImageCorners);
        RectF corpRect = RectUtils.trapToRect(corpImageCorners);
//        RectF rectF1 = RectUtils.trapToRect(tempImageCorners);
        Log.i(TAG, "tempRect: " + tempRect);
        Log.i(TAG, "corpRect: " + corpRect);
//        RectF rectF = RectUtils.array2Rect(tempImageCorners);
        return tempRect.contains(corpRect);
    }

    @Override
    protected void postScale(float scaleFactor, float px, float py) {
        super.postScale(scaleFactor, px, py);
    }

    public float getMaxScale() {
        return mMaxScale;
    }

    public float getMinScale() {
        return mMinScale;
    }

    private static class ZoomInImageRunnable implements Runnable {

        private WeakReference<CropImageView> mCropImageViewDef;
        //执行的总时长
        private long mDurationMs;
        //开始执行的时间
        private long mStartTime;
        //当前需要缩放的大小
        private float mDeltaScale;
        //老的那一次缩放
        private float mOldScale;

        //缩放中心点
        private float mCenterX;
        private float mCenterY;

        public ZoomInImageRunnable(CropImageView cropImageView,
                                   long durationMs,
                                   float deltaScale,
                                   float oldScale,
                                   float centerX,
                                   float centerY) {

            mCropImageViewDef = new WeakReference<>(cropImageView);
            mDurationMs = durationMs;
            mDeltaScale = deltaScale;
            mOldScale = oldScale;
            mCenterX = centerX;
            mCenterY = centerY;

            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            CropImageView cropImageView = mCropImageViewDef.get();
            if (cropImageView == null) {
                return;
            }

            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);
            float newScale = CubicEasing.easeInOut(currentMs, 0, mDeltaScale, mDurationMs);

            if (currentMs < mDurationMs) {
                cropImageView.zoomInImage(mOldScale + newScale, mCenterX, mCenterY);
                cropImageView.post(this);
            } else {
                cropImageView.setImageToWrapCropBounds();
            }

        }
    }

    private static class ImageWrapCropBoundsRunnable implements Runnable {
        private WeakReference<CropImageView> mCropImageView;

        private long mDurationMs;
        private float mCurrentScale;
        private float mDeltaScale;

        private float mCurrentX;
        private float mCurrentY;

        private float mDeltaX;
        private float mDeltaY;

        //是否只是平移
        private boolean isWrapperInCropBounds;

        private long mStartTime;


        public ImageWrapCropBoundsRunnable(CropImageView cropImageView, long durationMs,
                                           float currentScale, float deltaScale,
                                           float currentX, float currentY,
                                           float deltaX, float deltaY,
                                           boolean isWrapperInCropBounds) {
            mCropImageView = new WeakReference<>(cropImageView);
            mDurationMs = durationMs;
            mCurrentScale = currentScale;
            mDeltaScale = deltaScale;
            mDeltaX = deltaX;
            mDeltaY = deltaY;
            this.mCurrentX = currentX;
            this.mCurrentY = currentY;
            this.isWrapperInCropBounds = isWrapperInCropBounds;

            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            CropImageView cropImageView = mCropImageView.get();
            if (cropImageView == null) {
                return;
            }
            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);

            float newX = CubicEasing.easeOut(currentMs, 0, mDeltaX, mDurationMs);
            float newY = CubicEasing.easeOut(currentMs, 0, mDeltaY, mDurationMs);
            float newScale = CubicEasing.easeInOut(currentMs, 0, mDeltaScale, mDurationMs);

            if (currentMs < mDurationMs) {
                cropImageView.postTranslate(newX - (cropImageView.mCurrentImageCenter[0] - mCurrentX), newY - (cropImageView.mCurrentImageCenter[1] - mCurrentY));
                if (!isWrapperInCropBounds) {
                    cropImageView.zoomInImage(mCurrentScale + newScale, cropImageView.mCropRect.centerX(), cropImageView.mCropRect.centerY());
                }
                if (!cropImageView.isImageWrapCropBounds()) {
                    cropImageView.post(this);
                }
            }

        }
    }
}
