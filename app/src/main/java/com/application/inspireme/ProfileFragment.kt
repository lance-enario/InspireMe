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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.data.UserProfileCache
import com.application.inspireme.adapters.QuoteAdapter
import com.application.inspireme.model.Quote
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
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
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var noActivityText: TextView
    private var userId: String? = null
    
    // Add these lists to store the quotes
    private val createdQuotes = mutableListOf<Quote>()
    private val likedQuotes = mutableListOf<Quote>()
    private lateinit var quoteAdapter: QuoteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val userAuthPrefs = requireContext().getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
        userId = userAuthPrefs.getString("userId", null)
        
        // Initialize views
        bannerImageView = view.findViewById(R.id.banner_image)
        profilePic = view.findViewById(R.id.profile_picture)
        usernameTextView = view.findViewById(R.id.username_text)
        bioTextView = view.findViewById(R.id.bio_text)
        
        // Initialize new views for quotes display
        tabLayout = view.findViewById(R.id.profile_tabs)
        recyclerView = view.findViewById(R.id.activity_recycler_view)
        noActivityText = view.findViewById(R.id.no_activity_text)
        
        // Set up the adapter
        quoteAdapter = QuoteAdapter(requireContext(), emptyList()) { quote ->
            // Handle quote click if needed
        }
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = quoteAdapter
        
        // Check if we already have cached data
        if (UserProfileCache.isDataLoaded) {
            displayCachedData()
        } else {
            if (userId != null) {
                loadUserDataFromFirebase(userId!!)
            } else {
                loadSavedData()
            }
        }
        
        // Load quotes
        if (userId != null) {
            loadUserQuotes(userId!!)
            loadLikedQuotes(userId!!)
        }
        
        // Set up the tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> displayQuotes(createdQuotes)
                    1 -> displayQuotes(likedQuotes)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        
        // Set up button listeners
        view.findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsPageActivity::class.java)
            intent.putExtra("previous_fragment", "ProfileFragment")
            startActivity(intent)
        }
        
        view.findViewById<MaterialButton>(R.id.editProfileButton).setOnClickListener {
            val intent = Intent(requireContext(), ProfileSettingsActivity::class.java)
            intent.putExtra("previous_fragment", "ProfileFragment")
            startActivity(intent)
        }
    }
    
    // Method to display quotes in the RecyclerView
    private fun displayQuotes(quotes: List<Quote>) {
        if (quotes.isEmpty()) {
            noActivityText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noActivityText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            
            // Update the adapter with the new data
            (recyclerView.adapter as QuoteAdapter).updateQuotes(quotes)
        }
    }

    // Load user's created quotes
    private fun loadUserQuotes(userId: String) {
        FirebaseManager.getUserQuotes(userId) { quotes ->
            createdQuotes.clear()
            createdQuotes.addAll(quotes.sortedByDescending { it.timestamp })

            // If the "My Quotes" tab is selected, update the display
            if (tabLayout.selectedTabPosition == 0) {
                displayQuotes(createdQuotes)
            }
        }
    }

    // Load user's liked quotes
    private fun loadLikedQuotes(userId: String) {
        FirebaseManager.getLikedQuotes(userId, { quotes ->
            likedQuotes.clear()
            likedQuotes.addAll(quotes)
            
            // If the "Liked Quotes" tab is selected, update the display
            if (tabLayout.selectedTabPosition == 1) {
                displayQuotes(likedQuotes)
            }
        }, { error ->
            Toast.makeText(context, "Error loading liked quotes: $error", Toast.LENGTH_SHORT).show()
        })
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

//    private fun loadUserQuotes(userId: String) {
//        val noActivityText = view?.findViewById<TextView>(R.id.no_activity_text)
//        val recyclerView = view?.findViewById<RecyclerView>(R.id.activity_recycler_view)
//
//        FirebaseManager.getUserQuotes(userId) { quotes ->
//            if (quotes.isEmpty()) {
//                noActivityText?.visibility = View.VISIBLE
//                recyclerView?.visibility = View.GONE
//            } else {
//                noActivityText?.visibility = View.GONE
//                recyclerView?.visibility = View.VISIBLE
//
//                val sortedQuotes = quotes.sortedByDescending { it.timestamp }
//
//                recyclerView?.layoutManager = LinearLayoutManager(context)
//            }
//        }
//    }

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


