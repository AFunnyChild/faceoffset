/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faceDemo.currencyview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.faceDemo.activity.CameraActivity;

import java.io.IOException;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitTextureView extends TextureView {
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public AutoFitTextureView(final Context context) {
        this(context, null);

    }

    public AutoFitTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public AutoFitTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
     * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels;

        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
//            setMeasuredDimension(width, height);
//            Matrix fullScreenTransform = new Matrix();
//            getTransform(fullScreenTransform).reset();
//            fullScreenTransform.postScale((float) height / ratioHeight,
//                    1f, width * 0.5f, height * 0.5f);
//            setTransform(fullScreenTransform);
//            Log.d("setMeasuredDimension", "width: " + width + ":height:" + height + ":ratioHeight" + ratioHeight + ":ratioWeight:" + ratioWidth);
            if (width < height * ratioWidth / ratioHeight) {
                CameraActivity.ScreenWidth = width;
                CameraActivity.ScreenHeight = width * ratioHeight / ratioWidth;
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                CameraActivity.ScreenWidth = height * ratioWidth / ratioHeight;
                CameraActivity.ScreenHeight = height;
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
           // setMeasuredDimension(1920,1080);

        }
    }
}
