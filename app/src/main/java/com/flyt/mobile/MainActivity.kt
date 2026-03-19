package com.flyt.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    // ВЕРСИЯ ЭТОГО APK
    private val CURRENT_APP_VERSION = 1.2
    
    private val APP_VERSION_URL = "http://192.168.31.178:3000/app_version.txt"
    private val APK_URL = "http://192.168.31.178:3000/latest_launcher.apk"

    // Элементы интерфейса
    private lateinit var mainLayout: LinearLayout
    private lateinit var updateLayout: LinearLayout
    private lateinit var downloadLayout: LinearLayout
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация View (убедись, что ID совпадают с твоим XML)
        mainLayout = findViewById(R.id.mainLayout) // Контейнер с кнопками Играть/Настройки
        updateLayout = findViewById(R.id.updateLayout) // Контейнер "Доступна новая версия"
        downloadLayout = findViewById(R.id.downloadLayout) // Контейнер с прогресс-баром
        
        progressBar = findViewById(R.id.updateProgressBar)
        tvProgress = findViewById(R.id.tvUpdateProgress)
        tvStatus = findViewById(R.id.tvUpdateStatus)

        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val btnDownloadUpdate = findViewById<Button>(R.id.btnDownloadUpdate)
        val btnConfirmInstall = findViewById<Button>(R.id.btnConfirmInstall)

        // Скрываем всё лишнее при старте
        updateLayout.visibility = View.GONE
        downloadLayout.visibility = View.GONE

        checkAppUpdate()

        playButton.setOnClickListener {
            startActivity(Intent(this, DownloadActivity::class.java))
        }

        btnDownloadUpdate.setOnClickListener {
            showDownloadUI()
            downloadAndInstallApk()
        }
        
        btnConfirmInstall.setOnClickListener {
            val apkFile = File(getExternalFilesDir(null), "update.apk")
            if (apkFile.exists()) {
                installApk(apkFile)
            }
        }
    }

    private fun checkAppUpdate() {
        thread {
            try {
                val remoteVersion = URL(APP_VERSION_URL).readText().trim().toDouble()
                if (remoteVersion > CURRENT_APP_VERSION) {
                    runOnUiThread {
                        mainLayout.visibility = View.GONE
                        updateLayout.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showDownloadUI() {
        updateLayout.visibility = View.GONE
        downloadLayout.visibility = View.VISIBLE
        tvStatus.text = "Загрузка обновления..."
    }

    private fun downloadAndInstallApk() {
        thread {
            try {
                val apkFile = File(getExternalFilesDir(null), "update.apk")
                val url = URL(APK_URL)
                val connection = url.openConnection()
                connection.connect()
                
                val fileLength = connection.contentLengthLong
                val input = connection.getInputStream()
                val output = FileOutputStream(apkFile)

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)
                    
                    // Обновляем прогресс как в кеше
                    runOnUiThread {
                        val progress = ((total * 100) / fileLength).toInt()
                        progressBar.progress = progress
                        tvProgress.text = "$progress% (${formatSize(total)} / ${formatSize(fileLength)})"
                    }
                }

                output.flush()
                output.close()
                input.close()

                runOnUiThread {
                    tvStatus.text = "Обновление готово"
                    findViewById<Button>(R.id.btnConfirmInstall).visibility = View.VISIBLE
                    // Скрываем прогресс-бар после завершения
                    progressBar.visibility = View.INVISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        return String.format("%.2f MB", bytes.toDouble() / (1024 * 1024))
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
}