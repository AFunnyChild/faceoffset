package com.faceDemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import com.faceDemo.R;
import com.faceDemo.camera.CameraEngine;
import com.faceDemo.currencyview.OverlayView;
import com.faceDemo.encoder.BitmapEncoder;
import com.faceDemo.encoder.CircleEncoder;
import com.faceDemo.encoder.EncoderBus;
import com.faceDemo.encoder.RectEncoder;
import com.faceDemo.utils.PermissionUtils;
import com.tenginekit.KitCore;
import com.tenginekit.face.Face;
import com.tenginekit.face.FaceDetectInfo;
import com.tenginekit.face.FaceLandmarkInfo;
import com.tenginekit.model.TenginekitPoint;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ClassifierActivity extends CameraActivity {



    private static final String TAG = "ClassifierActivity";

    private OverlayView trackingOverlay;
    TextView   tv_log;
    NumberFormat nf = NumberFormat.getNumberInstance();
     float  mEyeCloseResult=0;
     float  mLeftEyeCloseResult=0;
  List<Integer>  mEyeDataList=new ArrayList<>();
  boolean  mEyeThreadRun=false;
    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return new Size(1280, 960);
    }

    public void Registe() {
        /**
         * canvas 绘制人脸框，人脸关键点
         * */
        EncoderBus.GetInstance().Registe(new BitmapEncoder(this));
        EncoderBus.GetInstance().Registe(new CircleEncoder(this));
        EncoderBus.GetInstance().Registe(new RectEncoder(this));
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        mEyeThreadRun=false;
    }
   int  no=0;
    @Override
    public void onPreviewSizeChosen(final Size size) {
        nf.setMaximumFractionDigits(2);
        mEyeThreadRun=true;
        new  Thread(){
            @Override
            public void run() {
                super.run();
                while (mEyeThreadRun){
                    mEyeDataList.add((int) (mEyeCloseResult*100));
                    mEyeDataList.add( (int)(mLeftEyeCloseResult*100));
                 //   Log.d("mEyeDataList", "run: start"+(int) (mEyeCloseResult*100));
//                    int inte= (int) (mEyeCloseResult*10);
//                    Log.d("ClassifierActivitytes", "run: "+inte);
                    if (mEyeDataList.size()>1000){
                        mEyeDataList.remove(0);
                    }
                    if (mEyeDataList.size()>=30) {
                        boolean isCloseState = true;
                        for (int i = 30; i >= 1; i--) {
                            int current = mEyeDataList.get(mEyeDataList.size() - i);
                            if (current <=75) {
                                isCloseState = false;
                            }
                        }
                        if(isCloseState){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

//                                    Log.d("ClassifierActivitytes", "run: start");
//                                    for (int i = 30; i >= 1; i--) {
//                                        int current = mEyeDataList.get(mEyeDataList.size() - i);
//                                        Log.d("ClassifierActivitytes", "run: "+current);
//                                    }
                                    mEyeDataList.clear();
                                    faceEyeClose();
                             //       no++;
                                //    Toast.makeText(ClassifierActivity.this, "close"+no+"/n"+"/n", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        }

                    SystemClock.sleep(100);
                }
            }
        }.start();
        Registe();
        EncoderBus.GetInstance().onSetFrameConfiguration(previewHeight, previewWidth);

        trackingOverlay = (OverlayView) findViewById(R.id.facing_overlay);
        tv_log=findViewById(R.id.tv_log);
        boolean visible = getIntent().getBooleanExtra("visible", false);
        if (visible==false){
            moveTaskToBack(true);
        }
      //
        trackingOverlay.addCallback(new OverlayView.DrawCallback() {
            @Override
            public void drawCallback(final Canvas canvas) {
                EncoderBus.GetInstance().onDraw(canvas);
            }
        });
    }

    @Override
    protected void processImage() {
        if (sensorEventUtil!= null) {
            //sensorEventUtil.orientation
            int degree = CameraEngine.getInstance().getCameraOrientation(sensorEventUtil.orientation);
            /**
             * 设置旋转角
             */
            KitCore.Camera.setRotation(degree - 90, false, (int) CameraActivity.ScreenWidth, (int) CameraActivity.ScreenHeight);
         //   degree - 90
            /**
             * 获取人脸信息
             */
            Face.FaceDetect faceDetect = Face.detect(mNV21Bytes);
            List<FaceDetectInfo> faceDetectInfos = new ArrayList<>();
            List<FaceLandmarkInfo> landmarkInfos = new ArrayList<>();
            if (faceDetect.getFaceCount() > 0) {
                faceDetectInfos = faceDetect.getDetectInfos();
                landmarkInfos = faceDetect.landmark2d();
                mEyeCloseResult=landmarkInfos.get(0).rightEyeClose;
                mLeftEyeCloseResult=landmarkInfos.get(0).leftEyeClose;

              String  info= "r="+ (int)landmarkInfos.get(0).roll+" y="+(int)landmarkInfos.get(0).yaw;
            //  String  info= "lEye="+nf.format(landmarkInfos.get(0).leftEyeClose) +"- rEye"+nf.format(landmarkInfos.get(0).rightEyeClose) ;

               faceOffset((int)(landmarkInfos.get(0).roll*1000),(int)(landmarkInfos.get(0).yaw*1000));
              tv_log.setText(info);
                Rect boundingBox = landmarkInfos.get(0).getBoundingBox();
               // Log.e(" faceOffset", mEyeCloseResult+"--"+mLeftEyeCloseResult);
                int xabs = Math.abs(boundingBox.left - boundingBox.right);
                int yabs = Math.abs(boundingBox.top - boundingBox.bottom);
                faceSize(xabs,yabs);
               // Log.e(" faceOffset", xabs+"--"+yabs);
                //Log.e(" faceOffset", boundingBox.left+"-"+boundingBox.top+"--"+boundingBox.right+"--"+boundingBox.bottom);
            }

       //     Log.d("#####", "processImage: " + faceDetectInfos.size());
            if (faceDetectInfos != null && faceDetectInfos.size() > 0) {
                Rect[] face_rect = new Rect[faceDetectInfos.size()];

                List<List<TenginekitPoint>> face_landmarks = new ArrayList<>();
                for (int i = 0; i < faceDetectInfos.size(); i++) {
                    Rect rect = new Rect();
                    rect = faceDetectInfos.get(i).asRect();
                    face_rect[i] = rect;
                    face_landmarks.add(landmarkInfos.get(i).landmarks);


                }
                EncoderBus.GetInstance().onProcessResults(face_rect);
                EncoderBus.GetInstance().onProcessResults(face_landmarks);
            }
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
                if (trackingOverlay!=null) {
                    trackingOverlay.postInvalidate();
                }
            }


        });
    }
    public static native   void  faceEyeClose();
    public static native   void  faceOffset(int roll,int yaw);
    public static  native  void  faceSize(int width,int height);
//    public static    void  faceOffset(int roll,int yaw){};
//    public static    void  faceSize(int width,int height){};
//    public static    void  faceEyeClose(){};
}