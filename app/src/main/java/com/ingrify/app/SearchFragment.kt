package com.ingrify.app

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment // Make sure this import is present and correct
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class SearchFragment : Fragment() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchResultsTextView: TextView
    private lateinit var tilSearchInput: TextInputLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using the 'view' parameter (the inflated layout)
        tilSearchInput = view.findViewById(R.id.til_search_input_custom)
        searchEditText = view.findViewById(R.id.et_search_query_custom)
//        searchResultsTextView = view.findViewById(R.id.tv_search_results)
//        val performSearchButton: MaterialButton = view.findViewById(R.id.btn_perform_search)

        // 1. Set OnClickListener for the endIcon (assuming it's part of your TextInputLayout)
        tilSearchInput.setEndIconOnClickListener {
            handleSearchAction() // Call a shared function for search logic
        }

        // 2. Set OnKeyListener for the keyboard's "Enter" button
        searchEditText.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                handleSearchAction()
                true
            } else {
                false
            }
        }

        // Keep your existing button listener if you still need a separate button
//        performSearchButton.setOnClickListener {
//            handleSearchAction() // Call the shared function
//        }
    }

    /**
     * Centralized function to handle the search action.
     * This avoids code duplication and keeps your search logic in one place.
     */
    private fun handleSearchAction() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            // CRITICAL CHANGE: Use requireContext() for Toast
            Toast.makeText(requireContext(), "Please enter a search query.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Your actual search implementation.
     * For demonstration, it updates a TextView and shows a Toast.
     * Replace this with your real search logic (e.g., API call, filtering a list).
     *
     * @param query The search query entered by the user.
     */
    private fun performSearch(query: String) {
        // Example: Update a TextView with search results
        searchResultsTextView.text = "Searching for: \"$query\"..."
        // CRITICAL CHANGE: Use requireContext() for Toast
        Toast.makeText(requireContext(), "Search initiated for: $query", Toast.LENGTH_SHORT).show()

        // Hide the keyboard after search
        hideKeyboard(searchEditText)

        // ### IMPORTANT: Add your actual search logic here ###
        // For example:
        // val searchViewModel: SearchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        // searchViewModel.search(query)
        // Or if you have a local list:
        // filterList(query)
    }

    /**
     * Helper function to hide the soft keyboard.
     *
     * @param view The view from which to hide the keyboard.
     */
    private fun hideKeyboard(view: View) {
        // CRITICAL CHANGE: Use requireContext().getSystemService() for InputMethodManager
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
