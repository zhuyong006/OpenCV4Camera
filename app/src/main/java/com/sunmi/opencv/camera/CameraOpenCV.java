package com.sunmi.opencv.camera;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class CameraOpenCV extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 , View.OnClickListener{
    private org.opencv.android.JavaCamera2View cv_camera = null;
    private int current_camera_idx = CameraBridgeViewBase.CAMERA_ID_BACK;
    private Button switch_camera = null;
    private static final String TAG = "OpenCV.Jon";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view);
        if(Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        switch_camera = findViewById(R.id.swich_camera);
        switch_camera.setOnClickListener(this);
        cv_camera = findViewById(R.id.cv_camera);
        cv_camera.setVisibility(SurfaceView.VISIBLE);
        cv_camera.setCameraIndex(current_camera_idx);
        cv_camera.enableView();
        cv_camera.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.camera_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.inverse) {
            Log.e(TAG,"inverse");
        }else if(item.getItemId() == R.id.gause_blue) {
            Log.e(TAG,"gause_blue");
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Mat dst = inputFrame.rgba();
//        //Log.e(TAG,"orientation : " + this.getRequestedOrientation());
//
//        if(current_camera_idx == CameraBridgeViewBase.CAMERA_ID_BACK)
//            Core.rotate(dst, dst, Core.ROTATE_90_CLOCKWISE);
//        if(current_camera_idx == CameraBridgeViewBase.CAMERA_ID_FRONT)
//        {
//            Core.rotate(dst, dst, Core.ROTATE_90_COUNTERCLOCKWISE);
//            Core.flip(dst, dst, 1);
//        }
        return inputFrame.rgba();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.swich_camera)
        {
            if(current_camera_idx == CameraBridgeViewBase.CAMERA_ID_BACK){
                current_camera_idx = CameraBridgeViewBase.CAMERA_ID_FRONT;
            }else{
                current_camera_idx = CameraBridgeViewBase.CAMERA_ID_BACK;
            }
            //Log.e(TAG,"click");
            cv_camera.disableView();
            cv_camera.setCameraIndex(current_camera_idx);
            cv_camera.enableView();

        }
    }
}
