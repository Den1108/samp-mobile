plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

// Репозитории теперь указываются только в settings.gradle.kts —
// блок allprojects { repositories {} } удалён, он устарел в AGP 8.x

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
