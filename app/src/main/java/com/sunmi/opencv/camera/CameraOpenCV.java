package com.sunmi.opencv.camera;

import android.Manifest;
import android.content.Context;
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
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraOpenCV extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 , View.OnClickListener{
    private org.opencv.android.JavaCamera2View cv_camera = null;
    private int current_camera_idx = CameraBridgeViewBase.CAMERA_ID_FRONT;
    private Button switch_camera = null;
    private int operate_cmd = -1;
    private static final String TAG = "OpenCV.Jon";
    //private String cascadeFileName = "haarcascade_eye_tree_eyeglasses.xml";
    private String cascadeFileName = "lbpcascade_frontalface.xml";
    static{
        System.loadLibrary("native-lib");
    }

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
        cv_camera.enableFpsMeter();
        cv_camera.setCvCameraViewListener(this);
        //Init Face Dectect CascadeClassifier
        try {
            initCascade(getCascadeDir());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private String getCascadeDir()throws IOException {
        InputStream input = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        File cascadeDir = this.getDir("cascade", Context.MODE_PRIVATE);
        File file = new File(cascadeDir.getAbsoluteFile(), cascadeFileName);
        Log.e(TAG,cascadeDir.getAbsoluteFile().getAbsolutePath());
        String dst = file.getAbsolutePath();
        FileOutputStream output = new FileOutputStream(file);
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = input.read(buff)) != -1) {
            Log.e(TAG,"1111");
            output.write(buff, 0, len);
        }

        input.close();
        output.close();
        return dst;
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.e(TAG,"witdh : " + width + "height : " + height);
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
            operate_cmd = 0;
        }else if(item.getItemId() == R.id.gause_blue) {
            operate_cmd = 1;
        }else if(item.getItemId() == R.id.edge) {
            operate_cmd = 2;
        }else if(item.getItemId() == R.id.face_detect) {
            operate_cmd = 3;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat dst = inputFrame.rgba();
        //Log.e(TAG,"orientation : " + this.getRequestedOrientation());

        // 90度 翻转
        if(current_camera_idx == CameraBridgeViewBase.CAMERA_ID_BACK)
            Core.rotate(dst, dst, Core.ROTATE_90_CLOCKWISE);
        if(current_camera_idx == CameraBridgeViewBase.CAMERA_ID_FRONT)
        {
            Core.rotate(dst, dst, Core.ROTATE_90_CLOCKWISE);
            Core.flip(dst,dst,0);
        }


        // Full Screen Preview
        process_image(dst);
        Imgproc.resize(dst,dst,new Size(cv_camera.getWidth(), cv_camera.getHeight()),0.0D,0.0D,0);
        return dst;
    }

    private void process_image(Mat frame) {
        if(operate_cmd == 0){
            Core.bitwise_not(frame,frame);
        }else if(operate_cmd == 1){
            Imgproc.GaussianBlur(frame,frame,new Size(25,25),0);
        }else if(operate_cmd == 2){
            Imgproc.cvtColor(frame,frame,Imgproc.COLOR_BGRA2GRAY);
            Imgproc.Canny(frame,frame,100,200,3,false);
        }else if(operate_cmd == 3){
            faceDetect(frame.getNativeObjAddr());
        }
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.swich_camera)
        {
            cv_camera.disableView();
            if(current_camera_idx == CameraBridgeViewBase.CAMERA_ID_BACK){
                current_camera_idx = CameraBridgeViewBase.CAMERA_ID_FRONT;
            }else{
                current_camera_idx = CameraBridgeViewBase.CAMERA_ID_BACK;
            }
            //Log.e(TAG,"click");
            cv_camera.setCameraIndex(current_camera_idx);
            CameraBridgeViewBase.init_status = false;
            cv_camera.enableView();

        }
    }
    public native boolean initCascade(String path);
    public native void faceDetect(long addr);

}
