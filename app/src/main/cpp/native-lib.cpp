#include <jni.h>
#include<opencv2/opencv.hpp>
#include<iostream>
#include <vector>
using namespace cv;
using namespace std;

#include <android/log.h>
#define ALOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"Jon",FORMAT,##__VA_ARGS__);

CascadeClassifier *cascade = nullptr;
Mat lm_eye_tpl;
Mat rm_eye_tpl;
Scalar object_color = Scalar(0,255,0);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_sunmi_opencv_camera_CameraOpenCV_initCascade(JNIEnv *env, jobject instance,
                                                      jstring path_){
    const char *cascadePath = env->GetStringUTFChars(path_,0);
    ALOGE("cascadePath:%s",cascadePath);
    cascade = new CascadeClassifier(cascadePath);
    if(!cascade)
    {
        ALOGE("CascadeClassifier Creat Failed\n");
        env->ReleaseStringUTFChars(path_, cascadePath);
        return false;
    }
    env->ReleaseStringUTFChars(path_, cascadePath);
    return true;
}

void tpl_match(Mat src, Mat tpl, int modes) {
    double minValue, maxValue;
    Point minLoc, maxLoc;
    int width = src.rows - tpl.rows + 1;
    int height = src.cols - tpl.cols + 1;

    Mat result = Mat(width,height,CV_32FC1);
    matchTemplate(src,tpl,result,modes);

    minMaxLoc(result,&minValue,&maxValue,&minLoc,&maxLoc);

    rectangle(src,Point(maxLoc.x,maxLoc.y),Point(maxLoc.x+tpl.rows,maxLoc.y+tpl.cols),object_color,2,8,0);
    return;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sunmi_opencv_camera_CameraOpenCV_objectDetect(JNIEnv *env, jobject instance, jlong addr, jint cmd, jboolean left_eye) {

    // TODO
    //取到Java端的Mat对象
    Mat obj = *(Mat *)addr;
    Mat gray;
    cvtColor(obj,gray,COLOR_BGRA2GRAY);
    equalizeHist(gray,gray);
    std::vector<Rect> rects;
    cascade->detectMultiScale(gray,rects,1.3,3,0,Size(40,40),Size(0,0));
    if(rects.empty()) { //匹配失败则用保存的模板做模板匹配
        gray.release();
        Mat tpl = left_eye ? lm_eye_tpl : rm_eye_tpl;
        if(tpl.empty() || obj.rows < tpl.rows || obj.cols < tpl.cols)
            return;
        tpl_match(obj,tpl,TM_CCOEFF_NORMED);
        return;
    }
    for(int i=0;i<rects.size();i++)
    {
        ALOGE("found object\n");
        rectangle(obj,rects[i],object_color,2,8,0);
        if(cmd == 5) {
            Mat eye = Mat(obj, rects[i]);
            if(left_eye)
                lm_eye_tpl = eye;
            else
                rm_eye_tpl = eye;
            eye.release();
        }else{
            lm_eye_tpl = rm_eye_tpl = Mat();
        }
    }
    gray.release();
    return;

}