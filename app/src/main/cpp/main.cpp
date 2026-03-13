#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_org_example_MainActivity_stringFromJNI( // Имя пакета org.example и класса MainActivity
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("SAMP Mobile Library Loaded Successfully!");
}