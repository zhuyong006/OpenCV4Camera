package com.sunmi.opencv.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button gray = null;
    private static final String TAG = "OpenCV.Jon";
    private ImageView imageView = null;
    private Button camera = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gray = findViewById(R.id.gray);
        gray.setOnClickListener(this);
        imageView = findViewById(R.id.gray_image);
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(this);
        initOpenCVLibs();
    }

    private void initOpenCVLibs() {

        boolean Status =  OpenCVLoader.initDebug();
        if(Status != true)
        {
            Log.e(TAG, "OpenCV Init Failed");
        }else{
            Log.e(TAG, "OpenCV Init Success");
        }
    }
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.gray)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.raw.girl, options);
            Mat src = new Mat();
            Utils.bitmapToMat(bitmap,src);
            Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
            Utils.matToBitmap(src,bitmap);
            imageView.setImageBitmap(bitmap);
            src.release();
        }else if(view.getId() == R.id.camera) {
            Log.e(TAG,"OpenCamera");
            Intent intent = new Intent(this.getApplicationContext(),CameraOpenCV.class);
            startActivity(intent);
        }
    }
}
