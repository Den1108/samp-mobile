package com.flyt.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.net.URL
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        
        playButton.setOnClickListener {
            if (areAllFilesDownloaded()) {
                val nickname = getSharedPreferences("FlytPrefs", MODE_PRIVATE).getString("nickname", "Player") ?: "Player"
                val gtaSaSetPath = "${getExternalFilesDir(null)?.absolutePath}/files/gta_sa.set"
                launchGame(nickname, gtaSaSetPath)
            } else {
                // Если файлов нет — принудительно открываем экран загрузки
                startActivity(Intent(this, DownloadActivity::class.java))
                Toast.makeText(this, "Сначала скачайте игровые файлы!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCache(path: String): Boolean = File(path).exists()

    external fun launchGame(nickname: String, path: String): String

    private fun areAllFilesDownloaded(): Boolean {
        val jsonContent = try { URL("https://raw.githubusercontent.com/Den1108/samp-mobile-cache/refs/heads/main/distribution.json").readText() } catch (e: Exception) { return false }
        val cacheArray = JSONObject(jsonContent).getJSONArray("cache")

        for (i in 0 until cacheArray.length()) {
            val fileObj = cacheArray.getJSONObject(i)
            val path = fileObj.getString("name").replace("\\", "/")
            val targetFile = File(getExternalFilesDir(null), path)
            val expectedSize = fileObj.getJSONArray("bytes").getLong(0)
        
            // Если хоть одного файла нет или размер не совпадает — возвращаем false
            if (!targetFile.exists() || targetFile.length() != expectedSize) {
                return false
            }
        }
        return true
    }
}