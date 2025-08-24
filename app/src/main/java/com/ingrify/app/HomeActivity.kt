package com.ingrify.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ingrify.app.R

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        UserSessionManager.init(applicationContext)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> switchFragment(HomeFragment(), "HOME")
                R.id.navigation_scan -> switchFragment(ScanFragment(), "SCAN")
                R.id.navigation_search -> switchFragment(SearchFragment(), "SEARCH")
                R.id.navigation_profile -> switchFragment(ProfileFragment(), "PROFILE")
                else -> false
            }
        }

        // Listen for back stack changes to update nav selection
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (currentFragment) {
                is HomeFragment -> bottomNavigationView.menu.findItem(R.id.navigation_home).isChecked = true
                is ScanFragment -> bottomNavigationView.menu.findItem(R.id.navigation_scan).isChecked = true
                is SearchFragment -> bottomNavigationView.menu.findItem(R.id.navigation_search).isChecked = true
                is ProfileFragment, is AllergenFragment -> bottomNavigationView.menu.findItem(R.id.navigation_profile).isChecked = true
            }
        }

        if (savedInstanceState == null) {
            switchFragment(HomeFragment(), "HOME")
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Avoid reloading same fragment
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return true
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
        return true
    }
}
