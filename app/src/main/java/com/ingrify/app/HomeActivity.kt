package com.ingrify.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        UserSessionManager.init(applicationContext)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.navigation_home -> {
                    selectedFragment = HomeFragment()
                }
                R.id.navigation_scan -> {
                    selectedFragment = ScanFragment()
                }
                R.id.navigation_search -> {
                    selectedFragment = SearchFragment()
                }
               R.id.navigation_profile -> {
                  selectedFragment = ProfileFragment()
                }
                else -> false
            }


            selectedFragment?.let { fragment ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
                true
            } ?: false
        }


        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }
}