package com.application.inspireme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var bannerImageView: ImageView
    private lateinit var profilePic: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    private val predefinedBanners = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)
    private val predefinedProfiles = listOf(R.drawable.male, R.drawable.female, R.drawable.punpun, R.drawable.profile)

    private var selectedBannerResId = R.drawable.bgoranges3
    private var selectedProfileResId = R.drawable.profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        bannerImageView = findViewById(R.id.bannerImageView)
        profilePic = findViewById(R.id.ProfilePic)
        usernameEditText = findViewById(R.id.Username)
        bioEditText = findViewById(R.id.Bio)
        val saveButton: Button = findViewById(R.id.Save)
        val cancelButton: Button = findViewById(R.id.Cancel)

        loadSavedData()

        bannerImageView.setOnClickListener { pickBannerImage() }
        profilePic.setOnClickListener { pickProfileImage() }

        saveButton.setOnClickListener { saveData() }
        cancelButton.setOnClickListener { finish() }
    }

    private fun pickBannerImage() {

        selectedBannerResId = predefinedBanners[(predefinedBanners.indexOf(selectedBannerResId) + 1) % predefinedBanners.size]
        bannerImageView.setImageResource(selectedBannerResId)
    }

    private fun pickProfileImage() {

        selectedProfileResId = predefinedProfiles[(predefinedProfiles.indexOf(selectedProfileResId) + 1) % predefinedProfiles.size]
        profilePic.setImageResource(selectedProfileResId)
    }

    private fun loadSavedData() {
        selectedBannerResId = sharedPreferences.getInt("bannerImageResId", R.drawable.banner3)
        selectedProfileResId = sharedPreferences.getInt("profileImageResId", R.drawable.profile)

        bannerImageView.setImageResource(selectedBannerResId)
        profilePic.setImageResource(selectedProfileResId)
        usernameEditText.setText(sharedPreferences.getString("username", ""))
        bioEditText.setText(sharedPreferences.getString("bio", ""))
    }

    private fun saveData() {
        sharedPreferences.edit()
            .putInt("bannerImageResId", selectedBannerResId)
            .putInt("profileImageResId", selectedProfileResId)
            .putString("username", usernameEditText.text.toString())
            .putString("bio", bioEditText.text.toString())
            .apply()

        startActivity(Intent(this, ProfileFragment::class.java))
        finish()
    }
}
