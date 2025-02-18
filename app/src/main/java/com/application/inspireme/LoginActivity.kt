package com.application.inspireme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


            val button_login = findViewById<Button>(R.id.LoginButton)
            button_login.setOnClickListener {
                val username = (findViewById<EditText>(R.id.Username)).text.toString().trim()
                val password = (findViewById<EditText>(R.id.Password)).text.toString().trim()

                if(username != "test" || password != "123"){
                    Toast.makeText(this, "Invalid Credentials. Please try again!", Toast.LENGTH_SHORT).show()
                } else {
                val intent = Intent(this, LandingPageActivity::class.java)
                startActivity(intent)
            }
        }
    }
}