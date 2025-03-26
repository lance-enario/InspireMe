package com.application.inspireme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class RegisterActivity : Activity() {
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)

        val emailInput = findViewById<EditText>(R.id.Username)
        val passwordInput = findViewById<EditText>(R.id.Password)
        val confirmPasswordInput = findViewById<EditText>(R.id.ConfirmPassword)
        val signupButton = findViewById<Button>(R.id.SignupButton)
        val loginButton = findViewById<TextView>(R.id.SigninText)

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
            
            // Save user data
            val editor = sharedPreferences.edit()
            editor.putString("email", email)
            editor.putString("password", password)
            editor.apply()
            
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
            
            // Return to login
            finish()
        }

        loginButton.setOnClickListener {
            finish()
        }
    }
}