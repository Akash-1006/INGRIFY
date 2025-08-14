package com.ingrify.app

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ingrify.app.UserSessionManager
import com.ingrify.app.RetrofitClient

class HomeFragment : Fragment() {

    private lateinit var welcomeMessageTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)



    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        welcomeMessageTextView = view.findViewById(R.id.tv_welcome_message)

        fetchUserProfile()

        val hamburger = view.findViewById<ImageView>(R.id.Hamburger_menu)

        hamburger.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.ham_home -> {
                        val allergenFragment = HomeFragment()
                        (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                            .selectedItemId = R.id.navigation_profile
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, allergenFragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.ham_scan -> { val scanFragment = ScanFragment()
                        (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                            .selectedItemId = R.id.navigation_scan
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, scanFragment)
                            .addToBackStack(null)
                            .commit()
                        true }
                    R.id.ham_search -> {  val searchFragment = SearchFragment()
                        (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                            .selectedItemId = R.id.navigation_search
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, searchFragment)
                            .addToBackStack(null)
                            .commit()
                        true }
                    R.id.ham_allergen -> { val allergenFragment = AllergenFragment()
                        (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                            .selectedItemId = R.id.navigation_profile
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, allergenFragment)
                            .addToBackStack(null)
                            .commit()
                        true }
                    R.id.ham_profile -> { val allergenFragment = ProfileFragment()
                        (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                            .selectedItemId = R.id.navigation_profile
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, allergenFragment)
                            .addToBackStack(null)
                            .commit()
                        true }
                    R.id.ham_settings -> { val allergenFragment = SettingsFragment()
                        (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                            .selectedItemId = R.id.navigation_profile
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, allergenFragment)
                            .addToBackStack(null)
                            .commit()
                        true }
                    else -> false
                }
            }
            popup.show()
        }

        // Setup click listeners for the MaterialCardView elements for fragment navigation
        view.findViewById<MaterialCardView>(R.id.card_scan).setOnClickListener {
            // Create an instance of the ScanFragment
            val scanFragment = ScanFragment()
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                .selectedItemId = R.id.navigation_scan
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, scanFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialCardView>(R.id.card_search).setOnClickListener {
            val searchFragment = SearchFragment()
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                .selectedItemId = R.id.navigation_search
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialCardView>(R.id.card_allergen).setOnClickListener {
            val allergenFragment = AllergenFragment()
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                .selectedItemId = R.id.navigation_profile
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, allergenFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchUserProfile() {
        val authToken = UserSessionManager.getAuthToken()

        if (authToken.isNullOrEmpty()) {
            welcomeMessageTextView.text ="Hello, User!!"
            context?.let {
                Toast.makeText(it, "Please log in to personalize your experience.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val authHeader = "Bearer $authToken"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUserProfile(authHeader)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val userProfile = response.body()
                        val userName = userProfile?.name
                        if (!userName.isNullOrEmpty()) {
                            welcomeMessageTextView.text = "Hello, ${userName}!!"
                            UserSessionManager.saveUserName(userName)
                        } else {
                            welcomeMessageTextView.text = "Hello, User!!"
                            context?.let {
                                Toast.makeText(it, "User name not found in profile.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        welcomeMessageTextView.text = "Hello, User!!"
                        context?.let {
                            Toast.makeText(it, "Failed to fetch profile: ${errorBody ?: response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    welcomeMessageTextView.text = "Hello, User!!"
                    context?.let {
                        Toast.makeText(it, "Network error fetching profile: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    e.printStackTrace()
                }
            }
        }
    }
}
