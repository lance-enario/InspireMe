package com.application.inspireme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signupButton = findViewById<Button>(R.id.SignupButton)
        val loginButton = findViewById<TextView>(R.id.SigninText)

        signupButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}