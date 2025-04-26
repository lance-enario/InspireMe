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
import android.widget.Toast
import com.application.inspireme.data.UserProfileCache
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    val userAuthPrefs = requireContext().getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
    val userId = userAuthPrefs.getString("userId", null)

    // Initialize views with correct IDs from fragment_profile.xml
    bannerImageView = view.findViewById(R.id.banner_image)
    profilePic = view.findViewById(R.id.profile_picture)
    usernameTextView = view.findViewById(R.id.username_text)
    bioTextView = view.findViewById(R.id.bio_text)

    // Check if we already have cached data
    if (UserProfileCache.isDataLoaded) {
        // Use cached data
        displayCachedData()
    } else {
        // Load data from Firebase if the user is logged in
        if (userId != null) {
            loadUserDataFromFirebase(userId)
        } else {
            // Fallback to SharedPreferences if not logged in
            loadSavedData()
        }
    }

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

    private fun displayCachedData() {
    // Display the username and bio
    usernameTextView.text = UserProfileCache.username
    bioTextView.text = UserProfileCache.bio
    
    // Try to load custom banner image
    if (UserProfileCache.customBannerUri != null) {
        loadImageFromUri(UserProfileCache.customBannerUri!!, bannerImageView)
    } else {
        bannerImageView.setImageResource(UserProfileCache.bannerResId)
        bannerImageView.setColorFilter(requireContext().getColor(R.color.green))
    }
    
    // Try to load custom profile image
    if (UserProfileCache.customProfileUri != null) {
        loadImageFromUri(UserProfileCache.customProfileUri!!, profilePic)
    } else {
        profilePic.setImageResource(UserProfileCache.profileResId)
    }
}

    override fun onResume() {
        super.onResume()
        // Reload data when returning to the fragment
        val userAuthPrefs = requireContext().getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
        val userId = userAuthPrefs.getString("userId", null)
        
        if (userId != null) {
            loadUserDataFromFirebase(userId)
        } else {
            loadSavedData()
        }
    }

    // Modify loadUserDataFromFirebase

private fun loadUserDataFromFirebase(userId: String) {
    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("users").child(userId)
    
    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                // Update cache
                UserProfileCache.username = snapshot.child("username").getValue(String::class.java) ?: "Default Name"
                UserProfileCache.bio = snapshot.child("bio").getValue(String::class.java) ?: "No bio available"
                
                // Get custom image paths from SharedPreferences
                UserProfileCache.customBannerUri = sharedPreferences.getString("customBannerUri", null)
                UserProfileCache.customProfileUri = sharedPreferences.getString("customProfileUri", null)
                
                // Get fallback resource IDs
                UserProfileCache.bannerResId = sharedPreferences.getInt("bannerImageResId", R.drawable.banner3)
                UserProfileCache.profileResId = sharedPreferences.getInt("profileImageResId", R.drawable.profile)
                
                // Mark data as loaded
                UserProfileCache.isDataLoaded = true
                UserProfileCache.lastUpdateTime = System.currentTimeMillis()
                
                // Display the data
                displayCachedData()
            } else {
                loadSavedData()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(requireContext(), "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            loadSavedData()
        }
    })
}

    private fun loadSavedData() {
        UserProfileCache.customBannerUri = sharedPreferences.getString("customBannerUri", null)
        UserProfileCache.customProfileUri = sharedPreferences.getString("customProfileUri", null)
        UserProfileCache.bannerResId = sharedPreferences.getInt("bannerImageResId", R.drawable.banner3)
        UserProfileCache.profileResId = sharedPreferences.getInt("profileImageResId", R.drawable.profile)
        UserProfileCache.username = sharedPreferences.getString("username", "Default Name") ?: "Default Name"
        UserProfileCache.bio = sharedPreferences.getString("bio", "No bio available") ?: "No bio available"
        UserProfileCache.isDataLoaded = true
        UserProfileCache.lastUpdateTime = System.currentTimeMillis()
        displayCachedData()
    }

    private fun loadImageFromUri(uri: String, imageView: ImageView) {
        try {
            val file = File(uri)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
                if (imageView == bannerImageView) {
                    imageView.clearColorFilter()
                }
            }
        } catch (e: Exception) {
            // If there's an error loading the image, load the default
            if (imageView == bannerImageView) {
                imageView.setImageResource(R.drawable.banner3)
                imageView.setColorFilter(requireContext().getColor(R.color.green))
            } else {
                imageView.setImageResource(R.drawable.profile)
            }
        }
    }
}