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
import androidx.constraintlayout.widget.ConstraintLayout

class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views by their IDs
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_help)

        // Set OnClickListener for the back button
        backButton.setOnClickListener {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }


        val expandToggleIcon = view.findViewById<ImageView>(R.id.iv_expand_toggle1)
        val ocrMenu = view.findViewById<MaterialCardView>(R.id.Q_1)
        val collapsibleDetailsLayout = view.findViewById<View>(R.id.cl_collapsible_details)
        var isExpanded = false
        ocrMenu.setOnClickListener {
            isExpanded = !isExpanded
            collapsibleDetailsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            expandToggleIcon.setImageResource(
                if (isExpanded) R.drawable.ic_drop_down else R.drawable.ic_right
            )}

        val expandToggleIcontwo = view.findViewById<ImageView>(R.id.iv_expand_toggle2)
        val ocrMenutwo = view.findViewById<MaterialCardView>(R.id.Q_2)
        val collapsibleDetailsLayouttwo = view.findViewById<View>(R.id.cl_collapsible_details1)
        var isExpandedone = false
        ocrMenutwo.setOnClickListener {
            isExpandedone = !isExpandedone
            collapsibleDetailsLayouttwo.visibility = if (isExpandedone) View.VISIBLE else View.GONE
            expandToggleIcontwo.setImageResource(
                if (isExpandedone) R.drawable.ic_drop_down else R.drawable.ic_right
            )}

        val expandToggleIconthree = view.findViewById<ImageView>(R.id.iv_expand_toggle3)
        val ocrMenuthree = view.findViewById<MaterialCardView>(R.id.Q_3)
        val collapsibleDetailsLayoutthree = view.findViewById<View>(R.id.cl_collapsible_details3)
        var isExpandedthree = false
        ocrMenuthree.setOnClickListener {
            isExpandedthree = !isExpandedthree
            collapsibleDetailsLayoutthree.visibility = if (isExpandedthree) View.VISIBLE else View.GONE
            expandToggleIconthree.setImageResource(
                if (isExpandedthree) R.drawable.ic_drop_down else R.drawable.ic_right
            )}

    }}