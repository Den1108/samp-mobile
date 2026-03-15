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

    // Объявляем переменную здесь, чтобы она была доступна во всем классе
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("samp-mobile")
        checkPermissions()

        // Инициализируем UI
        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        statusText = findViewById<TextView>(R.id.statusText) // Теперь она инициализирована сразу

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
        
        val updateRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag("game_update")
            .build()

        // Используем REPLACE, чтобы если старое обновление зависло, началось новое чистое
        workManager.enqueueUniqueWork("UPDATE_WORK", ExistingWorkPolicy.REPLACE, updateRequest)
        
        workManager.getWorkInfoByIdLiveData(updateRequest.id).observe(this) { info ->
            if (info != null) {
                val progress = info.progress.getString("progress_msg")
                if (progress != null) {
                    statusText.text = progress 
                }

                when (info.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        statusText.text = "Все файлы проверены!"
                        Toast.makeText(this, "Готово! Можно играть", Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.FAILED -> {
                        statusText.text = "Ошибка загрузки. Попробуйте еще раз."
                    }
                    else -> {}
                }
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
        try {
            log("Запуск проверки целостности файлов...")
            
            val jsonContent = URL(jsonUrl).readText()
            val root = JSONObject(jsonContent)
            val cdnBase = root.getString("cdnCache")
            val cacheArray = root.getJSONArray("cache")
            val totalFiles = cacheArray.length()

            for (i in 0 until totalFiles) {
                val fileObj = cacheArray.getJSONObject(i)
                val rawName = fileObj.getString("name")
                
                // Получаем ожидаемый размер файла (берем первый элемент массива bytes из вашего JSON)
                val expectedSize = fileObj.getJSONArray("bytes").getLong(0)
                
                val cleanPath = rawName.replace("\\", "/")
                val fileUrl = cdnBase + cleanPath
                val targetFile = File(applicationContext.getExternalFilesDir(null), cleanPath)

                // ОБНОВЛЯЕМ ПРОГРЕСС ДЛЯ ЭКРАНА
                val progressData = workDataOf("progress_msg" to "Проверка: ${i + 1}/$totalFiles\n${targetFile.name}")
                setProgressAsync(progressData)

                // ПРОВЕРКА: Если файл есть и размер совпадает — скипаем
                if (targetFile.exists() && targetFile.length() == expectedSize) {
                    continue 
                }

                // Если файла нет или размер не тот — качаем
                targetFile.parentFile?.mkdirs()
                downloadFile(fileUrl, targetFile)
            }

            return Result.success()

        } catch (e: Exception) {
            log("Ошибка: ${e.message}")
            return Result.failure()
        }
    }

    private fun downloadFile(url: String, destination: File) {
        URL(url).openStream().use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun log(message: String) {
        val logFile = File(applicationContext.getExternalFilesDir(null), "flyt_log.txt")
        logFile.appendText("[${System.currentTimeMillis()}] $message\n")
    }
}