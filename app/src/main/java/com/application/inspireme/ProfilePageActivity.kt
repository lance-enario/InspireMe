package com.application.inspireme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class ProfilePageActivity : Activity() {
    private lateinit var bannerImageView: ImageView
    private lateinit var profilePic: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        bannerImageView = findViewById(R.id.BannerPic)
        profilePic = findViewById(R.id.ProfilePic)
        usernameTextView = findViewById(R.id.usernameee)
        bioTextView = findViewById(R.id.biotextView)

        loadSavedData()

        findViewById<ImageButton>(R.id.Homebutton).setOnClickListener {
            startActivity(Intent(this, LandingPageActivity::class.java))
        }

        findViewById<ImageButton>(R.id.profileSettings).setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(this, SettingsPageActivity::class.java)
            intent.putExtra("previous_screen", "ProfilePageActivity")
            startActivity(intent)
        }
    }

    private fun loadSavedData() {
        val bannerResId = sharedPreferences.getInt("bannerImageResId", R.drawable.banner3)
        val profileResId = sharedPreferences.getInt("profileImageResId", R.drawable.profile)
        val username = sharedPreferences.getString("username", "Default Name") ?: "Default Name"
        val bio = sharedPreferences.getString("bio", "No bio available") ?: "No bio available"

        bannerImageView.setImageResource(bannerResId)
        profilePic.setImageResource(profileResId)

        usernameTextView.text = username
        bioTextView.text = bio
    }
}
