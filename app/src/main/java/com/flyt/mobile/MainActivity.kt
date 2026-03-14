package com.flyt.mobile

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {

    private var myDownloadId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("samp-mobile")

        // 1. Проверка разрешений при старте
        checkPermissions()

        val statusText = findViewById<TextView>(R.id.statusText)
        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        playButton.setOnClickListener {
            val nickname = getSharedPreferences("FlytPrefs", MODE_PRIVATE)
                .getString("nickname", "Player") ?: "Player"
            
            val result = launchGame(nickname)
            if (result.contains("Ошибка")) {
                startDownload("https://samp-cache.netlify.app/cache.zip")
            } else {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
            }
        }

        // Регистрация Receiver
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(onDownloadComplete, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(onDownloadComplete, filter)
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

    external fun launchGame(nickname: String): String

    fun startDownload(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Загрузка кэша")
            .setDescription("Скачивание...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "cache.zip")

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        myDownloadId = manager.enqueue(request)
        Toast.makeText(this, "Загрузка началась...", Toast.LENGTH_SHORT).show()
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != myDownloadId) return // Игнорируем чужие загрузки

            val zipFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "cache.zip")
            val targetDir = getExternalFilesDir(null)!!

            Thread {
                try {
                    logToFile("Старт распаковки")
                    unzip(zipFile, targetDir)
                    zipFile.delete() // Удаляем зип после успеха
                    runOnUiThread { Toast.makeText(context, "Успешно!", Toast.LENGTH_SHORT).show() }
                } catch (e: Exception) {
                    logToFile("Ошибка распаковки: ${e.message}")
                }
            }.start()
        }
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
                    FileOutputStream(newFile).use { fos -> zis.copyTo(fos) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    fun logToFile(message: String) {
        val logFile = File(getExternalFilesDir(null), "flyt_log.txt")
        logFile.appendText("[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}] $message\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}