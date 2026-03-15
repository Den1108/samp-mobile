package com.flyt.mobile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("samp-mobile")
        checkPermissions()

        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        playButton.setOnClickListener {
            val nickname = getSharedPreferences("FlytPrefs", MODE_PRIVATE)
                .getString("nickname", "Player") ?: "Player"

            logToFile("Попытка запуска игры для: $nickname")
            val result = launchGame(nickname)

            if (result.contains("Ошибка") || result.contains("не найден")) {
                logToFile("Кэш не прошел проверку. Запуск системы обновления...")
                startGameUpdate()
            } else {
                Toast.makeText(this, "Запуск: $result", Toast.LENGTH_SHORT).show()
            }
        }

        settingsButton.setOnClickListener {
            logToFile("Переход в настройки")
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 101)
            }
        }
    }

    private fun startGameUpdate() {
        val workManager = WorkManager.getInstance(this)
        
        // Создаем задачу на обновление
        val updateRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag("game_update")
            .build()

        workManager.enqueueUniqueWork("UPDATE_WORK", ExistingWorkPolicy.KEEP, updateRequest)
        
        Toast.makeText(this, "Проверка и загрузка файлов...", Toast.LENGTH_LONG).show()
        
        // Отслеживание прогресса (опционально)
        workManager.getWorkInfoByIdLiveData(updateRequest.id).observe(this) { info ->
            if (info?.state == WorkInfo.State.SUCCEEDED) {
                Toast.makeText(this, "Обновление завершено!", Toast.LENGTH_SHORT).show()
            } else if (info?.state == WorkInfo.State.FAILED) {
                Toast.makeText(this, "Ошибка при загрузке. Проверьте лог.", Toast.LENGTH_LONG).show()
            }
        }
    }

    external fun launchGame(nickname: String): String

    fun logToFile(message: String) {
        try {
            val logFile = File(getExternalFilesDir(null), "flyt_log.txt")
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            logFile.appendText("[$timestamp] $message\n")
        } catch (e: Exception) { e.printStackTrace() }
    }
}

/**
 * Воркер, который делает всю тяжелую работу: качает JSON, парсит его и качает файлы
 */
class UpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val jsonUrl = "https://raw.githubusercontent.com/Den1108/samp-mobile-cache/refs/heads/main/distribution.json"

    override fun doWork(): Result {
        log("Начало работы UpdateWorker")
        
        try {
            // 1. Скачиваем distribution.json
            val jsonContent = URL(jsonUrl).readText()
            val root = JSONObject(jsonContent)
            val cdnBase = root.getString("cdnCache") // "https://adorable-druid-2a5b25.netlify.app/"
            val cacheArray = root.getJSONArray("cache")

            log("JSON получен. Файлов к проверке: ${cacheArray.length()}")

            // 2. Итерируемся по списку файлов
            for (i in 0 until cacheArray.length()) {
                val fileObj = cacheArray.getJSONObject(i)
                val rawName = fileObj.getString("name") // "files\\data\\..."
                
                // Исправляем путь для Android (слэши)
                val cleanPath = rawName.replace("\\", "/")
                val fileUrl = cdnBase + cleanPath
                val targetFile = File(applicationContext.getExternalFilesDir(null), cleanPath)

                // 3. Создаем подпапки автоматически
                targetFile.parentFile?.let {
                    if (!it.exists()) {
                        val created = it.mkdirs()
                        if (created) log("Создана папка: ${it.absolutePath}")
                    }
                }

                // 4. Скачивание файла (с защитой от сбоев)
                downloadFile(fileUrl, targetFile)
            }

            log("Все файлы успешно обработаны")
            return Result.success()

        } catch (e: Exception) {
            log("КРИТИЧЕСКАЯ ОШИБКА ОБНОВЛЕНИЯ: ${e.stackTraceToString()}")
            return Result.failure()
        }
    }

    private fun downloadFile(url: String, destination: File) {
        // Можно добавить проверку: если файл существует и размер совпадает - не качать
        // Но по вашему запросу "не упрощать" — скачиваем/перезаписываем
        
        log("Загрузка: $url")
        URL(url).openStream().use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
        log("Сохранен: ${destination.name}")
    }

    private fun log(message: String) {
        val logFile = File(applicationContext.getExternalFilesDir(null), "flyt_log.txt")
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logFile.appendText("[$timestamp] [Worker] $message\n")
    }
}