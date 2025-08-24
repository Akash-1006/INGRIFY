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
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AppCompatDelegate // For dark mode toggle
import androidx.lifecycle.lifecycleScope
import com.ingrify.app.UserSessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private lateinit var usernameTextView: TextView

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
        usernameTextView = view.findViewById(R.id.username)
        val profileIconCard: MaterialCardView = view.findViewById(R.id.profile_icon)
        val settingsCard: MaterialCardView = view.findViewById(R.id.settings)
        val allergenDetailsCard: MaterialCardView = view.findViewById(R.id.Allergen_details)
        val changePasswordCard: MaterialCardView = view.findViewById(R.id.Change_password)
        val deleteAccountCard: MaterialCardView = view.findViewById(R.id.Delete_account)
        val logOutCard: MaterialCardView = view.findViewById(R.id.Log_out)
        fetchUserProfile()
        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
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
            Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show()

            UserSessionManager.clearSession()

            val intent = android.content.Intent(requireContext(), MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

    }
    private fun fetchUserProfile() {
        val token = UserSessionManager.getAuthToken()
        val name= UserSessionManager.getName()
        if (token.isNullOrEmpty()) {
            usernameTextView.text = "User"
            return
        }
        else{
            usernameTextView.text = "${name}" ?: "User"
        }


    }
}