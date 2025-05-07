package com.application.inspireme

import android.app.Activity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PrivacyActivity : Activity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var currentEmailEditText: EditText
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentEmailEditText = findViewById(R.id.currentEmail)
        currentEmailEditText.setText(currentUser.email)
        currentEmailEditText.isEnabled = false

        // Initialize views
        currentPasswordEditText = findViewById(R.id.currentPassword)
        newPasswordEditText = findViewById(R.id.newPassword)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)


        // Setup password visibility toggles
        setupPasswordToggle(findViewById(R.id.toggleCurrentPassword), currentPasswordEditText)
        setupPasswordToggle(findViewById(R.id.toggleNewPassword), newPasswordEditText)
        setupPasswordToggle(findViewById(R.id.toggleConfirmPassword), confirmPasswordEditText)

        // Set up button click listeners
        findViewById<com.google.android.material.button.MaterialButton>(R.id.Save).setOnClickListener {
            updatePassword()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.Cancel).setOnClickListener {
            finish()
        }

        val backBtn = findViewById<ImageView>(R.id.back_icon_left)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupPasswordToggle(button: ImageButton, editText: EditText) {
        button.setOnClickListener {
            if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                button.setImageResource(R.drawable.ic_visibility)
            } else {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                button.setImageResource(R.drawable.ic_visibility_off)
            }
            editText.setSelection(editText.text.length)
        }
    }

    private fun updatePassword() {
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Validate inputs
        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "Current password is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "New password is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Re-authenticate user first
        val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Update password
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { passwordTask ->
                            if (passwordTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Password updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Clear all password fields
                                currentPasswordEditText.text.clear()
                                newPasswordEditText.text.clear()
                                confirmPasswordEditText.text.clear()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to update password: ${passwordTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${reauthTask.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}