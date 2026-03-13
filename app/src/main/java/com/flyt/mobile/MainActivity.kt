package com.flyt.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("samp-mobile")

        val statusText = findViewById<TextView>(R.id.statusText)
        val playButton = findViewById<Button>(R.id.playButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        // Кнопка ИГРАТЬ: достаем ник и вызываем C++
        playButton.setOnClickListener {
            val prefs = getSharedPreferences("FlytPrefs", MODE_PRIVATE)
            val nickname = prefs.getString("nickname", "Player") ?: "Player"
            
            // Вызываем новую функцию, передавая туда ник
            val result = launchGame(nickname)
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // Раньше была stringFromJNI, теперь заменяем на launchGame
    external fun launchGame(nickname: String): String
}