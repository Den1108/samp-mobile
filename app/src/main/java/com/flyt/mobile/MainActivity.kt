package com.flyt.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        
        playButton.setOnClickListener {
            val nickname = getSharedPreferences("FlytPrefs", MODE_PRIVATE)
                .getString("nickname", "Player") ?: "Player"
            
            val cacheDir = getExternalFilesDir(null)?.absolutePath ?: ""
            val gtaSaSetPath = "$cacheDir/files/gta_sa.set"
            
            if (checkCache(gtaSaSetPath)) {
                val result = launchGame(nickname, gtaSaSetPath)
                // Запуск игры...
            } else {
                // Переход на экран загрузки
                startActivity(Intent(this, DownloadActivity::class.java))
            }
        }
    }

    private fun checkCache(path: String): Boolean = File(path).exists()

    external fun launchGame(nickname: String, path: String): String
}