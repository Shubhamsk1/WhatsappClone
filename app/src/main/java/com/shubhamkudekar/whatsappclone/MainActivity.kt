package com.shubhamkudekar.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.shubhamkudekar.whatsappclone.adapters.ScreenSliderAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewpager.adapter= ScreenSliderAdapter(this)
        TabLayoutMediator(tabs, viewpager) { tab, position ->
            when (position) {
                0 -> tab.text = "CHATS"
                1 -> tab.text = "PEOPLE"
            }
        }.attach()
    }
}