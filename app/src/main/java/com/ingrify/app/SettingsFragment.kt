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

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views by their IDs
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_settings)
        val settingsTitle: TextView = view.findViewById(R.id.Settings_title)
        val darkMode: MaterialCardView = view.findViewById(R.id.DarkMode)
        val notification: MaterialCardView = view.findViewById(R.id.Notifications)
        val permission: MaterialCardView = view.findViewById(R.id.Permission)
        val help: MaterialCardView = view.findViewById(R.id.Help)
        val about: MaterialCardView = view.findViewById(R.id.About)

        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        // Set OnClickListeners for MaterialCardViews


       permission.setOnClickListener {
            val permissionFragment = PermissionsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, permissionFragment)
                .addToBackStack(null)
                .commit()
        }

        help.setOnClickListener {
            val helpFragment = HelpFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, helpFragment)
                .addToBackStack(null)
                .commit()
        }

        about.setOnClickListener {
           val aboutFragment = AboutFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, aboutFragment)
                .addToBackStack(null)
                .commit()
        }

    }
}