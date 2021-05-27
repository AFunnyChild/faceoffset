package com.hnnk.lsh.facechair;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.faceDemo.activity.ActivityFaceList;
import com.faceDemo.activity.ClassifierActivity;
import com.faceDemo.activity.faceMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, ClassifierActivity.class));
        findViewById(R.id.btn_face_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      ActivityFaceList.getInstance().FinishActivity();
                  }
              });
            }
        });
        //finish();
    }
}
