package com.application.inspireme

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.application.inspireme.helper.SettingItem
import com.application.inspireme.helper.SettingsAdapter

class SettingsPageActivity : AppCompatActivity() {
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
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        when (previousScreen) {
            "HomeFragment" -> {
                val intent = Intent(this, NavigationBarActivity::class.java)
                startActivity(intent)
                finish()
            }
            "ProfileFragment" -> {
                val intent = Intent(this, NavigationBarActivity::class.java)
                intent.putExtra("openProfileFragment", true)
                startActivity(intent)
                finish()
            }
            else -> {
                super.onBackPressed()
            }
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
            // Clear logged in state
            val sharedPreferences = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        dialog.show()
    }
}