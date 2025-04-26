package com.application.inspireme

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.application.inspireme.helper.SettingItem
import com.application.inspireme.helper.SettingsAdapter
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

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

        val usernameDisplay = findViewById<TextView>(R.id.username_display)
        val userEmail = findViewById<TextView>(R.id.user_email)
        val profilePictureSmall = findViewById<CircleImageView>(R.id.profile_picture_small)

        // Get user information from SharedPreferences
        val userProfilePrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val userAuthPrefs = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)

        // Set the username
        usernameDisplay.text = userProfilePrefs.getString("username", "Username")

        // Set the email if user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userEmail.text = currentUser.email
        } else {
            userEmail.text = "Not logged in"
        }

        // Load profile picture if available
        val customProfileUri = userProfilePrefs.getString("customProfileUri", null)
        if (customProfileUri != null) {
            val imageFile = File(customProfileUri)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                profilePictureSmall.setImageBitmap(bitmap)
            }
        } else {
            // Use default profile image
            profilePictureSmall.setImageResource(R.drawable.profile)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val previousFragment = intent.getStringExtra("previous_fragment")
        val intent = Intent(this, NavigationBarActivity::class.java)

        when (previousFragment) {
            "ProfileFragment" -> intent.putExtra("openProfileFragment", true)
            "HomeFragment" -> intent.putExtra("openProfileFragment", false)
        }

        startActivity(intent)
        finish()
    }

    fun showLogoutDialog() {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_logout)
    dialog.setCancelable(false)

    val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
    val logoutButton = dialog.findViewById<Button>(R.id.logout_button)

    cancelButton.setOnClickListener { dialog.dismiss() }
    logoutButton.setOnClickListener {
        // Sign out from Firebase Auth
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        
        // Clear logged in state in SharedPreferences
        val sharedPreferences = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Clear all preferences
        editor.apply()
        
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