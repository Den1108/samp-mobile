#include <jni.h>
#include <string>
#include <sys/stat.h>
#include <android/log.h>

#define LOG_TAG "FlytMobile"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Проверяет, существует ли файл по заданному пути
static bool fileExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0);
}

// Проверяет, существует ли папка с кэшем игры
// Вызывается из PlayFragment перед запуском
extern "C" JNIEXPORT jboolean JNICALL
Java_com_flyt_mobile_PlayFragment_nativeCacheExists(
        JNIEnv* env, jobject /* this */, jstring cachePath) {

    const char* path = env->GetStringUTFChars(cachePath, nullptr);
    bool exists = fileExists(std::string(path));
    env->ReleaseStringUTFChars(cachePath, path);

    LOGI("Cache check at '%s': %s", path, exists ? "EXISTS" : "NOT FOUND");
    return static_cast<jboolean>(exists);
}

// Возвращает версию нативной библиотеки — удобно для дебага
extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_nativeGetVersion(JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF("1.0.0");
}
