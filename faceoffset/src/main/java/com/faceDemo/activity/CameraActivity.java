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

package com.faceDemo.activity;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.faceDemo.R;
import com.faceDemo.currencyview.LegacyCameraConnectionFragment;
import com.faceDemo.utils.MyLogger;
import com.faceDemo.utils.PermissionUtils;
import com.faceDemo.utils.SensorEventUtil;
import com.tenginekit.AndroidConfig;
import com.tenginekit.KitCore;

public abstract class CameraActivity extends AppCompatActivity implements
        Camera.PreviewCallback{
    private static final String TAG = "CameraActicity";

    // 照相机预览宽
    protected int previewWidth = 0;
    // 照相机预览高
    protected int previewHeight = 0;
    // 展示区域宽
    public static float ScreenWidth;
    // 展示区域高
    public static float ScreenHeight;

    public static int CameraId = 0;

    private boolean isProcessingFrame = false;
    // 是否是前置摄像头
    public static boolean is_front_camera = true;

    private Handler handler;
    private HandlerThread handlerThread;
    protected SensorEventUtil sensorEventUtil;

    // 相机的数据 nv21格式
    protected byte[] mNV21Bytes;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        ActivityFaceList.getInstance().addActivity(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        Window window=getWindow();
//        WindowManager.LayoutParams wl = window.getAttributes();
//        wl.flags=WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
////这句就是设置窗口里控件的透明度的．０.０全透明．１.０不透明．
//        wl.alpha=0f;
//        window.setAttributes(wl);
        //左上角显示
        Window window = getWindow();
        window.setGravity(Gravity.START|Gravity.TOP);

        //设置为1像素大小
        WindowManager.LayoutParams params = window.getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        int xpos = getIntent().getIntExtra("xpos",0);
        int ypos = getIntent().getIntExtra("ypos",0);
        params.x = xpos;
        params.y = ypos;
        params.width = 400;
        params.height = 400;
        params.alpha=0;
        boolean visible = getIntent().getBooleanExtra("visible", false);

        if (visible){
            params.alpha=100;
        }
        window.setAttributes(params);
        setContentView(R.layout.activity_camera);
        findViewById(R.id.btn_face_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorEventUtil=null;
                KitCore.release();
                finish();
            }
        });

        startVideoWithFaceDetected();

    }
    public  void CloseAndFinish(){
        sensorEventUtil=null;
        KitCore.release();
        finish();

    }
    private void startVideoWithFaceDetected() {
        PermissionUtils.checkPermission(this, new Runnable() {
            @Override
            public void run() {
                setFragment();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    ) {
                startVideoWithFaceDetected();
            } else {
                startVideoWithFaceDetected();
            }
        }
    }

    private void jumpToCameraActivity() {

    }

    public void Init() {
        mNV21Bytes = new byte[previewHeight * previewWidth];

        /**
         * 初始化
         * */
        KitCore.init(
                CameraActivity.this,
                AndroidConfig
                        .create()
                        .setCameraMode()
                        .openFunc(AndroidConfig.Func.Detect).openFunc(AndroidConfig.Func.Landmark)
                        .setDefaultInputImageFormat()
                        .setInputImageSize(previewWidth, previewHeight)
                        .setOutputImageSize((int) ScreenWidth, (int) ScreenHeight)
        );
        if (sensorEventUtil == null) {
            sensorEventUtil = new SensorEventUtil(this);
        }
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            return;
        }
        isProcessingFrame = true;
        try {
            if (mNV21Bytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                Init();
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height));
            }
        } catch (final Exception e) {
            MyLogger.logError(TAG, "onPreviewFrame: " + e);
            return;
        }
        mNV21Bytes = bytes;
        camera.addCallbackBuffer(bytes);
        isProcessingFrame = false;
        processImage();
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        processImage();
    }

    @Override
    public synchronized void onPause() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            MyLogger.logError(TAG, "onPause: " + e);
        }
        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        /**
         * 释放
         * */
        KitCore.release();
    }


    protected void setFragment() {
        LegacyCameraConnectionFragment fragment = new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        CameraId = fragment.getCameraId();
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();
}
