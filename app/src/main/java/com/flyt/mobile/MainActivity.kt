package com.flyt.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.net.URL
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        playButton.setOnClickListener {
            // Запускаем проверку в отдельном потоке, чтобы не вешать экран
            thread {
                val downloaded = areAllFilesDownloaded()
                
                runOnUiThread {
                    if (downloaded) {
                        val nickname = getSharedPreferences("FlytPrefs", MODE_PRIVATE)
                            .getString("nickname", "Player") ?: "Player"
                        
                        // Путь к конфигу игры
                        val gtaSaSetPath = File(getExternalFilesDir(null), "files/gta_sa.set").absolutePath
                        
                        Toast.makeText(this, "Запуск игры...", Toast.LENGTH_SHORT).show()
                        launchGame(nickname, gtaSaSetPath)
                    } else {
                        startActivity(Intent(this, DownloadActivity::class.java))
                        Toast.makeText(this, "Нужно обновить файлы", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // Эта функция теперь проверяет файлы более внимательно
    private fun areAllFilesDownloaded(): Boolean {
        return try {
            val url = URL("https://raw.githubusercontent.com/Den1108/samp-mobile-cache/refs/heads/main/distribution.json")
            val jsonContent = url.readText()
            val cacheArray = JSONObject(jsonContent).getJSONArray("cache")

            for (i in 0 until cacheArray.length()) {
                val fileObj = cacheArray.getJSONObject(i)
                val rawPath = fileObj.getString("name").replace("\\", "/")
                
                // ВАЖНО: убедитесь, что путь совпадает с тем, куда качает Worker
                val targetFile = File(getExternalFilesDir(null), rawPath)
                val expectedSize = fileObj.getJSONArray("bytes").getLong(0)

                if (!targetFile.exists()) {
                    println("ФАЙЛ ОТСУТСТВУЕТ: ${targetFile.absolutePath}")
                    return false
                }
                
                if (targetFile.length() != expectedSize) {
                    println("РАЗМЕР НЕ СОВПАДАЕТ: ${targetFile.name} (Есть: ${targetFile.length()}, Надо: $expectedSize)")
                    return false
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    external fun launchGame(nickname: String, path: String): String
}