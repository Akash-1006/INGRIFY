package com.ingrify.app

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import android.content.SharedPreferences

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
        val darkMode = view.findViewById<SwitchCompat>(R.id.darkModeSwitch)
        val notification = view.findViewById<SwitchCompat>(R.id.NotificationSwitch)
        val permission: MaterialCardView = view.findViewById(R.id.Permission)
        val help: MaterialCardView = view.findViewById(R.id.Help)
        val about: MaterialCardView = view.findViewById(R.id.About)
        val sharedPreferences = requireContext().getSharedPreferences("settings", MODE_PRIVATE)

        backButton.setOnClickListener {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        val isNotiON = sharedPreferences.getBoolean("Notifications", false)
        darkMode.isChecked = isDarkMode

        darkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("dark_mode", isChecked)
                .apply()

            // Show toast only when enabling dark mode
            if (isChecked) {
                Toast.makeText(requireContext(), "Dark mode is in beta", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(requireContext(), "Thanks for Understanding :)", Toast.LENGTH_SHORT).show()
            }
        }

        notification.isChecked = isNotiON

        notification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("Notifications", isChecked)
                .apply()

            if (isChecked) {
                Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
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