package com.sunmi.opencv.camera;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.core.Mat;

public class CameraOpenCV extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private org.opencv.android.JavaCamera2View cv_camera = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view);
        if(Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        cv_camera = findViewById(R.id.cv_camera);
        cv_camera.setVisibility(SurfaceView.VISIBLE);
        cv_camera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        cv_camera.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }
}
