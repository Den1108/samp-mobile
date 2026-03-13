package org.example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.util.Date

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Записываем лог запуска
        logToFile("App started")

        try {
            val tv = TextView(this)
            tv.text = "Loading..."
            setContentView(tv)
            
            // Пробуем загрузить библиотеку
            System.loadLibrary("samp-mobile")
            tv.text = stringFromJNI()
            
        } catch (e: Exception) {
            logToFile("CRASH: ${e.message}")
            finish() // Закрываем при ошибке
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