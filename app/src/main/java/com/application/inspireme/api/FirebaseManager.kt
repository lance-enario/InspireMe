package com.application.inspireme.api

import android.util.Log
import com.application.inspireme.model.Quote
import com.application.inspireme.model.QuoteResponse
import com.application.inspireme.model.User
import com.application.inspireme.model.LikedQuote
import com.application.inspireme.model.UserNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val quotesRef = database.getReference("quotes")
    private val likedQuotesRef = database.getReference("likedQuotes")
    private val followersRef = database.getReference("followers")
    private val followingRef = database.getReference("following")
    private val notificationsRef = database.getReference("notifications")

    fun saveUser(userId: String, user: User, onComplete: (Boolean) -> Unit) {
        usersRef.child(userId).setValue(user)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    
    fun updateUserProfile(userId: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        usersRef.child(userId).updateChildren(updates)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    
    fun getUserData(userId: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure("Failed to parse user data")
                    }
                } else {
                    onFailure("User not found")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    // Quote Operations ------------------------
    fun saveQuote(quote: Quote, onComplete: (Boolean, String?) -> Unit) {
        val quoteId = quotesRef.push().key ?: return onComplete(false, "Failed to generate ID")
        val updatedQuote = quote.copy(id = quoteId)
        
        quotesRef.child(quoteId).setValue(updatedQuote)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) quoteId else null)
            }
    }

    fun saveApiQuote(quoteResponse: QuoteResponse, onComplete: (Boolean, String?) -> Unit) {
        val quote = Quote(
            id = "api_${quoteResponse.id}",
            quote = quoteResponse.quote,
            author = quoteResponse.author,
            tags = quoteResponse.tags
        )
        
        quotesRef.child(quote.id).setValue(quote)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) quote.id else null)
            }
    }

    fun getAllQuotes(onSuccess: (List<Quote>) -> Unit, onFailure: (String) -> Unit) {
        quotesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quotesList = mutableListOf<Quote>()
                for (quoteSnapshot in snapshot.children) {
                    val quote = quoteSnapshot.getValue(Quote::class.java)
                    if (quote != null) {
                        quotesList.add(quote)
                    }
                }
                onSuccess(quotesList)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun getRandomQuoteBatch(onSuccess: (List<Quote>) -> Unit, onFailure: (String) -> Unit) {
        quotesRef.limitToFirst(500).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val quotes = task.result.children.mapNotNull {
                    it.getValue(Quote::class.java)
                }
                onSuccess(quotes)
            } else {
                onFailure(task.exception?.message ?: "Unknown error")
            }
        }
    }
    
    fun getQuoteById(quoteId: String, onSuccess: (Quote) -> Unit, onFailure: (String) -> Unit) {
        quotesRef.child(quoteId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val quote = snapshot.getValue(Quote::class.java)
                    if (quote != null) {
                        onSuccess(quote)
                    } else {
                        onFailure("Failed to parse quote data")
                    }
                } else {
                    onFailure("Quote not found")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun getUserQuotes(userId: String, onSuccess: (List<Quote>) -> Unit) {
        val userQuotesRef = database.getReference("userQuotes/$userId")
        
        userQuotesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quotesList = mutableListOf<Quote>()
                for (quoteSnapshot in snapshot.children) {
                    val quote = quoteSnapshot.getValue(Quote::class.java)
                    if (quote != null) {
                        quotesList.add(quote)
                    }
                }
                onSuccess(quotesList)
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
                onSuccess(emptyList())
            }
        })
    }

    // User-specific quotes
    fun saveUserQuote(userId: String, quote: Quote, onComplete: (Boolean, String?) -> Unit) {
        val userQuotesRef = database.getReference("userQuotes/$userId")
        val quoteId = userQuotesRef.push().key ?: return onComplete(false, "Failed to generate ID")
        
        // Set the ID in the quote object
        val updatedQuote = quote.copy(id = quoteId)
        
        userQuotesRef.child(quoteId).setValue(updatedQuote)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) quoteId else null)
            }
    }

    fun likeQuote(userId: String, quote: Quote, onComplete: (Boolean) -> Unit) {
    val likedQuotesUserRef = likedQuotesRef.child(userId)
    val quoteRef = likedQuotesUserRef.child(quote.id)
    
    val likedQuote = LikedQuote(
        quote = quote.quote,
        author = quote.author,
        timestamp = System.currentTimeMillis()
    )
    
    quoteRef.setValue(likedQuote)
        .addOnCompleteListener { task ->
            if (!quote.id.startsWith("api_") && quote.author.isNotEmpty()) {
                if (!task.isSuccessful) {
                    Log.e("FirebaseManager", "Error liking quote: ${task.exception?.message}")
                }
                val authorId = quote.userId
                if (authorId.isNotEmpty() && authorId != userId) {
                    // Get username first, then send notification
                    getUserData(userId, { user ->
                        sendNotification(
                            authorId,
                            userId,
                            "Someone liked your quote",
                            "${user.username} liked your quote: \"${quote.quote.take(50)}${if (quote.quote.length > 50) "..." else ""}\"",
                            "LIKE_QUOTE",
                            quote.id
                        )
                    }, { error ->
                        // If username lookup fails, still send notification with generic name
                        sendNotification(
                            authorId,
                            userId,
                            "Someone liked your quote",
                            "Someone liked your quote: \"${quote.quote.take(50)}${if (quote.quote.length > 50) "..." else ""}\"",
                            "LIKE_QUOTE",
                            quote.id
                        )
                    })
                }
            }
            onComplete(task.isSuccessful)
        }
}

    fun unlikeQuote(userId: String, quoteId: String, onComplete: (Boolean) -> Unit) {
        val quoteRef = likedQuotesRef.child(userId).child(quoteId)
        quoteRef.removeValue()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun checkIfQuoteLiked(userId: String, quoteId: String, onResult: (Boolean) -> Unit) {
        likedQuotesRef.child(userId).child(quoteId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.exists())
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onResult(false)
                }
            })
    }

    fun getLikedQuotes(userId: String, onSuccess: (List<Quote>) -> Unit, onFailure: (String) -> Unit) {
        likedQuotesRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val quotesList = mutableListOf<Quote>()
                    
                    if (!snapshot.exists()) {
                        onSuccess(quotesList)
                        return
                    }
                    
                    var pendingQuotes = snapshot.childrenCount.toInt()
                    
                    if (pendingQuotes == 0) {
                        onSuccess(quotesList)
                        return
                    }
                    
                    for (likedQuoteSnapshot in snapshot.children) {
                        val quoteId = likedQuoteSnapshot.key ?: continue
                        val timestamp = likedQuoteSnapshot.child("timestamp").getValue(Long::class.java) ?: 0
                        
                        getQuoteById(quoteId, { quote ->
                            // Add the quote with the liked timestamp
                            quotesList.add(quote.copy(timestamp = timestamp))
                            
                            pendingQuotes--
                            if (pendingQuotes == 0) {
                                // Sort by timestamp (newest first)
                                onSuccess(quotesList.sortedByDescending { it.timestamp })
                            }
                        }, { error ->
                            pendingQuotes--
                            if (pendingQuotes == 0) {
                                onSuccess(quotesList.sortedByDescending { it.timestamp })
                            }
                        })
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    fun followUser(followerId: String, targetUserId: String, onComplete: (Boolean) -> Unit) {
    // Add to current user's following list
    followingRef.child(followerId).child(targetUserId).setValue(System.currentTimeMillis())
        .addOnCompleteListener { followingTask ->
            if (followingTask.isSuccessful) {
                followersRef.child(targetUserId).child(followerId).setValue(System.currentTimeMillis())
                    .addOnCompleteListener { followersTask ->
                        if (followersTask.isSuccessful) {
                            getUserData(followerId, { user ->
                                sendNotification(
                                    targetUserId,
                                    followerId,
                                    "New Follower",
                                    "${user.username} started following you",
                                    "NEW_FOLLOWER",
                                    null
                                )
                            }, { error ->
                                sendNotification(
                                    targetUserId,
                                    followerId,
                                    "New Follower",
                                    "Someone started following you",
                                    "NEW_FOLLOWER",
                                    null
                                )
                            })
                        }
                        onComplete(followersTask.isSuccessful)
                    }
            } else {
                onComplete(false)
            }
        }
}

    // Unfollow a user
    fun unfollowUser(followerId: String, targetUserId: String, onComplete: (Boolean) -> Unit) {
        // Remove from current user's following list
        followingRef.child(followerId).child(targetUserId).removeValue()
            .addOnCompleteListener { followingTask ->
                if (followingTask.isSuccessful) {
                    // Remove from target user's followers list
                    followersRef.child(targetUserId).child(followerId).removeValue()
                        .addOnCompleteListener { followersTask ->
                            onComplete(followersTask.isSuccessful)
                        }
                } else {
                    onComplete(false)
                }
            }
    }

    // Check if user is following another user
    fun checkIfFollowing(followerId: String, targetUserId: String, onResult: (Boolean) -> Unit) {
        followingRef.child(followerId).child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.exists())
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onResult(false)
                }
            })
    }

    // Get all followers of a user
    fun getFollowers(userId: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        followersRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val followersList = mutableListOf<String>()
                    for (followerSnapshot in snapshot.children) {
                        val followerId = followerSnapshot.key
                        if (followerId != null) {
                            followersList.add(followerId)
                        }
                    }
                    onSuccess(followersList)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }
    fun getFollowing(userId: String, onSuccess: (List<String>) -> Unit, onFailure: (String) -> Unit) {
        followingRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val followingList = mutableListOf<String>()
                    for (followingSnapshot in snapshot.children) {
                        val followingId = followingSnapshot.key
                        if (followingId != null) {
                            followingList.add(followingId)
                        }
                    }
                    onSuccess(followingList)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    // Get follower count
    fun getFollowerCount(userId: String, onResult: (Int) -> Unit) {
        followersRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.childrenCount.toInt())
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onResult(0)
                }
            })
    }

    // Get following count
    fun getFollowingCount(userId: String, onResult: (Int) -> Unit) {
        followingRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.childrenCount.toInt())
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onResult(0)
                }
            })
    }

    // Send a notification to a user
    private fun sendNotification(
        recipientId: String,
        senderId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String?
    ) {
        val notification = UserNotification(
            id = notificationsRef.child(recipientId).push().key ?: return,
            senderId = senderId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            timestamp = System.currentTimeMillis(),
            read = false
        )
        
        notificationsRef.child(recipientId).child(notification.id).setValue(notification)
    }

    // Get all notifications for a user
    fun getNotifications(userId: String, onSuccess: (List<UserNotification>) -> Unit, onFailure: (String) -> Unit) {
        notificationsRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notificationsList = mutableListOf<UserNotification>()
                    for (notificationSnapshot in snapshot.children) {
                        val notification = notificationSnapshot.getValue(UserNotification::class.java)
                        if (notification != null) {
                            notificationsList.add(notification)
                        }
                    }
                    // Sort by timestamp (newest first)
                    onSuccess(notificationsList.sortedByDescending { it.timestamp })
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    // Mark a notification as read
    fun markNotificationAsRead(userId: String, notificationId: String, onComplete: (Boolean) -> Unit) {
        notificationsRef.child(userId).child(notificationId).child("read").setValue(true)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    // Mark all notifications as read
    fun markAllNotificationsAsRead(userId: String, onComplete: (Boolean) -> Unit) {
        getNotifications(userId, { notifications ->
            if (notifications.isEmpty()) {
                onComplete(true)
                return@getNotifications
            }
            
            var pendingUpdates = notifications.size
            var allSuccessful = true
            
            for (notification in notifications) {
                if (!notification.read) {
                    markNotificationAsRead(userId, notification.id) { success ->
                        allSuccessful = allSuccessful && success
                        pendingUpdates--
                        if (pendingUpdates == 0) {
                            onComplete(allSuccessful)
                        }
                    }
                } else {
                    pendingUpdates--
                    if (pendingUpdates == 0) {
                        onComplete(allSuccessful)
                    }
                }
            }
        }, { error ->
            onComplete(false)
        })
    }

    // Get unread notification count
    fun getUnreadNotificationCount(userId: String, onResult: (Int) -> Unit) {
        notificationsRef.child(userId).orderByChild("read").equalTo(false)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.childrenCount.toInt())
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onResult(0)
                }
            })
    }

    // Method to get quotes by tag
    fun getQuotesByTag(tag: String, onSuccess: (List<Quote>) -> Unit, onFailure: (String) -> Unit) {
        quotesRef.orderByChild("tags/0").equalTo(tag)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val quotesList = mutableListOf<Quote>()
                    
                    for (quoteSnapshot in snapshot.children) {
                        val quote = quoteSnapshot.getValue(Quote::class.java)
                        if (quote != null) {
                            quotesList.add(quote)
                        }
                    }
                    
                    // For multi-tag search
                    getAllQuotes({ allQuotes ->
                        for (quote in allQuotes) {
                            if (!quotesList.contains(quote) && quote.tags.contains(tag)) {
                                quotesList.add(quote)
                            }
                        }
                        onSuccess(quotesList)
                    }, { error ->
                        onSuccess(quotesList)
                    })
                }
                
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    fun getAllUsers(onComplete: (List<User>) -> Unit) {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val username = userSnapshot.child("username").getValue(String::class.java) ?: continue
                    val bio = userSnapshot.child("bio").getValue(String::class.java) ?: ""
                    
                    users.add(User(id = userId, username = username, bio = bio))
                }
                
                onComplete(users)
            }
            
            override fun onCancelled(error: DatabaseError) {
                onComplete(emptyList())
            }
        })
    }

    /**
     * Deletes a quote from the main quotes collection and user's quotes
     */
    fun deleteQuote(quoteId: String, userId: String, onComplete: (Boolean) -> Unit) {
        // Create a batch operation to delete from multiple locations
        val updates = HashMap<String, Any?>()
        
        // Delete from main quotes collection
        updates["/quotes/$quoteId"] = null
        
        // Delete from user quotes
        updates["/userQuotes/$userId/$quoteId"] = null
        
        // Execute all deletions as a single operation
        database.reference.updateChildren(updates)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
}