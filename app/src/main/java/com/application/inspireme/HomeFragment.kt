package com.application.inspireme

import android.content.Intent
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.application.inspireme.R
import com.application.inspireme.SettingsPageActivity
import com.application.inspireme.adapter.QuoteFeedAdapter
import com.application.inspireme.adapter.SuggestedUsersAdapter
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.listeners.OnUserProfileClickListener
import com.application.inspireme.model.Quote
import com.application.inspireme.model.User
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.atomic.AtomicInteger

class HomeFragment : Fragment(R.layout.fragment_home), OnUserProfileClickListener {

    private lateinit var quotesRecyclerView: RecyclerView
    private lateinit var suggestedUsersRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var quoteFeedAdapter: QuoteFeedAdapter
    private lateinit var suggestedUsersAdapter: SuggestedUsersAdapter

    private val quotes = mutableListOf<Quote>()
    private val suggestedUsers = mutableListOf<User>()
    private var currentUserId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        view.findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsPageActivity::class.java)
            intent.putExtra("previous_fragment", "HomeFragment")
            startActivity(intent)
        }

        // Setup search functionality
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        setupSearchView(searchView)

        quotesRecyclerView = view.findViewById(R.id.quotes_recycler_view)
        suggestedUsersRecyclerView = view.findViewById(R.id.suggested_users_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        // Set up quotes feed
        quotesRecyclerView.layoutManager = LinearLayoutManager(context)
        quoteFeedAdapter = QuoteFeedAdapter(requireContext(), quotes, this::handleQuoteLike, this)
        quotesRecyclerView.adapter = quoteFeedAdapter

        // Set up suggested users
        suggestedUsersRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        suggestedUsersAdapter = SuggestedUsersAdapter(suggestedUsers, this::handleFollowUser, this)
        suggestedUsersRecyclerView.adapter = suggestedUsersAdapter

        // Set up filter buttons
        setupFilterButtons(view)

        swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }

        currentFilter = QuoteFilter.FOR_YOU
        refreshContent()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private enum class QuoteFilter {
        FOR_YOU, RECENT, TRENDING, POPULAR
    }

    private var currentFilter = QuoteFilter.FOR_YOU
    private var filterButtons = mutableListOf<Button>()

    private fun setupFilterButtons(view: View) {
        // Find all filter buttons in the horizontal scroll view
        val buttonForYou = view.findViewById<Button>(R.id.btn_for_you)
        val buttonRecent = view.findViewById<Button>(R.id.btn_recent)
        val buttonTrending = view.findViewById<Button>(R.id.btn_trending)
        val buttonPopular = view.findViewById<Button>(R.id.btn_popular)

        filterButtons = mutableListOf(buttonForYou, buttonRecent, buttonTrending, buttonPopular)

        // Set up click listeners
        buttonForYou.setOnClickListener {
            selectFilter(QuoteFilter.FOR_YOU)
        }

        buttonRecent.setOnClickListener {
            selectFilter(QuoteFilter.RECENT)
        }

        buttonTrending.setOnClickListener {
            selectFilter(QuoteFilter.TRENDING)
        }

        buttonPopular.setOnClickListener {
            selectFilter(QuoteFilter.POPULAR)
        }

        // Highlight the default selected button
        updateButtonStyles()
    }

    private fun selectFilter(filter: QuoteFilter) {
        if (filter != currentFilter) {
            currentFilter = filter
            updateButtonStyles()
            loadFilteredQuotes()
        }
    }

    private fun updateButtonStyles() {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.green)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.light_gray)

        filterButtons.forEachIndexed { index, button ->
            val isSelected = when(index) {
                0 -> currentFilter == QuoteFilter.FOR_YOU
                1 -> currentFilter == QuoteFilter.RECENT
                2 -> currentFilter == QuoteFilter.TRENDING
                3 -> currentFilter == QuoteFilter.POPULAR
                else -> false
            }

            button.backgroundTintList = ColorStateList.valueOf(
                if (isSelected) selectedColor else unselectedColor
            )
        }
    }

    private fun loadFilteredQuotes() {
        when (currentFilter) {
            QuoteFilter.FOR_YOU -> loadQuotes()
            QuoteFilter.RECENT -> loadRecentQuotes()
            QuoteFilter.TRENDING -> loadTrendingQuotes()
            QuoteFilter.POPULAR -> loadPopularQuotes()
        }
    }

    private fun refreshContent() {
        loadFilteredQuotes()
        loadSuggestedUsers()
    }

    private fun loadQuotes() {
        if (currentUserId == null) {
            loadRandomQuotes()
            return
        }

        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        FirebaseManager.getFollowing(
            currentUserId!!,
            onSuccess = { followingIds ->
                if (followingIds.isEmpty()) {
                    // If not following anyone, load random quotes
                    loadRandomQuotes()
                } else {
                    // Load both followed users' quotes and some random quotes
                    val followedQuotes = mutableListOf<Quote>()
                    val pendingUsers = AtomicInteger(followingIds.size)

                    // Fetch quotes from each followed user
                    followingIds.forEach { userId ->
                        FirebaseManager.getUserQuotes(userId) { userQuotes ->
                            followedQuotes.addAll(userQuotes)

                            if (pendingUsers.decrementAndGet() == 0) {
                                FirebaseManager.getRandomQuoteBatch(
                                    onSuccess = { randomQuotes ->
                                        val mixedQuotes = createMixedFeed(followedQuotes, randomQuotes.take(10))
                                        updateQuotesFeed(mixedQuotes)
                                    },
                                    onFailure = { error ->
                                        // If random quotes fail, just use followed ones
                                        updateQuotesFeed(followedQuotes.sortedByDescending { it.timestamp })
                                        activity?.runOnUiThread {
                                            Toast.makeText(context, "Could not load discovery quotes", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            onFailure = { error ->
                // fall back to random quotes
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to load following data: $error", Toast.LENGTH_SHORT).show()
                    loadRandomQuotes()
                }
            }
        )
    }

    private fun loadRecentQuotes() {
        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        FirebaseManager.getAllQuotes(
            onSuccess = { allQuotes ->
                // Sort by timestamp (newest first)
                val recentQuotes = allQuotes
                    .sortedByDescending { it.timestamp }
                    .take(20)
                updateQuotesFeed(recentQuotes)
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error loading recent quotes: $error", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        )
    }

    private fun loadTrendingQuotes() {
        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        // Trending based on quotes that have been liked recently
        FirebaseManager.getAllQuotes(
            onSuccess = { allQuotes ->
                // Get quotes from last 7 days with tags that are trending
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                val trendingQuotes = allQuotes
                    .filter { it.timestamp > sevenDaysAgo }
                    .sortedByDescending { 
                        // Priority to quotes with popular tags
                        if (it.tags.any { tag -> tag in trendingTags }) 2 else 1
                    }
                    .take(20)
                updateQuotesFeed(trendingQuotes)
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error loading trending quotes: $error", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        )
    }

    private fun loadPopularQuotes() {
        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        FirebaseManager.getRandomQuoteBatch(
            onSuccess = { fetchedQuotes ->
                val popularQuotes = fetchedQuotes
                    .shuffled()
                    .take(20)
                updateQuotesFeed(popularQuotes)
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error loading popular quotes: $error", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        )
    }

    // A sample of trending tags - in a real app, this would be dynamically determined
    private val trendingTags = listOf(
        "motivation", "success", "mindfulness", "happiness", "inspiration"
    )

    /**
    * Creates a mixed feed of followed users' quotes and random discovery quotes
    */
    private fun createMixedFeed(followedQuotes: List<Quote>, randomQuotes: List<Quote>): List<Quote> {
        val result = mutableListOf<Quote>()

        // Filter out any duplicates between followed and random quotes
        val followedIds = followedQuotes.map { it.id }.toSet()
        val uniqueRandomQuotes = randomQuotes.filter { !followedIds.contains(it.id) }

        // Prioritize recent quotes from followed users
        val sortedFollowed = followedQuotes.sortedByDescending { it.timestamp }

        // use top 70% followed and 30% random
        if (sortedFollowed.size > 10) {
            val followedCount = (sortedFollowed.size * 0.7).toInt().coerceAtLeast(1)
            result.addAll(sortedFollowed.take(followedCount))

            // Add random quotes, labeled as "discovery"
            result.addAll(uniqueRandomQuotes.shuffled().take(5).map { 
                it.copy(isDiscovery = true)  // Add a flag to mark as discovery content
            })

            // Add remaining followed quotes
            result.addAll(sortedFollowed.drop(followedCount))
        } else {
            // If few followed quotes, interleave them with random quotes
            val interleavedQuotes = mutableListOf<Quote>()
            val maxSize = maxOf(sortedFollowed.size, uniqueRandomQuotes.size)

            for (i in 0 until maxSize) {
                if (i < sortedFollowed.size) {
                    interleavedQuotes.add(sortedFollowed[i])
                }

                if (i < uniqueRandomQuotes.size) {
                    interleavedQuotes.add(uniqueRandomQuotes[i].copy(isDiscovery = true))
                }
            }

            result.addAll(interleavedQuotes)
        }

        return result
    }   

    private fun loadRandomQuotes() {
        FirebaseManager.getRandomQuoteBatch(
            onSuccess = { fetchedQuotes ->
                updateQuotesFeed(fetchedQuotes.shuffled().take(20))
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error loading quotes: $error", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        )
    }

    private fun updateQuotesFeed(newQuotes: List<Quote>) {
        activity?.runOnUiThread {
            quotes.clear()
            quotes.addAll(newQuotes)
            quoteFeedAdapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false

            // Check and update like status for each quote
            if (currentUserId != null) {
                for (quote in quotes) {
                    FirebaseManager.checkIfQuoteLiked(currentUserId!!, quote.id) { isLiked ->
                        activity?.runOnUiThread {
                            quoteFeedAdapter.updateLikeStatus(quote.id, isLiked)
                        }
                    }
                }
            }
        }
    }

    private fun loadSuggestedUsers() {
        if (currentUserId == null) {
            suggestedUsers.clear()
            suggestedUsersAdapter.notifyDataSetChanged()
            return
        }

        FirebaseManager.getFollowing(
            currentUserId!!,
            onSuccess = { followingIds ->
                val alreadyFollowing = followingIds.toSet()

                FirebaseManager.getAllUsers { allUsers ->
                    val filteredUsers = allUsers
                        .filter { it.id != currentUserId && !alreadyFollowing.contains(it.id) }
                        .take(10) // Limit to 10 suggestions

                    activity?.runOnUiThread {
                        suggestedUsers.clear()
                        suggestedUsers.addAll(filteredUsers)
                        suggestedUsersAdapter.notifyDataSetChanged()
                    }
                }
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to load following data: $error", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        )
    }

    private fun handleQuoteLike(quote: Quote, isLiked: Boolean) {
        val userId = currentUserId ?: return

        if (isLiked) {
            FirebaseManager.likeQuote(userId, quote) { success ->
                if (!success) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to like quote", Toast.LENGTH_SHORT).show()
                        quoteFeedAdapter.updateLikeStatus(quote.id, false)
                    }
                }
            }
        } else {
            FirebaseManager.unlikeQuote(userId, quote.id) { success ->
                if (!success) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to unlike quote", Toast.LENGTH_SHORT).show()
                        quoteFeedAdapter.updateLikeStatus(quote.id, true)
                    }
                }
            }
        }
    }

    private fun handleFollowUser(user: User, isFollowing: Boolean) {
        val userId = currentUserId ?: return

        if (isFollowing) {
            FirebaseManager.followUser(userId, user.id) { success ->
                if (success) {
                    activity?.runOnUiThread {
                        // Update the adapter
                        suggestedUsers.remove(user)
                        suggestedUsersAdapter.notifyDataSetChanged()
                        // Refresh feed to include new followed user's quotes
                        loadQuotes()
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to follow user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupSearchView(searchView: SearchView) {
        // Set hint text
        searchView.queryHint = "Search quotes..."

        // Set search view listeners
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Only search if there are at least 3 characters
                if (newText.length >= 3) {
                    performSearch(newText)
                } else if (newText.isEmpty()) {
                    // If search text is cleared, reload current filter
                    loadFilteredQuotes()
                }
                return true
            }
        })

        // When search view is closed, reload the current filter
        searchView.setOnCloseListener {
            loadFilteredQuotes()
            false
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            loadFilteredQuotes()
            return
        }

        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        // Normalize search terms
        val searchTerms = query.lowercase().trim().split(" ")

        // Get all quotes and filter locally
        FirebaseManager.getAllQuotes(
            onSuccess = { allQuotes ->
                val filteredQuotes = allQuotes.filter { quote ->
                    // Check if any search term is contained in quote text
                    val quoteText = quote.quote.lowercase()
                    val authorName = quote.author.lowercase()
                    val tagsList = quote.tags.joinToString(" ").lowercase()

                    searchTerms.any { term ->
                        quoteText.contains(term) || 
                        authorName.contains(term) || 
                        tagsList.contains(term)
                    }
                }

                updateQuotesFeed(filteredQuotes)

                // Show message if no results found
                activity?.runOnUiThread {
                    if (filteredQuotes.isEmpty()) {
                        Toast.makeText(context, "No quotes found matching \"$query\"", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error searching quotes: $error", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        )
    }

    // Implementation of OnUserProfileClickListener
    override fun onUserProfileClicked(userId: String) {
        val profileFragment = ProfileFragment.newInstance(userId)
        // Use NavigationBarActivity's method to load fragment
        (activity as? NavigationBarActivity)?.loadFragment(profileFragment, true)
    }
}