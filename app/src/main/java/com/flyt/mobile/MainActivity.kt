package com.flyt.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.content.Context
import java.util.zip.ZipInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.File
import android.content.BroadcastReceiver
import android.content.IntentFilter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("samp-mobile")

        val statusText = findViewById<TextView>(R.id.statusText)
        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        // Кнопка ИГРАТЬ: достаем ник и вызываем C++
        playButton.setOnClickListener {
            val prefs = getSharedPreferences("FlytPrefs", MODE_PRIVATE)
            val nickname = prefs.getString("nickname", "Player") ?: "Player"
    
            // Вызываем проверку (C++ метод)
            val result = launchGame(nickname)
    
            if (result.contains("Ошибка")) { // Если C++ вернул ошибку, качаем
                Toast.makeText(this, "Кэш не найден, начинаю скачивание...", Toast.LENGTH_LONG).show()
                // Вставь сюда свою прямую ссылку на .zip архив с кэшем
                startDownload("https://samp-cache.netlify.app/cache.zip") 
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                // Здесь позже будет код для запуска самого игрового движка
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // Раньше была stringFromJNI, теперь заменяем на launchGame
    external fun launchGame(nickname: String): String

    fun startDownload(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Загрузка кэша Flyt Mobile")
            .setDescription("Скачивание файлов игры...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "cache.zip")

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    fun unzip(zipFile: File, targetDirectory: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(context, "Загрузка завершена! Распаковываю...", Toast.LENGTH_LONG).show()
        
            val zipFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "cache.zip")
            val targetDir = File(getExternalFilesDir(null), "") // Папка, куда распаковать
        
            // Запускаем распаковку в фоновом потоке, чтобы не "зависло" приложение
            Thread {
                unzip(zipFile, targetDir)
                runOnUiThread {
                    Toast.makeText(context, "Распаковка завершена! Можно играть.", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}