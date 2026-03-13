#include <sys/stat.h> // Для проверки файлов
#include <unistd.h>

// Функция для проверки, существует ли файл
bool fileExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_launchGame(
        JNIEnv* env, jobject /* this */, jstring nickname) {
    
    // Путь к файлу, который мы ищем (например, gta_sa.set)
    std::string cachePath = "/sdcard/Android/data/com.flyt.mobile/files/gta_sa.set";
    
    if (fileExists(cachePath)) {
        // Кэш найден
        return env->NewStringUTF("Кэш найден! Запуск...");
    } else {
        // Кэш не найден
        return env->NewStringUTF("Ошибка: файлы игры не найдены!");
    }
}