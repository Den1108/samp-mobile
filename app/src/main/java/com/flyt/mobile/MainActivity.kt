package com.flyt.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Слушатель нажатий на нижнюю панель
        bottomNav.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
    
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_news -> NewsFragment()
                R.id.nav_play -> PlayFragment()
                R.id.nav_donate -> DonateFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> PlayFragment()
            }

            // Проверяем, не открыт ли уже этот же фрагмент, чтобы не перезагружать его
            if (currentFragment?.javaClass != selectedFragment.javaClass) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            }
            true
        }

        // При первом запуске открываем вкладку "Играть"
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_play
        }
    }
}