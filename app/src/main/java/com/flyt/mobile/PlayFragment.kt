package com.flyt.mobile

import android.content.Context
import android.content.Intent
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

    private val CURRENT_APP_VERSION = 1.4
    private val APP_VERSION_URL = "http://192.168.31.178:3000/app_version.txt"
    private val APK_URL = "http://192.168.31.178:3000/latest_launcher.apk"

    private lateinit var mainLayout: LinearLayout
    private lateinit var updateLayout: LinearLayout
    private lateinit var downloadLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvStatus: TextView // Мы используем это имя
    private lateinit var btnConfirmInstall: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainLayout = view.findViewById(R.id.mainLayout)
        updateLayout = view.findViewById(R.id.updateLayout)
        downloadLayout = view.findViewById(R.id.downloadLayout)
        
        progressBar = view.findViewById(R.id.updateProgressBar)
        tvProgress = view.findViewById(R.id.tvUpdateProgress)
        // ВАЖНО: Привязываем tvStatus к ID из XML. 
        // Если в fragment_play.xml нет TextView с id tvUpdateStatus, создай его или используй существующий
        tvStatus = view.findViewById(R.id.tvUpdateProgress) 

        val playButton = view.findViewById<Button>(R.id.playButton)
        val btnDownloadUpdate = view.findViewById<Button>(R.id.btnDownloadUpdate)
        btnConfirmInstall = view.findViewById(R.id.btnConfirmInstall)

        updateLayout.visibility = View.GONE
        downloadLayout.visibility = View.GONE

        checkAppUpdate()

        playButton.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("FlytPrefs", Context.MODE_PRIVATE)
            val nickname = prefs.getString("nickname", "")

            if (nickname.isNullOrEmpty() || nickname == "Player") {
                Toast.makeText(requireContext(), "Сначала установите никнейм в настройках!", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(requireContext(), DownloadActivity::class.java))
            }
        }

        btnDownloadUpdate.setOnClickListener {
            showDownloadUI()
            downloadAndInstallApk()
        }
        
        btnConfirmInstall.setOnClickListener {
            val apkFile = File(requireContext().getExternalFilesDir(null), "update.apk")
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

    private fun showDownloadUI() {
        updateLayout.visibility = View.GONE
        downloadLayout.visibility = View.VISIBLE
        tvStatus.text = "Загрузка обновления..." // Теперь ошибка исчезнет
    }

    private fun downloadAndInstallApk() {
        thread {
            try {
                val apkFile = File(requireContext().getExternalFilesDir(null), "update.apk")
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
                    
                    val progress = ((total * 100) / fileLength).toInt()
                    activity?.runOnUiThread {
                        progressBar.progress = progress
                        tvProgress.text = "$progress% (${formatSize(total)} / ${formatSize(fileLength)})"
                    }
                }

                output.flush()
                output.close()
                input.close()

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