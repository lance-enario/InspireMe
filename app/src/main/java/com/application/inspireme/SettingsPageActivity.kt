package com.application.inspireme

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import com.application.inspireme.helper.SettingItem
import com.application.inspireme.helper.SettingsAdapter

class SettingsPageActivity : Activity() {
    private var previousScreen: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        previousScreen = intent.getStringExtra("previous_screen")
        val backButton = findViewById<ImageView>(R.id.back_icon_left)

        val settingsList = listOf(
            SettingItem(R.drawable.account_icon, "Account", R.drawable.arrow),
            SettingItem(R.drawable.notifications_icon, "Notifications", R.drawable.arrow),
            SettingItem(R.drawable.privacy_icon, "Privacy", R.drawable.arrow),
            SettingItem(R.drawable.language_icon, "Language", R.drawable.arrow),
            SettingItem(R.drawable.about_icon, "About", R.drawable.arrow),
            SettingItem(R.drawable.logout_icon, "Logout", null)
        )

        val adapter = SettingsAdapter(this, settingsList)
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val setting = settingsList[position]
            when (setting.text) {
                "Account" -> startActivity(Intent(this, AccountActivity::class.java))
                "Notifications" -> startActivity(Intent(this, NotificationsActivity::class.java))
                "Privacy" -> startActivity(Intent(this, PrivacyActivity::class.java))
                "Language" -> startActivity(Intent(this, LanguageActivity::class.java))
                "About" -> startActivity(Intent(this, AboutActivity::class.java))
                "Logout" -> showLogoutDialog()
            }
        }

        backButton.setOnClickListener {
            val intent = when (previousScreen) {
                "ProfilePageActivity" -> Intent(this, ProfilePageActivity::class.java)
                "LandingPageActivity" -> Intent(this, LandingPageActivity::class.java)
                else -> Intent(this, LandingPageActivity::class.java)
            }
            startActivity(intent)
        }
    }
    fun showLogoutDialog() {
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
