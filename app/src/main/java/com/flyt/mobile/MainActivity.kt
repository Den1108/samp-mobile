package com.flyt.mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.util.Date

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val tv = TextView(this)
    setContentView(tv)

    // Записываем что-нибудь в лог
    val logFile = File(getExternalFilesDir(null), "samp_debug.txt")
    try {
        FileWriter(logFile, true).use { it.append("Test log entry: ${Date()}\n") }
        
        // ЧИТАЕМ лог обратно и выводим на экран
        val logContent = logFile.readText()
        tv.text = "Файл создан: ${logFile.absolutePath}\n\nЛог:\n$logContent"
    } catch (e: Exception) {
        tv.text = "Ошибка: ${e.message}"
    }
}

    private fun logToFile(message: String) {
        try {
            val logFile = File(getExternalFilesDir(null), "samp_debug.txt")
            FileWriter(logFile, true).use {
                it.append("${Date()}: $message\n")
            }
        } catch (e: Exception) { }
    }

    external fun stringFromJNI(): String
}