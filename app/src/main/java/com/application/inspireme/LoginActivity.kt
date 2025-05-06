package com.application.inspireme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : Activity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var progressBar: ProgressBar? = null
    private val TAG = "LoginActivity"
    private lateinit var auth: FirebaseAuth

    private fun togglePasswordVisibility(editText: EditText, button: ImageButton) {
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(editText.text.length)
    }
    private fun startMainActivity() {
        val intent = Intent(this, NavigationBarActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()



        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        val togglePassword = findViewById<ImageButton>(R.id.togglePassword)
        val passwordInput = findViewById<EditText>(R.id.Password)
        togglePassword.setOnClickListener {
            togglePasswordVisibility(passwordInput, togglePassword)
        }

        val buttonLogin = findViewById<Button>(R.id.LoginButton)
        progressBar = findViewById(R.id.progressBar)

        // Create progress bar if it doesn't exist in layout
        if (progressBar == null) {
            progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
            progressBar?.visibility = View.GONE

            // Add progress bar to layout
            val layout = findViewById<ConstraintLayout>(R.id.loginContainer)
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

        buttonLogin.setOnClickListener {
            val emailInput = findViewById<EditText>(R.id.Username)?.text.toString().trim()
            val passwordInput = findViewById<EditText>(R.id.Password)?.text.toString().trim()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "Email is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordInput.isEmpty()) {
                Toast.makeText(this, "Password is required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show progress indicator
            progressBar?.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        
                        // Save login status and user ID
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("userId", user?.uid)
                        editor.apply()

                        progressBar?.visibility = View.GONE
                        startMainActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        progressBar?.visibility = View.GONE
                        Toast.makeText(
                            baseContext, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        val buttonRegister = findViewById<TextView>(R.id.SignupText)
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            startMainActivity()
        }
    }
}