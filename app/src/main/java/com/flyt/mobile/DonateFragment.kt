package com.flyt.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class DonateFragment : Fragment(R.layout.fragment_donate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnDonate = view.findViewById<Button>(R.id.btnOpenDonateSite)
        btnDonate.setOnClickListener {
            val url = "https://your-donate-site.com" // Замени на свой сайт
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}