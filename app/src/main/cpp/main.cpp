#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("Flyt Mobile Library Loaded!");
}