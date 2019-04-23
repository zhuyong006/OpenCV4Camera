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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.facedetect.DetectionBasedTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CameraOpenCV extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 , View.OnClickListener{
    private org.opencv.android.JavaCamera2View cv_camera = null;
    private int current_camera_idx = CameraBridgeViewBase.CAMERA_ID_FRONT;
    private Button switch_camera = null;
    private int operate_cmd = -1;
    private Scalar eye_color = new Scalar(0,0,255);
    private DetectionBasedTracker tracker = null;
    private static final String TAG = "OpenCV.Jon";
    private String eyeCascadeFileName = "haarcascade_eye_tree_eyeglasses.xml";
    private String faceCascadeFileName = "lbpcascade_frontalface.xml";
    private boolean faceTrackerFlag = false;
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
        //初始化人眼检测
        String Cascade_path = null;
        try {
            Cascade_path = getCascadeDir(eyeCascadeFileName);
            initCascade(Cascade_path);
        }catch (IOException e){
            e.printStackTrace();
        }
        //初始化人脸追踪
        try {
            tracker = new DetectionBasedTracker(getCascadeDir(faceCascadeFileName), 5);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private String getCascadeDir(String cascadeFileName)throws IOException {
        InputStream input = null;
        if(cascadeFileName==eyeCascadeFileName)
            input  = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
        else if(cascadeFileName==faceCascadeFileName)
            input  = getResources().openRawResource(R.raw.lbpcascade_frontalface);

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
        }else if(item.getItemId() == R.id.obj_detect) {
            operate_cmd = 3;
        }else if(item.getItemId() == R.id.face_track) {
            operate_cmd = 4;
        }else if(item.getItemId() == R.id.eye_detect) {
            operate_cmd = 5;
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
        //如果之前已经启动了，人脸追踪，而目前是做其他处理，则先将人脸追踪关闭
        if((operate_cmd != 4) && (operate_cmd != 5) && faceTrackerFlag)
        {
            Log.e(TAG,"tracker stop");
            tracker.stop();
            faceTrackerFlag = false;
        }

        if(operate_cmd == 0){   //反色处理
            Core.bitwise_not(frame,frame);
        }else if(operate_cmd == 1){ //高斯处理
            Imgproc.GaussianBlur(frame,frame,new Size(25,25),0);
        }else if(operate_cmd == 2){ //边缘检测
            Imgproc.cvtColor(frame,frame,Imgproc.COLOR_BGRA2GRAY);
            Imgproc.Canny(frame,frame,100,200,3,false);
        }else if(operate_cmd == 3){ //目标检测
            faceDetect(frame.getNativeObjAddr());
        }else if(operate_cmd == 4 || operate_cmd == 5){ //人脸追踪以及人眼检测
            //对于operate_cmd == 4，只是单纯的人脸追踪
            /* 对于operate_cmd == 5，则是在人脸追踪的基础上
            *   a. 首先在人脸追踪的基础上，先找到人脸
            *   b. 找到人脸后根据人体的生物特征找到人眼区域
            *   c. 找到人眼的区域后，将人眼区域的子图交给级联检测器去精准的识别人眼
            * */
            faceTrack(frame);
        }
    }

    private void eyeDetect(Mat frame,Rect rect) {
            int fWidth = rect.width;
            int fHeight = rect.height;

            //step 1 : 找到人眼的大致区域
            int offy = (int) (fHeight * 0.35f);
            int offx = (int) (fWidth * 0.15f);
            int sh = (int) (fHeight * 0.13f);
            int sw = (int) (fWidth * 0.32f);
            int gap = (int) (fWidth * 0.025f);
            Point lp_eye = new Point(rect.x + offx,rect.y + offy);
            Point lp_end = new Point(lp_eye.x + sw -gap,lp_eye.y+sh);

            int right_offx = (int)(fWidth * 0.095f);
            int rew = (int) (sw * 0.81f);
            Point rp_eye = new Point(rect.x + fWidth/2 + right_offx,rect.y + offy);
            Point rp_end = new Point(rp_eye.x + rew,rp_eye.y + sh);

            Imgproc.rectangle(frame,lp_eye,lp_end,eye_color,2,8,0);
            Imgproc.rectangle(frame,rp_eye,rp_end,eye_color,2,8,0);

            //step 2： 将人眼(左右眼)的大致区域送给级联检测器去检测，返回结果是人眼的精准区域
            int le_width = (int)(lp_end.x-lp_eye.x);
            int le_height = (int)(lp_end.y-lp_eye.y);
            Mat lm_eye = frame.submat(new Rect((int)lp_eye.x,(int) lp_eye.y,le_width,le_height));
            faceDetect(lm_eye.getNativeObjAddr());

            int re_width = (int)(rp_end.x-rp_eye.x);
            int re_height = (int)(rp_end.y-rp_eye.y);
            Mat rm_eye = frame.submat(new Rect((int)rp_eye.x,(int) rp_eye.y,re_width,re_height));
            faceDetect(rm_eye.getNativeObjAddr());

            //step 3：模板匹配

            return;
    }

    private void faceTrack(Mat frame) {
        if(!faceTrackerFlag) {
            Log.e(TAG,"tracker start");
            tracker.setMinFaceSize(5);
            tracker.start();
            faceTrackerFlag = true;
        }

        MatOfRect mRects = new MatOfRect();
        Mat gray = new Mat();
        Imgproc.cvtColor(frame,gray,Imgproc.COLOR_BGRA2GRAY);
        tracker.detect(gray,mRects);
        List<Rect> rects = mRects.toList();
        if(rects.size() == 0)
        {
            gray.release();
            mRects.release();
            return;
        }

        for(int i=0;i<rects.size();i++)
        {
            Rect rect = rects.get(i);
            Imgproc.rectangle(frame,rect,new Scalar(255,0,0),2,8,0);
            if(operate_cmd == 5) //人眼检测
                eyeDetect(frame,rect);
        }

        gray.release();
        mRects.release();
        return ;
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
