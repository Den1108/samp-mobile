#include <jni.h>       // Обязательно для работы с JNI (JNIEXPORT и т.д.)
#include <string>      // Обязательно для работы со строками (std::string)
#include <sys/stat.h>  // Для проверки файлов

// Функция проверки наличия файла
bool fileExists(const std::string& path) {
    struct stat buffer;
    return (stat(path.c_str(), &buffer) == 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_launchGame(JNIEnv* env, jobject /* this */, jstring nickname, jstring filePath) {
    const char* path = env->GetStringUTFChars(filePath, nullptr);
    
    struct stat buffer;
    bool exists = (stat(path, &buffer) == 0);
    
    env->ReleaseStringUTFChars(filePath, path); // Обязательно освобождаем память!

    if (exists) {
        return env->NewStringUTF("Кэш найден! Запуск...");
    } else {
        return env->NewStringUTF("Ошибка: кэш не найден");
    }
}