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

class LoginActivity : Activity() {
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
        
        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startMainActivity()
            return
        }
        
        setContentView(R.layout.activity_login)

        val buttonLogin = findViewById<Button>(R.id.LoginButton)
        buttonLogin.setOnClickListener {
            val emailInput = findViewById<EditText>(R.id.Username).text.toString().trim()
            val passwordInput = findViewById<EditText>(R.id.Password).text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the user exists in SharedPreferences
            val registeredEmail = sharedPreferences.getString("email", "")
            val registeredPassword = sharedPreferences.getString("password", "")

            if (emailInput == registeredEmail && passwordInput == registeredPassword) {
                // Save login status
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                startMainActivity()
            } else {
                Toast.makeText(this, "Invalid credentials. Please try again!", Toast.LENGTH_SHORT).show()
            }
        }

        val buttonRegister = findViewById<TextView>(R.id.SignupText)
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, GenerateQuoteActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}