package com.application.inspireme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.data.UserProfileCache
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileSettingsActivity : AppCompatActivity() {
    private lateinit var bannerImageView: ImageView
    private lateinit var profilePic: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    private var selectedBannerId: String = "banner3"
    private var selectedProfileId: String = "profile1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        bannerImageView = findViewById(R.id.bannerImageView)
        profilePic = findViewById(R.id.ProfilePic)
        usernameEditText = findViewById(R.id.Username)
        bioEditText = findViewById(R.id.Bio)

        findViewById<ImageView>(R.id.back_icon_left).setOnClickListener { onBackPressed() }
        findViewById<MaterialButton>(R.id.Save).setOnClickListener { saveData() }
        findViewById<MaterialButton>(R.id.Cancel).setOnClickListener { finish() }

        bannerImageView.setOnClickListener { showImageSelectionDialog(true) }
        profilePic.setOnClickListener { showImageSelectionDialog(false) }

        loadSavedData()
    }

    private fun showAnimalSelectionDialog() {
        val animals = listOf(
            "Capybara" to "capybara",
            "Cat" to "cat",
            "Cat Footprint" to "cat_footprint",
            "Corgi" to "corgi",
            "Dog" to "dog",
            "Dog Paw" to "dog_paw",
            "Doge" to "doge",
            "Duck" to "duck",
            "Gorilla" to "gorilla"
        )

        val dialogView = layoutInflater.inflate(R.layout.dialog_image_picker, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.imageGrid).apply {
            layoutManager = GridLayoutManager(this@ProfileSettingsActivity, 2) // Fixed to 2 columns
            addItemDecoration(GridSpacingItemDecoration(2, 16.dpToPx(), true))
            adapter = ImageAdapter(
                animals.map { (name, id) -> UserProfileCache.profileImages[id]!! to id },
                false
            ) { selectedId ->
                selectedProfileId = selectedId
                profilePic.setImageResource(UserProfileCache.profileImages[selectedId]!!)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Profile Picture")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun showImageSelectionDialog(isBanner: Boolean) {
        if (!isBanner) {
            showAnimalSelectionDialog()
            return
        }
        val images = if (isBanner) UserProfileCache.bannerImages else UserProfileCache.profileImages
        val currentSelection = if (isBanner) selectedBannerId else selectedProfileId

        val dialogView = layoutInflater.inflate(R.layout.dialog_image_picker, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.imageGrid)

        recyclerView.layoutManager = if (isBanner) {
            LinearLayoutManager(this)
        } else {
            GridLayoutManager(this, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) = 1
                }
            }
        }

        // Create adapter with proper click handling
        val adapter = ImageAdapter(
            images.map { (id, resId) -> resId to id },
            isBanner
        ) { selectedId ->
            // This lambda will be called when an item is clicked
            try {
                if (isBanner) {
                    selectedBannerId = selectedId
                    bannerImageView.setImageResource(images[selectedId] ?: R.drawable.banner3)
                } else {
                    selectedProfileId = selectedId
                    profilePic.setImageResource(images[selectedId] ?: R.drawable.profile)
                }
            } catch (e: Exception) {
                Log.e("ImageSelection", "Error setting image: ${e.message}")
                Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
            }
        }

        adapter.setSelectedItem(currentSelection)
        recyclerView.adapter = adapter

        MaterialAlertDialogBuilder(this)
            .setTitle(if (isBanner) "Select Banner" else "Select Profile Picture")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Add this extension function
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    // Add this ItemDecoration class as an inner class
    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }

    // Remove all SharedPreferences references and modify saveData()
    private fun saveData() {
        val username = usernameEditText.text.toString()
        val bio = bioEditText.text.toString()

        // Update local cache
        UserProfileCache.username = username
        UserProfileCache.bio = bio
        UserProfileCache.bannerId = selectedBannerId
        UserProfileCache.profileId = selectedProfileId
        UserProfileCache.isDataLoaded = true
        UserProfileCache.lastUpdateTime = System.currentTimeMillis()

        // Save to Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let {
            val updates = mapOf(
                "username" to username,
                "bio" to bio,
                "bannerId" to selectedBannerId,
                "profileId" to selectedProfileId
            )

            FirebaseManager.updateUserProfile(it, updates) { success ->
                if (success) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Modify loadSavedData() to load from Firebase
    private fun loadSavedData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseManager.getUserData(userId,
            onSuccess = { user ->
                selectedBannerId = user.bannerId
                selectedProfileId = user.profileId

                // Update UI
                bannerImageView.setImageResource(UserProfileCache.bannerImages[selectedBannerId] ?: R.drawable.banner3)
                profilePic.setImageResource(UserProfileCache.profileImages[selectedProfileId] ?: R.drawable.capybara)
                usernameEditText.setText(user.username)
                bioEditText.setText(user.bio)

                // Update cache
                UserProfileCache.username = user.username
                UserProfileCache.bio = user.bio
                UserProfileCache.bannerId = user.bannerId
                UserProfileCache.profileId = user.profileId
            },
            onFailure = { error ->
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                // Set defaults
                bannerImageView.setImageResource(R.drawable.banner3)
                profilePic.setImageResource(R.drawable.capybara)
            }
        )
    }
}