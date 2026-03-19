package com.flyt.mobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.concurrent.thread

class PlayFragment : Fragment(R.layout.fragment_play) {

    // Те же настройки, что были в MainActivity
    private val CURRENT_APP_VERSION = 1.4
    private val APP_VERSION_URL = "http://192.168.31.178:3000/app_version.txt"
    private val APK_URL = "http://192.168.31.178:3000/latest_launcher.apk"

    private lateinit var mainLayout: LinearLayout
    private lateinit var updateLayout: LinearLayout
    private lateinit var downloadLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnConfirmInstall: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация элементов внутри фрагмента
        mainLayout = view.findViewById(R.id.mainLayout)
        updateLayout = view.findViewById(R.id.updateLayout)
        downloadLayout = view.findViewById(R.id.downloadLayout)
        progressBar = view.findViewById(R.id.updateProgressBar)
        tvProgress = view.findViewById(R.id.tvUpdateProgress)
        tvStatus = view.findViewById(R.id.tvUpdateStatus)
        btnConfirmInstall = view.findViewById(R.id.btnConfirmInstall)

        val playButton = view.findViewById<Button>(R.id.playButton)
        val btnDownloadUpdate = view.findViewById<Button>(R.id.btnDownloadUpdate)

        // Логика кнопки ИГРАТЬ
        playButton.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("FlytPrefs", Context.MODE_PRIVATE)
            val nickname = prefs.getString("nickname", "")

            if (nickname.isNullOrEmpty() || nickname == "Player") {
                Toast.makeText(requireContext(), "Сначала установите ник в настройках!", Toast.LENGTH_SHORT).show()
                // Здесь можно добавить программное переключение на вкладку настроек, если нужно
            } else {
                // Переход к загрузке кэша игры
                startActivity(Intent(requireContext(), DownloadActivity::class.java))
            }
        }

        // Логика кнопки ЗАГРУЗИТЬ ОБНОВЛЕНИЕ
        btnDownloadUpdate.setOnClickListener {
            updateLayout.visibility = View.GONE
            downloadLayout.visibility = View.VISIBLE
            startApkDownload()
        }

        // Логика кнопки ОБНОВИТЬ (установка APK)
        btnConfirmInstall.setOnClickListener {
            val apkFile = File(requireContext().getExternalFilesDir(null), "latest_launcher.apk")
            installApk(apkFile)
        }

        // Проверка обновления при открытии вкладки
        checkAppUpdate()
    }

    private fun checkAppUpdate() {
        thread {
            try {
                val remoteVersionStr = URL(APP_VERSION_URL).readText().trim()
                val remoteVersion = remoteVersionStr.toDouble()
                if (remoteVersion > CURRENT_APP_VERSION) {
                    activity?.runOnUiThread {
                        mainLayout.visibility = View.GONE
                        updateLayout.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startApkDownload() {
        thread {
            try {
                val apkFile = File(requireContext().getExternalFilesDir(null), "latest_launcher.apk")
                val connection = URL(APK_URL).openConnection()
                connection.connect()
                val fileLength = connection.contentLengthLong

                URL(APK_URL).openStream().use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val data = ByteArray(8192)
                        var total: Long = 0
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            total += count
                            output.write(data, 0, count)
                            val progress = ((total * 100) / fileLength).toInt()
                            
                            activity?.runOnUiThread {
                                progressBar.progress = progress
                                tvProgress.text = "$progress% (${formatSize(total)} / ${formatSize(fileLength)})"
                            }
                        }
                    }
                }

                activity?.runOnUiThread {
                    tvStatus.text = "Обновление готово"
                    btnConfirmInstall.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        return String.format("%.2f MB", bytes.toDouble() / (1024 * 1024))
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}