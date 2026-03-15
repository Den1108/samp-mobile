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

        btnInstall.setOnClickListener {
            btnInstall.isEnabled = false
            val work = OneTimeWorkRequestBuilder<UpdateWorker>().build()
            WorkManager.getInstance(this).enqueue(work)

            WorkManager.getInstance(this).getWorkInfoByIdLiveData(work.id).observe(this) { info ->
                if (info != null) {
                    val msg = info.progress.getString("msg") ?: "Подготовка..."
                    val current = info.progress.getLong("current", 0)
                    val total = info.progress.getLong("total", 1)

                    statusText.text = msg
                    progressBar.max = 100
                    progressBar.progress = ((current * 100) / total).toInt()
                    progressDetail.text = "${current / 1024} KB / ${total / 1024} KB"

                    if (info.state == WorkInfo.State.SUCCEEDED) finish()
                }
            }
        }
    }
}