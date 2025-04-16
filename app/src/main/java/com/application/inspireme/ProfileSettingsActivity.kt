package com.application.inspireme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileSettingsActivity : AppCompatActivity() {
    private val REQUEST_BANNER_IMAGE = 0
    private val REQUEST_PROFILE_IMAGE = 1

    private var currentImageRequest = REQUEST_BANNER_IMAGE

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. You won't be able to select custom images.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    // Instead of directly saving, launch the cropping activity
                    launchImageCropper(selectedImageUri)
                }
            }
        }

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val resultUri = UCrop.getOutput(result.data!!)
                if (resultUri != null) {
                    // Get the absolute path from the URI
                    val filePath = resultUri.path

                    when (currentImageRequest) {
                        REQUEST_BANNER_IMAGE -> {
                            customBannerUri = filePath
                            loadImageFromUri(filePath, bannerImageView)
                        }
                        REQUEST_PROFILE_IMAGE -> {
                            customProfileUri = filePath
                            loadImageFromUri(filePath, profilePic)
                        }
                    }
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                Toast.makeText(this, "Image cropping failed: ${cropError?.message}", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var bannerImageView: ImageView
    private lateinit var profilePic: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    private var selectedBannerResId = R.drawable.banner3
    private var selectedProfileResId = R.drawable.profile

    private var customBannerUri: String? = null
    private var customProfileUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        bannerImageView = findViewById(R.id.bannerImageView)
        profilePic = findViewById(R.id.ProfilePic)
        usernameEditText = findViewById(R.id.Username)
        bioEditText = findViewById(R.id.Bio)
        val saveButton: MaterialButton = findViewById(R.id.Save)
        val cancelButton: MaterialButton = findViewById(R.id.Cancel)

        findViewById<ImageView>(R.id.back_icon_left).setOnClickListener {
            onBackPressed()
        }

        loadSavedData()

        bannerImageView.setOnClickListener {
            currentImageRequest = REQUEST_BANNER_IMAGE
            checkAndRequestPermission()
        }

        profilePic.setOnClickListener {
            currentImageRequest = REQUEST_PROFILE_IMAGE
            checkAndRequestPermission()
        }

        saveButton.setOnClickListener { saveData() }
        cancelButton.setOnClickListener { finish() }

        // Add this to your onCreate method
        findViewById<ImageButton>(R.id.editBannerButton).setOnClickListener {
            if (customBannerUri != null) {
                currentImageRequest = REQUEST_BANNER_IMAGE
                reCropExistingImage(customBannerUri!!)
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.editProfileButton).setOnClickListener {
            if (customProfileUri != null) {
                currentImageRequest = REQUEST_PROFILE_IMAGE
                reCropExistingImage(customProfileUri!!)
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkAndRequestPermission() {
        val permission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_IMAGES
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Manifest.permission.READ_EXTERNAL_STORAGE
            else -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun launchImageCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${UUID.randomUUID()}.jpg"))

        val uCropOptions = UCrop.Options().apply {
            setCompressionQuality(90)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(true)
            setShowCropGrid(true)
            setShowCropFrame(true)
            setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL)
            setMaxScaleMultiplier(5f)
            setCircleDimmedLayer(currentImageRequest == REQUEST_PROFILE_IMAGE) // Make profile crop circular
            setToolbarTitle(if (currentImageRequest == REQUEST_BANNER_IMAGE) "Crop Banner" else "Crop Profile Picture")

            setToolbarColor(ContextCompat.getColor(this@ProfileSettingsActivity, R.color.white))
            setStatusBarColor(ContextCompat.getColor(this@ProfileSettingsActivity, R.color.black))
            setToolbarWidgetColor(ContextCompat.getColor(this@ProfileSettingsActivity, R.color.black))
        }

        val uCropIntent = when (currentImageRequest) {
            REQUEST_BANNER_IMAGE -> {
                // For banner, use a wider aspect ratio (e.g., 16:9)
                UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(16f, 9f)
                    .withOptions(uCropOptions)
                    .getIntent(this)
            }
            REQUEST_PROFILE_IMAGE -> {
                // For profile picture, use a 1:1 (square) aspect ratio
                UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withOptions(uCropOptions)
                    .getIntent(this)
            }
            else -> {
                UCrop.of(sourceUri, destinationUri)
                    .withOptions(uCropOptions)
                    .getIntent(this)
            }
        }

        cropImageLauncher.launch(uCropIntent)
    }
    private fun reCropExistingImage(imagePath: String) {
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            val sourceUri = Uri.fromFile(imageFile)
            launchImageCropper(sourceUri)
        } else {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageFromUri(uri: String?, imageView: ImageView) {
        if (uri != null) {
            val imageFile = File(uri)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun loadSavedData() {
        customBannerUri = sharedPreferences.getString("customBannerUri", null)
        customProfileUri = sharedPreferences.getString("customProfileUri", null)

        selectedBannerResId = sharedPreferences.getInt("bannerImageResId", R.drawable.banner3)
        selectedProfileResId = sharedPreferences.getInt("profileImageResId", R.drawable.profile)

        if (customBannerUri != null) {
            loadImageFromUri(customBannerUri, bannerImageView)
        } else {
            bannerImageView.setImageResource(selectedBannerResId)
        }

        if (customProfileUri != null) {
            loadImageFromUri(customProfileUri, profilePic)
        } else {
            profilePic.setImageResource(selectedProfileResId)
        }

        usernameEditText.setText(sharedPreferences.getString("username", ""))
        bioEditText.setText(sharedPreferences.getString("bio", ""))
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()

        editor.putInt("bannerImageResId", selectedBannerResId)
        editor.putInt("profileImageResId", selectedProfileResId)

        if (customBannerUri != null) {
            editor.putString("customBannerUri", customBannerUri)
        }

        if (customProfileUri != null) {
            editor.putString("customProfileUri", customProfileUri)
        }

        editor.putString("username", usernameEditText.text.toString())
        editor.putString("bio", bioEditText.text.toString())
        editor.apply()

        finish()
    }
}