package com.flyt.mobile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.File

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editNickname = view.findViewById<EditText>(R.id.editNickname)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val prefs = requireContext().getSharedPreferences("FlytPrefs", Context.MODE_PRIVATE)

        // Загружаем текущий ник
        editNickname.setText(prefs.getString("nickname", "Player"))

        saveButton.setOnClickListener {
            val nickname = editNickname.text.toString().trim()
            
            if (nickname.isNotEmpty()) {
                // 1. Сохраняем в память лаунчера
                prefs.edit().putString("nickname", nickname).apply()
                
                // 2. Записываем в файл для игры
                saveNicknameToFile(nickname)
                
                Toast.makeText(requireContext(), "Никнейм сохранен!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Ник не может быть пустым", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNicknameToFile(nickname: String) {
        try {
            val sampDir = File(requireContext().getExternalFilesDir(null), "SAMP")
            if (!sampDir.exists()) sampDir.mkdirs()
            File(sampDir, "settings.ini").writeText("[client]\nname=$nickname\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}