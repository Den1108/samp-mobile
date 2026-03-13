#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_flyt_mobile_MainActivity_launchGame( // Имя функции теперь включает launchGame
        JNIEnv* env,
        jobject /* this */,
        jstring nickname) { // Добавили параметр nickname
    
    // Преобразуем Java-строку в C++ строку
    const char *nativeNickname = env->GetStringUTFChars(nickname, 0);
    
    std::string message = "Запуск игры с ником: ";
    message += nativeNickname;
    
    // Освобождаем память
    env->ReleaseStringUTFChars(nickname, nativeNickname);
    
    return env->NewStringUTF(message.c_str());
}