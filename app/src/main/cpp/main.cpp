#include <jni.h>       // Обязательно для работы с JNI (JNIEXPORT и т.д.)
#include <string>      // Обязательно для работы со строками (std::string)
#include <sys/stat.h>  // Для проверки файлов

// Функция проверки наличия файла
bool fileExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_launchGame(JNIEnv* env, jobject /* this */, jstring nickname) {
    // Используем путь, который гарантированно доступен приложению
    const char* path = "/sdcard/Android/data/com.flyt.mobile/files/gta_sa.set";
    
    struct stat buffer;
    if (stat(path, &buffer) == 0) {
        return env->NewStringUTF("Кэш найден! Запуск...");
    } else {
        // Возвращаем именно это сообщение, чтобы Kotlin поймал "Ошибка"
        return env->NewStringUTF("Ошибка: кэш не найден");
    }
}