package com.application.inspireme

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class NavigationBarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_bar)

        val homefragment = HomeFragment()
        val profilefragment = ProfileFragment()
        val btnHome = findViewById<ImageButton>(R.id.Homebutton)
        val btnProfile = findViewById<ImageButton>(R.id.Profilebutton)

        if (intent.getBooleanExtra("openProfileFragment", false)) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, profilefragment)
                commit()
            }
        } else {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, homefragment)
                commit()
            }
        }

        btnHome.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, homefragment)
                commit()
            }
        }

        btnProfile.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, profilefragment)
                commit()
            }
        }
    }

    override fun onBackPressed() {
        // Prevent going back to login screen by not calling super.onBackPressed()
        // You can show a dialog asking if the user wants to exit the app instead
    }
}