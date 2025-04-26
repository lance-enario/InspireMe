package com.application.inspireme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.application.inspireme.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : Activity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var progressBar: ProgressBar? = null
    private val TAG = "RegisterActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)

        val emailInput = findViewById<EditText>(R.id.Username)
        val passwordInput = findViewById<EditText>(R.id.Password)
        val confirmPasswordInput = findViewById<EditText>(R.id.ConfirmPassword)
        val signupButton = findViewById<Button>(R.id.SignupButton)
        val loginButton = findViewById<TextView>(R.id.SigninText)
        progressBar = findViewById(R.id.progressBar)

        // Create progress bar if it doesn't exist in layout
        if (progressBar == null) {
            progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
            progressBar?.visibility = View.GONE

            // Add progress bar to layout
            val layout = findViewById<ConstraintLayout>(R.id.registerContainer)
            if (layout != null) {
                val params = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

                layout.addView(progressBar, params)
            }
        }

        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validate inputs
            if (email.isEmpty()) {
                Toast.makeText(this, "Email is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show progress bar
            progressBar?.visibility = View.VISIBLE

            // Create user with email and password using Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        val userId = user?.uid

                        // Save additional user information to the Realtime Database
                        if (userId != null) {
                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("users")
                            
                            val user = User(
                                email = email.lowercase(),
                                username = email.substringBefore("@"),
                                bio = "No bio available",
                                createdAt = System.currentTimeMillis()
                            )
                            
                            usersRef.child(userId).setValue(user)
                                .addOnCompleteListener { dbTask ->
                                    progressBar?.visibility = View.GONE
                                    
                                    if (dbTask.isSuccessful) {
                                        // Sign out the user as we want them to sign in via the login page
                                        auth.signOut()
                                        
                                        Toast.makeText(this@RegisterActivity, 
                                            "Registration successful! Please login.", 
                                            Toast.LENGTH_SHORT).show()
                                            
                                        // Navigate back to login activity
                                        finish()
                                    } else {
                                        Log.e(TAG, "Failed to save user data: ${dbTask.exception?.message}")
                                        Toast.makeText(this@RegisterActivity, 
                                            "Registration successful, but failed to save profile data", 
                                            Toast.LENGTH_SHORT).show()
                                        
                                        // Sign out the user
                                        auth.signOut()
                                        finish()
                                    }
                                }
                        }
                    } else {
                        // If sign up fails, display a message to the user.
                        progressBar?.visibility = View.GONE
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@RegisterActivity, 
                            "Registration failed: ${task.exception?.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        loginButton.setOnClickListener {
            finish()
        }
    }
}