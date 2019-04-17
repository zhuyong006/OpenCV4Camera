#include <jni.h>
#include<opencv2/opencv.hpp>
#include<iostream>
#include <vector>
using namespace cv;
using namespace std;

#include <android/log.h>
#define ALOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"Jon",FORMAT,##__VA_ARGS__);

CascadeClassifier *cascade = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_sunmi_opencv_camera_CameraOpenCV_initCascade(JNIEnv *env, jobject instance){
    ALOGE("Java_sunmi_opencv_camera_MainActivity_stringFromC");
    cascade = new CascadeClassifier("/data/haarcascade_eye_tree_eyeglasses.xml");
    if(!cascade)
    {
        ALOGE("CascadeClassifier Creat Failed\n");
        return false;
    }
    return true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sunmi_opencv_camera_CameraOpenCV_faceDetect(JNIEnv *env, jobject instance, jlong addr) {

    // TODO
    //取到Java端的Mat对象
    Mat obj = *(Mat *)addr;
    Mat gray;
    ALOGE("face detect enter E\n");

    cvtColor(obj,gray,COLOR_RGBA2GRAY);
    std::vector<Rect> rects;
    cascade->detectMultiScale(gray,rects,1.1,5,0,Size(10,10),Size(300,300));
    if(rects.empty()) return;
    for(int i=0;i<rects.size();i++)
    {
        ALOGE("found face\n");
        rectangle(obj,rects[i],Scalar(255,0,0),2,8,0);
    }
    return;

}