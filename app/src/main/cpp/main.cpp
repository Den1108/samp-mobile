#include <jni.h>       // Обязательно для работы с JNI (JNIEXPORT и т.д.)
#include <string>      // Обязательно для работы со строками (std::string)
#include <sys/stat.h>  // Для проверки файлов

// Функция проверки наличия файла
bool fileExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_launchGame(
        JNIEnv* env,
        jobject /* this */,
        jstring nickname) {
    
    // Путь к файлу кэша
    std::string cachePath = "/sdcard/Android/data/com.flyt.mobile/files/gta_sa.set";
    
    if (fileExists(cachePath)) {
        return env->NewStringUTF("Кэш найден! Начинаю загрузку...");
    } else {
        return env->NewStringUTF("Ошибка: файлы игры не найдены в /data/com.flyt.mobile/files/");
    }
}