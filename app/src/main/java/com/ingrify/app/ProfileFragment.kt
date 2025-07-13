package com.ingrify.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import androidx.navigation.fragment.findNavController // Import for navigation (if you're using Navigation Component)
import androidx.appcompat.app.AppCompatDelegate // For dark mode toggle

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views by their IDs
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_login)
        val profileTitle: TextView = view.findViewById(R.id.Profile_title)
        val profileIconCard: MaterialCardView = view.findViewById(R.id.profile_icon)
        val settingsCard: MaterialCardView = view.findViewById(R.id.settings)
        val allergenDetailsCard: MaterialCardView = view.findViewById(R.id.Allergen_details)
        val changePasswordCard: MaterialCardView = view.findViewById(R.id.Change_password)
        val deleteAccountCard: MaterialCardView = view.findViewById(R.id.Delete_account)
        val logOutCard: MaterialCardView = view.findViewById(R.id.Log_out)

        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            // Or if not using Navigation Component, you might use:
            // activity?.onBackPressed()
            Toast.makeText(context, "Back button clicked", Toast.LENGTH_SHORT).show()
        }

        // Set OnClickListeners for MaterialCardViews
        profileIconCard.setOnClickListener {
            val AccountFragment = AccountFragment()
            // Begin a fragment transaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AccountFragment)
                .addToBackStack(null)
                .commit()
        }

        settingsCard.setOnClickListener {
            val settingsFragment = SettingsFragment()
            // Begin a fragment transaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit()
        }

        allergenDetailsCard.setOnClickListener {
            val allergenFragment = AllergenFragment()
            // Begin a fragment transaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, allergenFragment)
                .addToBackStack(null)
                .commit()
        }

        changePasswordCard.setOnClickListener {
            val ChangepasswordFragment = ChangepasswordFragment()
            // Begin a fragment transaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangepasswordFragment)
                .addToBackStack(null)
                .commit()
        }

        deleteAccountCard.setOnClickListener {
            val deleteFragment = DeleteFragment()
            // Begin a fragment transaction
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, deleteFragment)
                .addToBackStack(null)
                .commit()
        }

        logOutCard.setOnClickListener {
            Toast.makeText(context, "Log Out clicked!", Toast.LENGTH_SHORT).show()
            // Example: Perform logout operation (clear user session, navigate to login)
            // performLogout()
        }
    }
}