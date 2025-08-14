package com.ingrify.app

import android.content.Context
import android.os.Bundle
import android.text.InputType // Import InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.core.view.isVisible

class AllergenFragment : Fragment() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var tilSearchInput: TextInputLayout
    private lateinit var progressBar: ProgressBar



    private val FRAGMENT_CONTAINER_ID = R.id.fragment_container

    private val gson = Gson()

    // Store the original input type to restore it later
    private var originalSearchInputType: Int = InputType.TYPE_CLASS_TEXT // Default to text

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_allergen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using findViewById
        tilSearchInput = view.findViewById(R.id.til_search_input_custom)
        searchEditText = view.findViewById(R.id.et_search_query_custom)
        progressBar = view.findViewById(R.id.progress_bar)

        // Store the original input type for the searchEditText
        originalSearchInputType = searchEditText.inputType

        // Ensure searchEditText is clickable by default if it wasn't already.
        // This allows us to toggle its clickable state.
        searchEditText.isClickable = true
        searchEditText.isFocusable = true
        searchEditText.isFocusableInTouchMode = true


        // Set the click listener for the search icon (end icon) in the TextInputLayout
        tilSearchInput.setEndIconOnClickListener {
            handleSearchAction()
        }

        // Set the editor action listener for the TextInputEditText (for keyboard "Search" or "Done" button)
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                handleSearchAction()
                true // Consume the event
            } else {
                false // Do not consume the event
            }
        }
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_allergen)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * Handles the initiation of a search action.
     * Prevents new searches if one is already in progress.
     */
    private fun handleSearchAction() {
        // Guard clause: If the progress bar is currently visible,
        // it means a search operation is already ongoing.
        // In this case, we ignore subsequent requests to start a new search.
        if (progressBar.isVisible) {
            Log.d("SearchFragment", "Search already in progress, ignoring new action.")
            return
        }

        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            Toast.makeText(requireContext(), "Please enter a search query.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Executes the ingredient search by making an API call.
     * Manages UI state during the search (showing/hiding progress, disabling/enabling interaction).
     */
    private fun performSearch(query: String) {
        // 1. Hide the soft keyboard if it's open
        hideKeyboard(searchEditText)

        // 2. Explicitly disable the search input bar's interaction capabilities
        //    without changing its visual 'enabled' state.
        searchEditText.isFocusable = false
        searchEditText.isFocusableInTouchMode = false
        searchEditText.isClickable = false
        searchEditText.inputType = InputType.TYPE_NULL // Prevents keyboard from popping up

        // 3. Show the transparent full-screen progress bar.
        //    This view's clickable=true and focusable=true will intercept all touches
        //    on the rest of the screen.
        progressBar.visibility = View.VISIBLE
        Log.d("SearchFragment", "ProgressBar shown, UI interactions disabled for input bar and screen.")


        // Launch a coroutine for the network operation
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Construct the JSON request body
                val jsonString = """{"ingredient_name":"$query"}"""
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())

                // Perform the network call on the IO dispatcher
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getIngredientRaw(requestBody)
                }

                if (response.isSuccessful) {
                    val rawResponseBody = response.body()?.string()
                    if (rawResponseBody != null) {
                        try {
                            // Parse the initial response to check status and get raw analysis
                            val ingredientSearchResponse = gson.fromJson(rawResponseBody, IngredientSearchResponse::class.java)

                            if (ingredientSearchResponse.status == "success" && ingredientSearchResponse.analysisResultRaw.isNotEmpty()) {
                                // Parse the nested analysisResultRaw string into an AnalysisResult object
                                val analysisResult = gson.fromJson(ingredientSearchResponse.analysisResultRaw, AnalysisResult::class.java)

                                Log.d("API_RESULT", "Parsed and Passing: Name=${analysisResult.name}, Use=${analysisResult.use}")
                                // Load the SearchResultFragment with detailed results
                                loadSearchResultFragment(ingredientSearchResponse.ingredient, analysisResult)
                            } else {
                                // Case: API call successful, but no valid analysis data or status isn't "success"
                                val message = "API call successful, but no analysis data found or status not 'success'."
                                Log.d("API_RESULT", message)
                                loadSearchResultFragment(message) // Pass message for display
                            }
                        } catch (e: Exception) {
                            // Handle JSON parsing errors
                            val message = "Failed to parse API response: ${e.localizedMessage ?: "Unknown parsing error."}"
                            Log.e("API_ERROR", "JSON Parsing Exception: $message", e)
                            loadSearchResultFragment(message) // Pass error message
                        }
                    } else {
                        // Case: Empty response body
                        val message = "Empty response body received for '$query'."
                        Log.d("API_RESULT", message)
                        loadSearchResultFragment(message) // Pass message for display
                    }
                } else {
                    // Handle unsuccessful HTTP responses (e.g., 404, 500)
                    val errorText = response.errorBody()?.string()
                    val message = "Error searching for '$query': ${response.code()} - ${errorText ?: "Unknown error"}"
                    Log.e("API_ERROR", "API Error: $message")
                    loadSearchResultFragment(message) // Pass error message
                }

            } catch (e: Exception) {
                // Handle general exceptions (e.g., network issues, timeouts)
                val errorMessage = "Exception searching for '$query': ${e.localizedMessage ?: "An unknown error occurred."}"
                Log.e("API_ERROR", "Exception during API call: $errorMessage", e)
                loadSearchResultFragment(errorMessage) // Pass error message
            } finally {
                // This block always executes, regardless of success or failure.
                // 4. Hide the progress bar
                progressBar.visibility = View.GONE

                // 5. Re-enable the search input bar's interaction capabilities
                searchEditText.isFocusable = true
                searchEditText.isFocusableInTouchMode = true
                searchEditText.isClickable = true
                searchEditText.inputType = originalSearchInputType // Restore original input type
            }
        }
    }

    /**
     * Loads the SearchResultFragment with an error message.
     */
    private fun loadSearchResultFragment(errorMessage: String) {
        val searchResultFragment = SearchResultFragment.newInstance(errorMessage)
        parentFragmentManager.beginTransaction()
            .replace(FRAGMENT_CONTAINER_ID, searchResultFragment)
            .addToBackStack(null) // Allows returning to SearchFragment
            .commit()
    }

    /**
     * Loads the SearchResultFragment with successful ingredient and analysis data.
     */
    private fun loadSearchResultFragment(ingredientName: String, analysisResult: AnalysisResult) {
        val searchResultFragment = SearchResultFragment.newInstance(ingredientName, analysisResult)
        parentFragmentManager.beginTransaction()
            .replace(FRAGMENT_CONTAINER_ID, searchResultFragment)
            .addToBackStack(null) // Allows returning to SearchFragment
            .commit()
    }

    /**
     * Hides the soft keyboard.
     */
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}