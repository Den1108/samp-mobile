package com.flyt.mobile

import android.content.Context
import androidx.work.*
import org.json.JSONObject
import java.io.*
import java.net.URL

class UpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val JSON_URL = "https://raw.githubusercontent.com/Den1108/samp-mobile-cache/refs/heads/main/distribution.json"

    override fun doWork(): Result {
        return try {
            // 1. Загружаем манифест (distribution.json)
            val jsonContent = URL(JSON_URL).readText()
            val root = JSONObject(jsonContent)
            
            // Базовый URL для скачивания
            val cdnBase = root.getString("cdnCache")
            val cacheArray = root.getJSONArray("cache")
            val totalFiles = cacheArray.length()

            // 2. Проходим по списку файлов из JSON
            for (i in 0 until totalFiles) {
                val fileObj = cacheArray.getJSONObject(i)
                val rawName = fileObj.getString("name")
                
                // Исправляем слеши для Android
                val cleanPath = rawName.replace("\\", "/")
                val targetFile = File(applicationContext.getExternalFilesDir(null), cleanPath)
                
                // Получаем ожидаемый размер из JSON (массив bytes, первый элемент)
                val bytesArray = fileObj.getJSONArray("bytes")
                val expectedSize = if (bytesArray.length() > 0) bytesArray.getLong(0) else -1L

                // ГЛАВНАЯ ПРОВЕРКА: Если файл существует и его размер совпадает с ожидаемым — пропускаем
                if (targetFile.exists() && (expectedSize == -1L || targetFile.length() == expectedSize)) {
                    updateProgress("Проверка: ${targetFile.name}", (i + 1).toLong(), totalFiles.toLong())
                    continue
                }

                // 3. Скачивание (если файла нет или размер не совпал)
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
        connection.connectTimeout = 15000 
        connection.readTimeout = 15000
        
        val fileLength = connection.contentLengthLong

        // Создаем папки, если их еще нет
        destination.parentFile?.mkdirs()

        connection.getInputStream().use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(16384) 
                var bytesRead: Int
                var totalRead: Long = 0
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (isStopped) return // Если Worker остановлен, прерываем цикл
                    
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    
                    // Отправляем данные в UI (DownloadActivity)
                    setProgressAsync(workDataOf(
                        "msg" to "Загрузка ($fileIndex/$totalFiles): ${destination.name}",
                        "current" to totalRead,
                        "total" to (if (fileLength > 0) fileLength else 1L)
                    ))
                }
            }
        }
    }

    private fun updateProgress(msg: String, current: Long, total: Long) {
        setProgressAsync(workDataOf(
            "msg" to msg,
            "current" to current,
            "total" to total
        ))
    }
}