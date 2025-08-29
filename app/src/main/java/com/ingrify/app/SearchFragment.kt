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
import android.widget.TextView
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchFragment : Fragment() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var tilSearchInput: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private val searchList = mutableListOf<SearchItem>()
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchtext:TextView



    private val FRAGMENT_CONTAINER_ID = R.id.fragment_container

    private val gson = Gson()

    // Store the original input type to restore it later
    private var originalSearchInputType: Int = InputType.TYPE_CLASS_TEXT // Default to text

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using findViewById
        tilSearchInput = view.findViewById(R.id.til_search_input_custom)
        searchEditText = view.findViewById(R.id.et_search_query_custom)
        progressBar = view.findViewById(R.id.progress_bar)
        searchtext=view.findViewById(R.id.tv_recent_search_title)

        // Store the original input type for the searchEditText
        originalSearchInputType = searchEditText.inputType


        searchEditText.isClickable = true
        searchEditText.isFocusable = true
        searchEditText.isFocusableInTouchMode = true

        recyclerView = view.findViewById(R.id.SearchRecyclerView)

        searchAdapter = SearchAdapter(searchList) { searchItem ->
            try {
                val analysis = gson.fromJson(searchItem.analysis, AnalysisResult::class.java)
                val fragment = SearchResultFragment.newInstance(searchItem.query, analysis)
                parentFragmentManager.beginTransaction()
                    .replace(FRAGMENT_CONTAINER_ID, fragment)
                    .addToBackStack(null)
                    .commit()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load analysis", Toast.LENGTH_SHORT).show()
                Log.e("SearchFragment", "Error parsing analysis JSON", e)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = searchAdapter
        fetchSearchHistory()


        // Set the click listener for the search icon (end icon) in the TextInputLayout
        tilSearchInput.setEndIconOnClickListener {
            handleSearchAction()
            tilSearchInput.editText?.text?.clear()
        }


        // Set the editor action listener for the TextInputEditText (for keyboard "Search" or "Done" button)
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                handleSearchAction()
                tilSearchInput.editText?.text?.clear()
                true // Consume the event
            } else {
                false // Do not consume the event
            }
        }
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_search)
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

        // 2. Disable the search input bar interaction
        searchEditText.isFocusable = false
        searchEditText.isFocusableInTouchMode = false
        searchEditText.isClickable = false
        searchEditText.inputType = InputType.TYPE_NULL

        // 3. Show progress bar
        progressBar.visibility = View.VISIBLE
        Log.d("SearchFragment", "ProgressBar shown, UI interactions disabled.")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jsonString = """{"ingredient_name":"$query"}"""
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())

                val token = UserSessionManager.getAuthToken()

                val response = withContext(Dispatchers.IO) {
                    if (!token.isNullOrEmpty()) {
                        Log.d("API_CALL", "Calling API with JWT token")
                        RetrofitClient.apiService.getSingleIngredient("Bearer $token", requestBody)
                    } else {
                        Log.d("API_CALL", "Calling API without JWT token")
                        RetrofitClient.apiService.getIngredientRaw(requestBody)
                    }
                }

                if (response.isSuccessful) {
                    val rawResponseBody = response.body()?.string()
                    if (rawResponseBody != null) {
                        try {
                            val ingredientSearchResponse = gson.fromJson(rawResponseBody, IngredientSearchResponse::class.java)

                            if (ingredientSearchResponse.status == "success" && ingredientSearchResponse.analysisResultRaw.isNotEmpty()) {
                                val analysisResult = gson.fromJson(ingredientSearchResponse.analysisResultRaw, AnalysisResult::class.java)
                                Log.d("API_RESULT", "Parsed and Passing: Name=${analysisResult.name}, Use=${analysisResult.use}")
                                loadSearchResultFragment(ingredientSearchResponse.ingredient, analysisResult)
                            } else {
                                val message = "API call successful, but no analysis data found or status not 'success'."
                                Log.d("API_RESULT", message)
                                loadSearchResultFragment(message)
                            }
                        } catch (e: Exception) {
                            val message = "Failed to parse API response: ${e.localizedMessage ?: "Unknown parsing error."}"
                            Log.e("API_ERROR", message, e)
                            loadSearchResultFragment(message)
                        }
                    } else {
                        val message = "Empty response body received for '$query'."
                        Log.d("API_RESULT", message)
                        loadSearchResultFragment(message)
                    }
                } else {
                    val errorText = response.errorBody()?.string()
                    val message = "Error searching for '$query': ${response.code()} - ${errorText ?: "Unknown error"}"
                    Log.e("API_ERROR", message)
                    loadSearchResultFragment(message)
                }
            } catch (e: Exception) {
                val errorMessage = "Exception searching for '$query': ${e.localizedMessage ?: "An unknown error occurred."}"
                Log.e("API_ERROR", errorMessage, e)
                loadSearchResultFragment(errorMessage)
            } finally {
                progressBar.visibility = View.GONE
                searchEditText.isFocusable = true
                searchEditText.isFocusableInTouchMode = true
                searchEditText.isClickable = true
                searchEditText.inputType = originalSearchInputType
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
    private fun fetchSearchHistory() {
        // Show the progress bar while fetching data
        progressBar.visibility = View.VISIBLE

        // Hide the search text and RecyclerView initially
        searchtext.visibility = View.GONE
        recyclerView.visibility = View.GONE // Assuming you have a reference to your RecyclerView here

        lifecycleScope.launch {
            try {
                val token = UserSessionManager.getAuthToken()

                if (token.isNullOrEmpty()) {
                    // User is not logged in, hide everything and show a Toast
                    Toast.makeText(requireContext(), "Login to save recent history", Toast.LENGTH_LONG).show()
                } else {
                    val response = RetrofitClient.apiService.getSearch("Bearer $token")
                    if (response.isSuccessful) {
                        val searchItems = response.body()?.data ?: emptyList()
                        searchList.clear()
                        searchList.addAll(searchItems)
                        Log.d("SearchHistoryFragment", "Search history count: ${searchList.size}")
                        searchAdapter.notifyDataSetChanged()

                        // Check if the list is empty and set visibility accordingly
                        if (searchList.isEmpty()) {
                            searchtext.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        } else {
                            searchtext.visibility = View.VISIBLE
                            recyclerView.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load search history", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // Always hide the progress bar when the process is complete
                progressBar.visibility = View.GONE
            }
        }
    }

}