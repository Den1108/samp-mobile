package com.flyt.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Подключаем наш XML

        val statusText = findViewById<TextView>(R.id.statusText)
        val playButton = findViewById<Button>(R.id.playButton)

        // Вызываем функцию из C++
        System.loadLibrary("samp-mobile")
        statusText.text = stringFromJNI()

        playButton.setOnClickListener {
            Toast.makeText(this, "Запуск игры...", Toast.LENGTH_SHORT).show()
            // Здесь позже будет вызов C++ функции для старта движка
        }
    }

    external fun stringFromJNI(): String
}