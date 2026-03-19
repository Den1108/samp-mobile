package com.flyt.mobile

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val editNickname = findViewById<EditText>(R.id.editNickname)
        val saveButton = findViewById<Button>(R.id.saveButton)
        // Используем константу для имени настроек
        val prefs = getSharedPreferences("FlytPrefs", MODE_PRIVATE)

        // Загружаем текущий ник
        editNickname.setText(prefs.getString("nickname", "Player"))

        saveButton.setOnClickListener {
            val nickname = editNickname.text.toString().trim()
            
            if (nickname.isNotEmpty()) {
                // 1. Сохраняем в настройки лаунчера
                prefs.edit().putString("nickname", nickname).apply()
                
                // 2. Записываем в файл настроек игры
                saveNicknameToFile(nickname)
                
                Toast.makeText(this, "Никнейм сохранен", Toast.LENGTH_SHORT).show()
                finish() 
            } else {
                Toast.makeText(this, "Введите никнейм!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNicknameToFile(nickname: String) {
        try {
            // Путь к папке SAMP в файлах приложения
            val sampDir = File(getExternalFilesDir(null), "SAMP")
            if (!sampDir.exists()) sampDir.mkdirs()

            val settingsFile = File(sampDir, "settings.ini")
            
            // Формат INI, который понимает SAMP
            val content = "[client]\nname=$nickname\n"
            settingsFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}