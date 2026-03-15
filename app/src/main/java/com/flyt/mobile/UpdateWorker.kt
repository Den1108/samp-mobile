package com.flyt.mobile

import android.content.Context
import androidx.work.*
import org.json.JSONObject
import java.io.*
import java.net.URL

class UpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val jsonUrl = "https://raw.githubusercontent.com/Den1108/samp-mobile-cache/refs/heads/main/distribution.json"
            val jsonContent = URL(jsonUrl).readText()
            val root = JSONObject(jsonContent)
            val cdnBase = root.getString("cdnCache")
            val cacheArray = root.getJSONArray("cache")
            val totalFiles = cacheArray.length()

            for (i in 0 until totalFiles) {
                val fileObj = cacheArray.getJSONObject(i)
                val rawName = fileObj.getString("name")
                val cleanPath = rawName.replace("\\", "/")
                val targetFile = File(applicationContext.getExternalFilesDir(null), cleanPath)
                
                // Получаем ожидаемый размер (безопасное чтение)
                val bytesArray = fileObj.getJSONArray("bytes")
                val expectedSize = if (bytesArray.length() > 0) bytesArray.getLong(0) else -1L

                // ПРОВЕРКА: Если файл существует и размер совпадает — пропускаем
                if (targetFile.exists() && (expectedSize == -1L || targetFile.length() == expectedSize)) {
                    continue
                }

                // Скачиваем, если файла нет или он поврежден
                val fileUrl = cdnBase + cleanPath
                downloadFile(fileUrl, targetFile, i + 1, totalFiles)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun downloadFile(url: String, destination: File, fileIndex: Int, totalFiles: Int) {
        val connection = URL(url).openConnection()
        val fileLength = connection.contentLengthLong

        destination.parentFile?.mkdirs()

        connection.getInputStream().use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead: Long = 0
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    
                    setProgressAsync(workDataOf(
                        "msg" to "Файл $fileIndex из $totalFiles: ${destination.name}",
                        "current" to totalRead,
                        "total" to (if (fileLength > 0) fileLength else 1L)
                    ))
                }
            }
        }
    }
}