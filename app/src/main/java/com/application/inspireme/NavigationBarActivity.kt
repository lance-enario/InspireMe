package com.application.inspireme

import HomeFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationBarActivity : AppCompatActivity() {

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_bar)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Load the default fragment (e.g., HomeFragment)
        loadFragment(HomeFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Homebutton -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.CategoryButton -> {
                    //loadFragment(CategoryFragment())
                    true
                }
                R.id.Notifbutton -> {
                    //loadFragment(NotificationFragment())
                    true
                }
                R.id.Profilebutton -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent navigating back to the login screen
        // Show a dialog or custom behavior if needed
    }
}