package com.faceDemo.activity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityFaceList {

    //类初始化时，不初始化这个对象(延时加载，真正用的时候再创建)
    private static ActivityFaceList instance;

    //构造器私有化
    private ActivityFaceList(){}

    //方法同步，调用效率低
    public static synchronized ActivityFaceList getInstance(){
        if(instance==null){
            instance=new ActivityFaceList();
        }
        return instance;
    }
    List<Activity>  activityList=new ArrayList<>();
    public  void addActivity(Activity  activity){
        activityList.add(activity);

    }
    public   void FinishActivity(){
        for (Activity activity : activityList) {
              if (activity instanceof CameraActivity){
                  CameraActivity  cameraActivity=(CameraActivity)activity;
                  cameraActivity.CloseAndFinish();
              }

        }

        activityList.clear();
    }
}
