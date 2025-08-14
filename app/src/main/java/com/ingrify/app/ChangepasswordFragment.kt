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

class ChangepasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_changepassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views by their IDs
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_changepassword)

        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }}