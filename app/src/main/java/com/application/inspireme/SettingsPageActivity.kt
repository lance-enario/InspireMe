package com.application.inspireme

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class SettingsPageActivity : Activity() {
    private var previousScreen: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)

        val aboutButton = findViewById<ConstraintLayout>(R.id.about_constraint)
        aboutButton.setOnClickListener {
            val aboutintent = Intent(this, AboutActivity::class.java)
            startActivity(aboutintent)
        }

        previousScreen = intent.getStringExtra("previous_screen")
        val backButton = findViewById<ImageView>(R.id.back_icon_left)
        backButton.setOnClickListener {
            val intent = when (previousScreen) {
                "ProfilePageActivity" -> Intent(this, ProfilePageActivity::class.java)
                "LandingPageActivity" -> Intent(this, LandingPageActivity::class.java)
                else -> Intent(this, LandingPageActivity::class.java)
            }
            startActivity(intent)
        }
    }
    fun showLogoutDialog(view: View) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(false)

        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
        val logoutButton = dialog.findViewById<Button>(R.id.logout_button)

        cancelButton.setOnClickListener { dialog.dismiss() }
        logoutButton.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()
    }
}
