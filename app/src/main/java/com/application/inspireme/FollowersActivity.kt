package com.application.inspireme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.adapter.FollowerAdapter
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.model.User

class FollowersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var followerAdapter: FollowerAdapter
    private val followersList = mutableListOf<User>()
    private var userId: String? = null
    private lateinit var noFollowersTextView: TextView


    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)

        val toolbar: Toolbar = findViewById(R.id.toolbar_followers)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Followers"

        userId = intent.getStringExtra(EXTRA_USER_ID)
        noFollowersTextView = findViewById(R.id.textView_no_followers)

        if (userId == null) {
            Toast.makeText(this, "User ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerView_followers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        followerAdapter = FollowerAdapter(this, followersList) { follower ->
            // Navigate to follower's profile
            // This assumes your MainActivity hosts ProfileFragment and can handle this intent
            val intent = Intent(this, NavigationBarActivity::class.java).apply {
                putExtra("navigateTo", "ProfileFragment")
                putExtra("arg_user_id", follower.id)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        recyclerView.adapter = followerAdapter

        loadFollowers(userId!!)
    }

    private fun loadFollowers(userIdToLoad: String) {
        FirebaseManager.getFollowerIds(userIdToLoad,
            onSuccess = { followerIds ->
                if (followerIds.isEmpty()) {
                    noFollowersTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    followersList.clear()
                    followerAdapter.updateFollowers(followersList)
                    return@getFollowerIds
                }

                noFollowersTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                followersList.clear()
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
                            Log.e("FollowersActivity", "Error fetching data for follower $followerId: $error")
                            if (loadedCount == totalFollowers) {
                                followerAdapter.updateFollowers(followersList.sortedBy { it.username.toLowerCase() })
                            }
                        }
                    )
                }
            },
            onFailure = { error ->
                Toast.makeText(this, "Failed to load followers: $error", Toast.LENGTH_SHORT).show()
                noFollowersTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Log.e("FollowersActivity", "Error fetching follower IDs: $error")
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
