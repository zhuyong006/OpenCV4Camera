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

extern "C"
JNIEXPORT void JNICALL
Java_com_sunmi_opencv_camera_CameraOpenCV_faceDetect(JNIEnv *env, jobject instance, jlong addr) {

    // TODO
    //取到Java端的Mat对象
    Mat obj = *(Mat *)addr;
    Mat gray;

    cvtColor(obj,gray,COLOR_BGRA2GRAY);
    equalizeHist(gray,gray);
    std::vector<Rect> rects;
    cascade->detectMultiScale(gray,rects,1.3,3,0,Size(10,10),Size(0,0));
    if(rects.empty()) return;
    for(int i=0;i<rects.size();i++)
    {
        ALOGE("found face\n");
        rectangle(obj,rects[i],Scalar(255,0,0),2,8,0);
    }
//    rectangle(obj,Point(10,10),Point(100,100),Scalar(255,0,0),2,8,0);
    return;

}