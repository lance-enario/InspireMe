package com.application.inspireme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.application.inspireme.adapter.QuoteAdapter
import com.application.inspireme.adapter.FollowerAdapter
import com.application.inspireme.model.Quote
import com.application.inspireme.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import de.hdodenhof.circleimageview.CircleImageView
import android.app.AlertDialog
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

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
    private lateinit var followButton: MaterialButton

    private var loggedInUserId: String? = null
    private var viewingUserId: String? = null
    private var isOwnProfile: Boolean = false

    private val createdQuotes = mutableListOf<Quote>()
    private val likedQuotes = mutableListOf<Quote>()
    private lateinit var quoteAdapter: QuoteAdapter

    private val bannerImages = listOf(
        R.drawable.banner1 to "banner1",
        R.drawable.banner2 to "banner2",
        R.drawable.banner3 to "banner3",
        R.drawable.banner4 to "banner4",
        R.drawable.banner5 to "banner5",
        R.drawable.banner6 to "banner6"
    )

    private val profileImages = listOf(
        R.drawable.capybara to "capybara",
        R.drawable.cat_icon to "cat",
        R.drawable.cat_footprint to "cat_footprint",
        R.drawable.corgi to "corgi",
        R.drawable.dog to "dog",
        R.drawable.dog_paw to "dog_paw",
        R.drawable.doge to "doge",
        R.drawable.duck to "duck",
        R.drawable.gorilla to "gorilla"
    )

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(userId: String? = null): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            userId?.let { args.putString(ARG_USER_ID, it) }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val userAuthPrefs = requireContext().getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
        loggedInUserId = userAuthPrefs.getString("userId", null)

        viewingUserId = arguments?.getString(ARG_USER_ID) ?: loggedInUserId

        if (viewingUserId == null) {
            Toast.makeText(requireContext(), "User not identified.", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        isOwnProfile = viewingUserId == loggedInUserId && loggedInUserId != null

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
        followButton = view.findViewById(R.id.followButton)

        followersCountText.setOnClickListener {
            viewingUserId?.let { userId ->
                if (userId.isNotEmpty()) {
                    showFollowersDialog(userId)
                } else {
                    Toast.makeText(requireContext(), "User ID is not available.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val editProfileButton = view.findViewById<MaterialButton>(R.id.editProfileButton)
        val settingsButton = view.findViewById<ImageButton>(R.id.button_settings)

        if (isOwnProfile) {
            editProfileButton.visibility = View.VISIBLE
            settingsButton.visibility = View.VISIBLE
            followButton.visibility = View.GONE
            editProfileButton.setOnClickListener {
                val intent = Intent(requireContext(), ProfileSettingsActivity::class.java)
                intent.putExtra("previous_fragment", "ProfileFragment")
                startActivity(intent)
            }
            settingsButton.setOnClickListener {
                val intent = Intent(requireContext(), SettingsPageActivity::class.java)
                intent.putExtra("previous_fragment", "ProfileFragment")
                startActivity(intent)
            }
        } else {
            editProfileButton.visibility = View.GONE
            settingsButton.visibility = View.GONE
            if (loggedInUserId != null) {
                followButton.visibility = View.VISIBLE
                setupFollowButton(loggedInUserId!!, viewingUserId!!)
            } else {
                followButton.visibility = View.GONE
            }
        }

        quoteAdapter = QuoteAdapter(requireContext(), emptyList()) { quote ->
            val selectedTabIsMyQuotes = tabLayout.selectedTabPosition == 0
            val selectedTabIsLikedQuotes = tabLayout.selectedTabPosition == 1

            if (isOwnProfile) {
                if (selectedTabIsMyQuotes && quote.userId == viewingUserId) {
                    showDeleteQuoteDialog(quote)
                } else if (selectedTabIsLikedQuotes) {
                    showUnlikeQuoteDialog(quote)
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = quoteAdapter

        loadDataForProfile(viewingUserId!!)

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
    }

    private fun showFollowersDialog(userId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_followers, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView_followers_dialog)
        val noFollowersText: TextView = dialogView.findViewById(R.id.textView_no_followers_dialog)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Followers")
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .create()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val followersList = mutableListOf<User>()
        val followerAdapter = FollowerAdapter(requireContext(), followersList) { follower ->
            dialog.dismiss()
            viewingUserId = follower.id
            loadDataForProfile(follower.id)
            tabLayout.getTabAt(0)?.select()

            if (follower.id == loggedInUserId) {
                isOwnProfile = true
                followButton.visibility = View.GONE
                view?.findViewById<MaterialButton>(R.id.editProfileButton)?.visibility = View.VISIBLE
                view?.findViewById<ImageButton>(R.id.button_settings)?.visibility = View.VISIBLE
            } else {
                isOwnProfile = false
                if (loggedInUserId != null) {
                    followButton.visibility = View.VISIBLE
                    setupFollowButton(loggedInUserId!!, follower.id)
                }
                view?.findViewById<MaterialButton>(R.id.editProfileButton)?.visibility = View.GONE
                view?.findViewById<ImageButton>(R.id.button_settings)?.visibility = View.GONE
            }
        }
        recyclerView.adapter = followerAdapter

        FirebaseManager.getFollowerIds(userId,
            onSuccess = { followerIds ->
                if (followerIds.isEmpty()) {
                    noFollowersText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    return@getFollowerIds
                }

                noFollowersText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val totalFollowers = followerIds.size
                var loadedCount = 0

                followerIds.forEach { followerId ->
                    FirebaseManager.getUserData(followerId,
                        onSuccess = { user ->
                            followersList.add(user)
                            loadedCount++
                            if (loadedCount == totalFollowers) {
                                followerAdapter.updateFollowers(followersList.sortedBy { it.username.toLowerCase() })
                            }
                        },
                        onFailure = { error ->
                            loadedCount++
                            if (loadedCount == totalFollowers) {
                                followerAdapter.updateFollowers(followersList.sortedBy { it.username.toLowerCase() })
                            }
                        }
                    )
                }
            },
            onFailure = { error ->
                noFollowersText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Toast.makeText(context, "Failed to load followers: $error", Toast.LENGTH_SHORT).show()
            }
        )

        dialog.show()
    }

    private fun loadDataForProfile(userIdToLoad: String) {
        loadUserDataFromFirebase(userIdToLoad)
        loadUserQuotes(userIdToLoad)
        loadLikedQuotes(userIdToLoad)
        loadUserStats(userIdToLoad)
    }

    private fun loadUserDataFromFirebase(userIdToDisplay: String) {
        FirebaseManager.getUserData(userIdToDisplay,
            onSuccess = { user ->
                activity?.runOnUiThread {
                    usernameTextView.text = user.username
                    bioTextView.text = user.bio

                    val bannerResId = UserProfileCache.bannerImages[user.bannerId] ?: R.drawable.banner3
                    val profileResId = UserProfileCache.profileImages[user.profileId] ?: R.drawable.capybara

                    bannerImageView.setImageResource(bannerResId)
                    profilePic.setImageResource(profileResId)

                    if (isOwnProfile) {
                        UserProfileCache.username = user.username
                        UserProfileCache.bio = user.bio
                        UserProfileCache.bannerId = user.bannerId
                        UserProfileCache.profileId = user.profileId
                        UserProfileCache.isDataLoaded = true
                        UserProfileCache.lastUpdateTime = System.currentTimeMillis()
                    }
                }
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load user data: $error", Toast.LENGTH_SHORT).show()

                    bannerImageView.setImageResource(R.drawable.banner3)
                    profilePic.setImageResource(R.drawable.capybara)
                    usernameTextView.text = "User"
                    bioTextView.text = "Could not load bio."
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewingUserId?.let {
            loadDataForProfile(it)
            if (!isOwnProfile && loggedInUserId != null) {
                setupFollowButton(loggedInUserId!!, it)
            }
        }
    }

    private fun setupFollowButton(currentLoggedInUserId: String, targetProfileUserId: String) {
        FirebaseManager.checkIfFollowing(currentLoggedInUserId, targetProfileUserId) { isFollowing ->
            activity?.runOnUiThread {
                updateFollowButtonState(isFollowing)
                followButton.setOnClickListener {
                    val currentlyFollowing = followButton.text == "Unfollow"
                    if (currentlyFollowing) {
                        FirebaseManager.unfollowUser(currentLoggedInUserId, targetProfileUserId) { success ->
                            if (success) {
                                updateFollowButtonState(false)
                                loadUserStats(targetProfileUserId)
                            } else {
                                Toast.makeText(context, "Failed to unfollow", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        FirebaseManager.followUser(currentLoggedInUserId, targetProfileUserId) { success ->
                            if (success) {
                                updateFollowButtonState(true)
                                loadUserStats(targetProfileUserId)
                            } else {
                                Toast.makeText(context, "Failed to follow", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateFollowButtonState(isFollowing: Boolean) {
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
        val whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white)

        followButton.backgroundTintList = ColorStateList.valueOf(whiteColor)
        followButton.strokeColor = ColorStateList.valueOf(greenColor)

        val strokeWidthDp = 2
        followButton.strokeWidth = (strokeWidthDp * resources.displayMetrics.density).toInt()

        if (isFollowing) {
            followButton.text = "Unfollow"
            followButton.setTextColor(greenColor)
            followButton.backgroundTintList = ColorStateList.valueOf(whiteColor)
        } else {
            followButton.text = "Follow"
            followButton.setTextColor(whiteColor)
            followButton.backgroundTintList = ColorStateList.valueOf(greenColor)
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

    private fun displayCachedData() {
        usernameTextView.text = UserProfileCache.username
        bioTextView.text = UserProfileCache.bio

        bannerImages.firstOrNull { it.second == UserProfileCache.bannerId }?.let {
            bannerImageView.setImageResource(it.first)
        } ?: run {
            bannerImageView.setImageResource(R.drawable.banner3)
        }

        profileImages.firstOrNull { it.second == UserProfileCache.profileId }?.let {
            profilePic.setImageResource(it.first)
        } ?: run {
            profilePic.setImageResource(R.drawable.profile)
        }
    }

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

    private fun deleteQuote(quote: Quote) {
        loggedInUserId?.let { uid ->
            val loadingSnackbar = Snackbar.make(requireView(), "Deleting quote...", Snackbar.LENGTH_INDEFINITE)
            loadingSnackbar.show()

            FirebaseManager.deleteQuote(quote.id, uid) { success ->
                activity?.runOnUiThread {
                    loadingSnackbar.dismiss()

                    if (success) {
                        val position = createdQuotes.indexOfFirst { it.id == quote.id }
                        if (position != -1) {
                            createdQuotes.removeAt(position)
                            displayQuotes(createdQuotes)

                            val currentPosts = postsCountText.text.toString()
                            val postsCount = if (currentPosts.contains("K") || currentPosts.contains("M")) {
                                loadUserStats(uid)
                                -1
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

    private fun unlikeQuote(quote: Quote) {
        loggedInUserId?.let { uid ->
            val loadingSnackbar = Snackbar.make(requireView(), "Removing from liked quotes...", Snackbar.LENGTH_INDEFINITE)
            loadingSnackbar.show()

            FirebaseManager.unlikeQuote(uid, quote.id) { success ->
                activity?.runOnUiThread {
                    loadingSnackbar.dismiss()

                    if (success) {
                        val position = likedQuotes.indexOfFirst { it.id == quote.id }
                        if (position != -1) {
                            likedQuotes.removeAt(position)
                            displayQuotes(likedQuotes)

                            val currentLikes = likesCountText.text.toString()
                            val likesCount = if (currentLikes.contains("K") || currentLikes.contains("M")) {
                                loadUserStats(uid)
                                -1
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


