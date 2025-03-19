package com.application.inspireme

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.developer_screen)

        val backButton = findViewById<ImageView>(R.id.back_icon_about)
        backButton.setOnClickListener {
            val intent = Intent(this, SettingsPageActivity::class.java)
            startActivity(intent)
        }
    }
}