package com.flyt.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val editNickname = findViewById<EditText>(R.id.editNickname)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val prefs = getSharedPreferences("FlytPrefs", MODE_PRIVATE)

        // Читаем сохраненный ник
        editNickname.setText(prefs.getString("nickname", "Player"))

        saveButton.setOnClickListener {
            prefs.edit().putString("nickname", editNickname.text.toString()).apply()
            finish() // Закрываем экран после сохранения
        }
    }
}