package com.application.inspireme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.navigation.NavigationBarView

class NavigationBarActivity : AppCompatActivity() {

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_bar)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        loadFragment(HomeFragment()) // Load the default fragment

        // Set label visibility to show only when selected
        bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            bottomNavigationView.menu.setGroupCheckable(0, true, true)
            when (item.itemId) {
                R.id.Homebutton -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.CategoryButton -> {
                    loadFragment(CategoryFragment())
                    true
                }
                R.id.Notifbutton -> {
                    loadFragment(NotificationFragment())
                    true
                }
                R.id.Profilebutton -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        val fabCreate = findViewById<FloatingActionButton>(R.id.fab)
        fabCreate.setOnClickListener {
            bottomNavigationView.menu.setGroupCheckable(0, false, true)
            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isChecked = false
            }

            bottomNavigationView.clearFocus()
            loadFragment(CreateFragment(), addToBackStack = true)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
