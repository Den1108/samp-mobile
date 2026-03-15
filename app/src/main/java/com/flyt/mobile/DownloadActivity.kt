package com.flyt.mobile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*

class DownloadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val statusText = findViewById<TextView>(R.id.statusText)
        val progressDetail = findViewById<TextView>(R.id.progressDetail)
        val btnInstall = findViewById<Button>(R.id.btnInstall)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        val workManager = WorkManager.getInstance(this)
        workManager.getWorkInfosForUniqueWork("GAME_DOWNLOAD").get().let { infos ->
            if (infos.any { it.state == WorkInfo.State.RUNNING }) {
                Toast.makeText(this, "Загрузка уже идет!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        btnInstall.setOnClickListener {
            btnInstall.isEnabled = false
            
            // Уникальная задача: если уже идет - не запускаем новую
            val workRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
                .addTag("game_update")
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                "GAME_DOWNLOAD", 
                ExistingWorkPolicy.KEEP, 
                workRequest
            )
        }

        // Подписываемся на прогресс (автоматически привяжется к текущей задаче)
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData("GAME_DOWNLOAD")
            .observe(this) { workInfos ->
                val info = workInfos?.firstOrNull() ?: return@observe
                
                val msg = info.progress.getString("msg") ?: "Ожидание..."
                val current = info.progress.getLong("current", 0)
                val total = info.progress.getLong("total", 1)

                statusText.text = msg
                progressBar.max = 100
                progressBar.progress = ((current * 100) / total).toInt()
                progressDetail.text = "${current / 1024} KB / ${total / 1024} KB"

                if (info.state == WorkInfo.State.SUCCEEDED) {
                    Toast.makeText(this, "Готово!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        btnCancel.setOnClickListener { 
            WorkManager.getInstance(this).cancelUniqueWork("GAME_DOWNLOAD")
            finish() 
        }
    }
}