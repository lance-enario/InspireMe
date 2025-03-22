package com.application.inspireme

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class NavigationBarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_bar)

        val homefragment = HomeFragment()
        val profilefragment = ProfileFragment()
        val btnHome = findViewById<ImageButton>(R.id.Homebutton)
        val btnProfile = findViewById<ImageButton>(R.id.Profilebutton)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, homefragment)
            commit()
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
}