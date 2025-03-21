package com.application.inspireme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var bannerImageView: ImageView
    private lateinit var profilePic: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        bannerImageView = view.findViewById(R.id.BannerPic)
        profilePic = view.findViewById(R.id.ProfilePic)
        usernameTextView = view.findViewById(R.id.usernameee)
        bioTextView = view.findViewById(R.id.biotextView)

        loadSavedData()

        view.findViewById<ImageButton>(R.id.profileSettings).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSettingsActivity::class.java))
        }

        view.findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsPageActivity::class.java)
            intent.putExtra("previous_screen", "ProfileFragment")
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
