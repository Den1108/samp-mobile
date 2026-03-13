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
    
    val tv = TextView(this)
    tv.textSize = 20f
    setContentView(tv)
    
    try {
        System.loadLibrary("samp-mobile")
        tv.text = stringFromJNI()
    } catch (e: Throwable) {
        // Выводим ошибку прямо на экран, чтобы ты её увидел!
        tv.text = "ERROR: ${e.javaClass.simpleName}\n${e.message}"
        logToFile("CRASH: ${e.message}")
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