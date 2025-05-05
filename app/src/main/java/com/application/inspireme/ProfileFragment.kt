package com.application.inspireme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.data.UserProfileCache
import com.application.inspireme.adapters.QuoteAdapter
import com.application.inspireme.model.Quote
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import android.app.AlertDialog

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
    private lateinit var likesCountText: TextView
    private lateinit var postsCountText: TextView
    private lateinit var followersCountText: TextView
    private var userId: String? = null

    private val createdQuotes = mutableListOf<Quote>()
    private val likedQuotes = mutableListOf<Quote>()
    private lateinit var quoteAdapter: QuoteAdapter

    private val bannerImages = listOf(
        R.drawable.banner1 to "banner1",
        R.drawable.banner2 to "banner2",
        R.drawable.banner3 to "banner3"
    )

    private val profileImages = listOf(
        R.drawable.capybara to "capybara",
        R.drawable.cat to "cat",
        R.drawable.cat_footprint to "cat_footprint",
        R.drawable.corgi to "corgi",
        R.drawable.dog to "dog",
        R.drawable.dog_paw to "dog_paw",
        R.drawable.doge to "doge",
        R.drawable.duck to "duck",
        R.drawable.gorilla to "gorilla"
    )

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
        tabLayout = view.findViewById(R.id.profile_tabs)
        recyclerView = view.findViewById(R.id.activity_recycler_view)
        noActivityText = view.findViewById(R.id.no_activity_text)
        likesCountText = view.findViewById(R.id.likes_count)
        postsCountText = view.findViewById(R.id.posts_count)
        followersCountText = view.findViewById(R.id.followers_count)

        // Set up the adapter with quote click handling
        quoteAdapter = QuoteAdapter(requireContext(), emptyList()) { quote ->
            // Only allow deleting if it's the user's own quote and we're on the "My Quotes" tab
            if (tabLayout.selectedTabPosition == 0 && quote.userId == userId) {
                showDeleteQuoteDialog(quote)
            } else if (tabLayout.selectedTabPosition == 1) {
                // For liked quotes tab, show unlike option
                showUnlikeQuoteDialog(quote)
            }
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
            loadUserStats(userId!!)
        } else {
            updateStatsUI(0, 0, 0)
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

    private fun displayQuotes(quotes: List<Quote>) {
        if (quotes.isEmpty()) {
            noActivityText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noActivityText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            (recyclerView.adapter as QuoteAdapter).updateQuotes(quotes)
        }
    }

    private fun loadUserQuotes(userId: String) {
        FirebaseManager.getUserQuotes(userId) { quotes ->
            createdQuotes.clear()
            createdQuotes.addAll(quotes.sortedByDescending { it.timestamp })

            if (tabLayout.selectedTabPosition == 0) {
                displayQuotes(createdQuotes)
            }
        }
    }

    private fun loadLikedQuotes(userId: String) {
        FirebaseManager.getLikedQuotes(userId, { quotes ->
            likedQuotes.clear()
            likedQuotes.addAll(quotes)

            if (tabLayout.selectedTabPosition == 1) {
                displayQuotes(likedQuotes)
            }
        }, { error ->
            Toast.makeText(context, "Error loading liked quotes: $error", Toast.LENGTH_SHORT).show()
        })
    }

    private fun loadUserStats(userId: String) {
        updateStatsUI(0, 0, 0)

        var likesCount = 0
        var postsCount = 0
        var followersCount = 0
        var completedOperations = 0

        fun checkCompletion() {
            completedOperations++
            if (completedOperations == 3) {
                updateStatsUI(likesCount, postsCount, followersCount)
            }
        }

        FirebaseManager.getLikedQuotes(
            userId,
            onSuccess = { likedQuotes ->
                likesCount = likedQuotes.size
                checkCompletion()
            },
            onFailure = { error ->
                checkCompletion()
            }
        )

        FirebaseManager.getUserQuotes(userId) { userQuotes ->
            postsCount = userQuotes.size
            checkCompletion()
        }

        FirebaseManager.getFollowerCount(userId) { count ->
            followersCount = count
            checkCompletion()
        }
    }

    private fun updateStatsUI(likesCount: Int, postsCount: Int, followersCount: Int) {
        activity?.runOnUiThread {
            likesCountText.text = formatCount(likesCount)
            postsCountText.text = formatCount(postsCount)
            followersCountText.text = formatCount(followersCount)
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count < 1000 -> count.toString()
            count < 1000000 -> String.format("%.1fK", count / 1000.0).replace(".0K", "K")
            else -> String.format("%.1fM", count / 1000000.0).replace(".0M", "M")
        }
    }

    fun refreshStats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadUserStats(userId)
        }
    }

    fun loadUserProfile(userId: String) {
        loadUserStats(userId)
    }

    private fun displayCachedData() {
        usernameTextView.text = UserProfileCache.username
        bioTextView.text = UserProfileCache.bio

        // Set banner image based on stored ID
        bannerImages.firstOrNull { it.second == UserProfileCache.bannerId }?.let {
            bannerImageView.setImageResource(it.first)
        } ?: run {
            bannerImageView.setImageResource(R.drawable.banner3)
        }

        // Set profile image based on stored ID
        profileImages.firstOrNull { it.second == UserProfileCache.profileId }?.let {
            profilePic.setImageResource(it.first)
        } ?: run {
            profilePic.setImageResource(R.drawable.profile)
        }
    }

    override fun onResume() {
        super.onResume()
        UserProfileCache.isDataLoaded = false // Force refresh
        val userAuthPrefs = requireContext().getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
        val userId = userAuthPrefs.getString("userId", null)

        if (userId != null) {
            loadUserDataFromFirebase(userId)
        } else {
            loadSavedData()
        }

        // Refresh stats and quotes
        userId?.let {
            loadUserQuotes(it)
            loadLikedQuotes(it)
            loadUserStats(it)
        }
    }

    private fun loadUserDataFromFirebase(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    UserProfileCache.username = snapshot.child("username").getValue(String::class.java) ?: "Default Name"
                    UserProfileCache.bio = snapshot.child("bio").getValue(String::class.java) ?: "No bio available"
                    UserProfileCache.bannerId = snapshot.child("bannerId").getValue(String::class.java) ?: "banner3"
                    UserProfileCache.profileId = snapshot.child("profileId").getValue(String::class.java) ?: "profile1"

                    UserProfileCache.isDataLoaded = true
                    UserProfileCache.lastUpdateTime = System.currentTimeMillis()

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
        UserProfileCache.bannerId = sharedPreferences.getString("bannerId", "banner3") ?: "banner3"
        UserProfileCache.profileId = sharedPreferences.getString("profileId", "profile1") ?: "profile1"
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
            if (imageView == bannerImageView) {
                imageView.setImageResource(R.drawable.banner3)
                imageView.setColorFilter(requireContext().getColor(R.color.green))
            } else {
                imageView.setImageResource(R.drawable.profile)
            }
        }
    }

    /**
     * Shows a confirmation dialog for deleting a quote
     */
    private fun showDeleteQuoteDialog(quote: Quote) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Quote")
            .setMessage("Are you sure you want to delete this quote?\n\n\"${quote.quote}\"\n\n- ${quote.author}")
            .setPositiveButton("Delete") { _, _ ->
                deleteQuote(quote)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows a confirmation dialog for unliking a quote
     */
    private fun showUnlikeQuoteDialog(quote: Quote) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unlike Quote")
            .setMessage("Remove this quote from your liked quotes?\n\n\"${quote.quote}\"\n\n- ${quote.author}")
            .setPositiveButton("Unlike") { _, _ ->
                unlikeQuote(quote)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes a quote from Firebase and updates the UI
     */
    private fun deleteQuote(quote: Quote) {
        userId?.let { uid ->
            // Show loading indicator
            val loadingSnackbar = Snackbar.make(requireView(), "Deleting quote...", Snackbar.LENGTH_INDEFINITE)
            loadingSnackbar.show()

            // Delete from main quotes collection and user's quotes
            FirebaseManager.deleteQuote(quote.id, uid) { success ->
                activity?.runOnUiThread {
                    loadingSnackbar.dismiss()
                    
                    if (success) {
                        // Remove from local list and update UI
                        val position = createdQuotes.indexOfFirst { it.id == quote.id }
                        if (position != -1) {
                            createdQuotes.removeAt(position)
                            displayQuotes(createdQuotes)
                            
                            // Update stats
                            val currentPosts = postsCountText.text.toString()
                            val postsCount = if (currentPosts.contains("K") || currentPosts.contains("M")) {
                                // If using K/M notation, reload stats from server
                                loadUserStats(uid)
                                -1 // dummy value
                            } else {
                                currentPosts.toInt() - 1
                            }
                            
                            if (postsCount >= 0) {
                                postsCountText.text = formatCount(postsCount)
                            }
                            
                            Snackbar.make(requireView(), "Quote deleted successfully", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(requireView(), "Failed to delete quote", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Unlikes a quote and updates the UI
     */
    private fun unlikeQuote(quote: Quote) {
        userId?.let { uid ->
            // Show loading indicator
            val loadingSnackbar = Snackbar.make(requireView(), "Removing from liked quotes...", Snackbar.LENGTH_INDEFINITE)
            loadingSnackbar.show()
            
            FirebaseManager.unlikeQuote(uid, quote.id) { success ->
                activity?.runOnUiThread {
                    loadingSnackbar.dismiss()
                    
                    if (success) {
                        // Remove from local list and update UI
                        val position = likedQuotes.indexOfFirst { it.id == quote.id }
                        if (position != -1) {
                            likedQuotes.removeAt(position)
                            displayQuotes(likedQuotes)
                            
                            // Update likes count
                            val currentLikes = likesCountText.text.toString()
                            val likesCount = if (currentLikes.contains("K") || currentLikes.contains("M")) {
                                // If using K/M notation, reload stats from server
                                loadUserStats(uid)
                                -1 // dummy value
                            } else {
                                currentLikes.toInt() - 1
                            }
                            
                            if (likesCount >= 0) {
                                likesCountText.text = formatCount(likesCount)
                            }
                            
                            Snackbar.make(requireView(), "Quote removed from likes", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(requireView(), "Failed to unlike quote", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}


