plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.flyt.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.flyt.mobile"
        minSdk = 24
        targetSdk = 34
        // Увеличивай эти значения при каждом обновлении APK
        versionCode = 9
        versionName = "1.8"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17 -fexceptions")
                abiFilters("armeabi-v7a", "arm64-v8a")
            }
        }
    }

    // 1. Настройка подписи (вставь свои пароли из терминала)
    signingConfigs {
        create("release") {
            storeFile = file("FlytMobile-release-key.jks")
            storePassword = "denjik20"
            keyAlias = "FlytMobile-release-key"
            keyPassword = "denjik20"
        }
    }

    buildTypes {
        getByName("release") {
            // 2. Привязываем подпись к релизу
            signingConfig = signingConfigs.getByName("release")
            
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        getByName("debug") {
            // Можно использовать ту же подпись для дебага, если нужно тестировать обновление
            signingConfig = signingConfigs.getByName("release")
        }
    }

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

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
}