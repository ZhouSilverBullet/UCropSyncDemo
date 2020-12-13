package com.zhousaito.ucropsyncdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private GestureImageView mCropImageView;
    private CropView mCropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCropImageView = findViewById(R.id.cropImageView);
        mCropView = findViewById(R.id.cropView);

        mCropView.post(() -> {
            int padding = 200;

            RectF rectF = new RectF(padding, padding, mCropView.getWidth() - padding, mCropView.getHeight() - padding);
            mCropView.setCropRectF(rectF);

            mCropImageView.setCropRect(rectF);
        });

    }
}