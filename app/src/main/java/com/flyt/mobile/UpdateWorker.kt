package com.flyt.mobile

import android.content.Context
import androidx.work.*
import org.json.JSONObject
import java.io.*
import java.net.URL

class UpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val jsonUrl = "https://raw.githubusercontent.com/Den1108/samp-mobile-cache/refs/heads/main/distribution.json"
        val jsonContent = URL(jsonUrl).readText()
        val cacheArray = JSONObject(jsonContent).getJSONArray("cache")

        for (i in 0 until cacheArray.length()) {
        val fileObj = cacheArray.getJSONObject(i)
        val rawName = fileObj.getString("name")
        val cleanPath = rawName.replace("\\", "/")
        val fileUrl = JSONObject(jsonContent).getString("cdnCache") + cleanPath
        val targetFile = File(applicationContext.getExternalFilesDir(null), cleanPath)
    
        val connection = URL(fileUrl).openConnection()
        val fileLength = connection.contentLengthLong
    
        targetFile.parentFile?.mkdirs()
    
        connection.getInputStream().use { input ->
            FileOutputStream(targetFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead: Long = 0
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                
                    // Передаем данные в DownloadActivity
                    setProgressAsync(workDataOf(
                        "msg" to "Загрузка: ${targetFile.name}",
                        "current" to totalRead,
                        "total" to (if (fileLength > 0) fileLength else 1) // Защита от деления на 0
                    ))
                }
            }
        }
    }
        return Result.success()
    }
}