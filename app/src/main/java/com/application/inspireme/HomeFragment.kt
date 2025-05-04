package com.application.inspireme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.application.inspireme.R
import com.application.inspireme.SettingsPageActivity
import com.application.inspireme.adapter.QuoteFeedAdapter
import com.application.inspireme.adapter.SuggestedUsersAdapter
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.model.Quote
import com.application.inspireme.model.User
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.atomic.AtomicInteger

class HomeFragment : Fragment(R.layout.fragment_home) {
    
    private lateinit var quotesRecyclerView: RecyclerView
    private lateinit var suggestedUsersRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var quoteFeedAdapter: QuoteFeedAdapter
    private lateinit var suggestedUsersAdapter: SuggestedUsersAdapter
    
    private val quotes = mutableListOf<Quote>()
    private val suggestedUsers = mutableListOf<User>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsPageActivity::class.java)
            intent.putExtra("previous_fragment", "HomeFragment")
            startActivity(intent)
        }

        quotesRecyclerView = view.findViewById(R.id.quotes_recycler_view)
        suggestedUsersRecyclerView = view.findViewById(R.id.suggested_users_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        
        // Set up quotes feed
        quotesRecyclerView.layoutManager = LinearLayoutManager(context)
        quoteFeedAdapter = QuoteFeedAdapter(requireContext(), quotes, 
            onLikeClicked = { quote, isLiked -> handleQuoteLike(quote, isLiked) }
        )
        quotesRecyclerView.adapter = quoteFeedAdapter
        
        // Set up suggested users
        suggestedUsersRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        suggestedUsersAdapter = SuggestedUsersAdapter(suggestedUsers, 
            onFollowClicked = { user, isFollowing -> handleFollowUser(user, isFollowing) }
        )
        suggestedUsersRecyclerView.adapter = suggestedUsersAdapter
        
        swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }
        
        refreshContent()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    private fun refreshContent() {
        loadQuotes()
        loadSuggestedUsers()
    }
    
    private fun loadQuotes() {
        if (currentUserId == null) {
            // If not logged in, just show random quotes
            loadRandomQuotes()
            return
        }

        // First show loading state
        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }

        // We'll create a mix of followed users' quotes and random quotes
        FirebaseManager.getFollowing(
            currentUserId,
            onSuccess = { followingIds ->
                if (followingIds.isEmpty()) {
                    // If not following anyone, just load random quotes
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
                                // Now that we have followed quotes, get some random ones too
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
                // Handle error - fall back to random quotes
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to load following data: $error", Toast.LENGTH_SHORT).show()
                    loadRandomQuotes()
                }
            }
        )
    }

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
        
        // If we have many followed quotes, use top 70% followed and 30% random
        if (sortedFollowed.size > 10) {
            val followedCount = (sortedFollowed.size * 0.7).toInt().coerceAtLeast(1)
            result.addAll(sortedFollowed.take(followedCount))
            
            // Add some random quotes, labeled as "discovery"
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
                    FirebaseManager.checkIfQuoteLiked(currentUserId, quote.id) { isLiked ->
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
            currentUserId,
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
}