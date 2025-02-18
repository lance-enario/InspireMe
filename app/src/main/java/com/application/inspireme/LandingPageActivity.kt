package com.application.inspireme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LandingPageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.landing_page)

        var intent;

        val button = findViewById<ImageButton>(R.id.Profilebutton)
        button.setOnClickListener {
            intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }

        val settings_button = findViewById(R.id.button_settings)
        settings_button.setOnClickListener {
            intent = Intent(this, SettingsPageActivity::class.java)
        }
    }
}