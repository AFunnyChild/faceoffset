package com.faceDemo.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.faceDemo.R;
import com.faceDemo.utils.PermissionUtils;

public class faceMainActivity extends AppCompatActivity implements View.OnClickListener{

    public static String TAG = "MainActivity";
    private ImageView mEffectVideo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_face);
        findViewById(R.id.detect).setOnClickListener(this);
        findViewById(R.id.btn_face_close).setOnClickListener(this);
         ActivityFaceList.getInstance().addActivity(this);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.detect){
            startVideoWithFaceDetected();
        }else  if(v.getId()==R.id.btn_face_close){
        ActivityFaceList.getInstance().FinishActivity();
        //    finish();

        }

    }

    private void startVideoWithFaceDetected() {
        PermissionUtils.checkPermission(this, new Runnable() {
            @Override
            public void run() {
                jumpToCameraActivity();
            }
        });
    }

    public void jumpToCameraActivity()
    {
        Intent intent = new Intent(faceMainActivity.this, ClassifierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                jumpToCameraActivity();
            } else {
                startVideoWithFaceDetected();
            }
        }
    }
}
