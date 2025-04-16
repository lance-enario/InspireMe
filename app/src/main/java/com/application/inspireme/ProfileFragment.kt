package com.application.inspireme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var bannerImageView: ImageView
    private lateinit var profilePic: CircleImageView
    private lateinit var usernameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Initialize views with correct IDs from fragment_profile.xml
        bannerImageView = view.findViewById(R.id.banner_image)
        profilePic = view.findViewById(R.id.profile_picture)
        usernameTextView = view.findViewById(R.id.username_text)
        bioTextView = view.findViewById(R.id.bio_text)

        // Load data from SharedPreferences
        loadSavedData()

        // Set up Settings button click listener
        view.findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsPageActivity::class.java)
            intent.putExtra("previous_fragment", "ProfileFragment")
            startActivity(intent)
        }

        // Set up Edit Profile button click listener
        view.findViewById<MaterialButton>(R.id.editProfileButton).setOnClickListener {
            val intent = Intent(requireContext(), ProfileSettingsActivity::class.java)
            intent.putExtra("previous_fragment", "ProfileFragment")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to the fragment
        loadSavedData()
    }

    private fun loadSavedData() {
        // Get custom image paths
        val customBannerUri = sharedPreferences.getString("customBannerUri", null)
        val customProfileUri = sharedPreferences.getString("customProfileUri", null)

        // Get fallback resource IDs
        val bannerResId = sharedPreferences.getInt("bannerImageResId", R.drawable.banner3)
        val profileResId = sharedPreferences.getInt("profileImageResId", R.drawable.profile)

        // Try to load custom banner image
        if (customBannerUri != null) {
            loadImageFromUri(customBannerUri, bannerImageView)
        } else {
            bannerImageView.setImageResource(bannerResId)
            // Add green tint to match the app theme
            bannerImageView.setColorFilter(requireContext().getColor(R.color.green))
        }

        // Try to load custom profile image
        if (customProfileUri != null) {
            loadImageFromUri(customProfileUri, profilePic)
        } else {
            profilePic.setImageResource(profileResId)
        }

        // Load text data
        val username = sharedPreferences.getString("username", "Default Name") ?: "Default Name"
        val bio = sharedPreferences.getString("bio", "No bio available") ?: "No bio available"

        usernameTextView.text = username
        bioTextView.text = bio
    }

    private fun loadImageFromUri(uri: String, imageView: ImageView) {
        val imageFile = File(uri)
        if (imageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            imageView.setImageBitmap(bitmap)
            // Remove any color filters that may have been applied
            imageView.clearColorFilter()
        }
    }
}