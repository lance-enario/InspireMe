package com.application.inspireme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val backButton = findViewById<ImageView>(R.id.back_icon_about)
        backButton.setOnClickListener {
            val intent = Intent(this, SettingsPageActivity::class.java)
            startActivity(intent)
        }
    }
}