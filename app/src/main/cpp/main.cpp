#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_launcher_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "SAMP Mobile C++ Library Loaded!";
    return env->NewStringUTF(hello.c_str());
}