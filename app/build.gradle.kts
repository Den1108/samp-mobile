plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.launcher"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Настройка взаимодействия с C++ кодом
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17 -fexceptions")
                abiFilters("armeabi-v7a", "arm64-v8a")
            }
        }
    }

    // Связь с CMakeLists.txt (обязательно для компиляции .so библиотек)
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}