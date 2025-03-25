package com.application.inspireme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        val emaill = "test@gmail.com"
//        val password = "123"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonLogin = findViewById<Button>(R.id.LoginButton)
        buttonLogin.setOnClickListener {
            val usernameInput = findViewById<EditText>(R.id.Username).text.toString().trim()
            val passwordInput = findViewById<EditText>(R.id.Password).text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(usernameInput).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

//            if (usernameInput != emaill || passwordInput != password) {
//                Toast.makeText(this, "Invalid Credentials. Please try again!", Toast.LENGTH_SHORT).show()
//            } else {
                val intent = Intent(this, NavigationBarActivity::class.java)
                startActivity(intent)
//            }
        }

        val buttonRegister = findViewById<TextView>(R.id.SignupText)
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
