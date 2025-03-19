package com.application.inspireme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class LandingPageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        val profileButton = findViewById<ImageButton>(R.id.Profilebutton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<ImageButton>(R.id.button_settings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsPageActivity::class.java)
            intent.putExtra("previous_screen", "LandingPageActivity")
            startActivity(intent)
        }
    }
}