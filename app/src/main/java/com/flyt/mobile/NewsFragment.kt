package com.flyt.mobile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class NewsFragment : Fragment(R.layout.fragment_news) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Здесь в будущем будет код для загрузки новостей из интернета
    }
}