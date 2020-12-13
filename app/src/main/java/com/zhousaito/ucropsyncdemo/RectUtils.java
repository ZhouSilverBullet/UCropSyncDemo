package com.zhousaito.ucropsyncdemo;

import android.graphics.RectF;

/**
 * @Author zhouzhou
 * @Date :12/13/20
 * @Version :1.0
 * @Brief :
 */

public class RectUtils {

    public static float[] createCorners(RectF rect) {
        return new float[]{rect.left, rect.top,
                rect.right, rect.top,
                rect.right, rect.bottom,
                rect.left, rect.bottom};
    }

    public static float[] createCenter(RectF rect) {
        return new float[]{rect.centerX(), rect.centerY()};
    }

    public static RectF array2Rect(float[] corners) {
        RectF rectF = new RectF(corners[0], corners[1], corners[2], corners[5]);
        return rectF;
    }

    public static RectF trapToRect(float[] array) {
        RectF r = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = Math.round(array[i - 1] * 10) / 10.f;
            float y = Math.round(array[i] * 10) / 10.f;
            r.left = (x < r.left) ? x : r.left;
            r.top = (y < r.top) ? y : r.top;
            r.right = (x > r.right) ? x : r.right;
            r.bottom = (y > r.bottom) ? y : r.bottom;
        }
        r.sort();
        return r;
    }

}
