package com.flyt.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    // ВЕРСИЯ ЭТОГО APK (меняй при каждом обновлении приложения)
    private val CURRENT_APP_VERSION = 1.0
    
    // Ссылки для обновления самого Лаунчера (APK)
    private val APP_VERSION_URL = "http://192.168.31.178:3000/app_version.txt"
    private val APK_URL = "http://192.168.31.178:3000/latest_launcher.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        // 1. При запуске проверяем наличие обновлений самого APK
        checkAppUpdate()

        playButton.setOnClickListener {
            // Теперь просто переходим в DownloadActivity. 
            // UpdateWorker внутри сам поймет: качать всё или файлы уже на месте.
            startActivity(Intent(this, DownloadActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun checkAppUpdate() {
        thread {
            try {
                val remoteVersion = URL(APP_VERSION_URL).readText().trim().toDouble()
                if (remoteVersion > CURRENT_APP_VERSION) {
                    runOnUiThread {
                        Toast.makeText(this, "Обновление лаунчера...", Toast.LENGTH_LONG).show()
                        downloadAndInstallApk()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadAndInstallApk() {
        thread {
            try {
                val apkFile = File(getExternalFilesDir(null), "update.apk")
                URL(APK_URL).openStream().use { input ->
                    apkFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                runOnUiThread { installApk(apkFile) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    // Метод для запуска игры (вызывается обычно из DownloadActivity после успеха)
    fun startLaunchSequence() {
        val nickname = getSharedPreferences("FlytPrefs", MODE_PRIVATE).getString("nickname", "Player") ?: "Player"
        val gtaSaSetPath = File(getExternalFilesDir(null), "files/gta_sa.set").absolutePath
        launchGame(nickname, gtaSaSetPath)
    }

    external fun launchGame(nickname: String, path: String): String
}