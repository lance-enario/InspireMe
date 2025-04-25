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

        // Load data from Firebase if the user is logged in
        if (userId != null) {
            loadUserDataFromFirebase(userId)
        } else {
            // Fallback to SharedPreferences if not logged in
            loadSavedData()
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

    private fun loadUserDataFromFirebase(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)
        
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: "Default Name"
                    val bio = snapshot.child("bio").getValue(String::class.java) ?: "No bio available"
                    
                    usernameTextView.text = username
                    bioTextView.text = bio
                    
                    // For images, we still use local storage as Firebase Storage would be needed for images
                    // Load custom image paths
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
                } else {
                    // User not found, fall back to local data
                    loadSavedData()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
                // Fall back to local data on error
                loadSavedData()
            }
        })
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