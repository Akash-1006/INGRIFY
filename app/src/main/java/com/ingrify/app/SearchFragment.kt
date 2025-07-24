package com.ingrify.app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


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

        tilSearchInput = view.findViewById(R.id.til_search_input_custom)
        searchEditText = view.findViewById(R.id.et_search_query_custom)
        searchResultsTextView = view.findViewById(R.id.tv_search_results)


        tilSearchInput.setEndIconOnClickListener {
            handleSearchAction()
        }

        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                handleSearchAction()
                true
            } else {
                false
            }
        }
    }

    private fun handleSearchAction() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            Toast.makeText(requireContext(), "Please enter a search query.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch(query: String) {
        hideKeyboard(searchEditText)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jsonString = """{"ingredient_name":"$query"}"""
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getIngredientRaw(requestBody)
                }

                if (response.isSuccessful) {
                    val resultText = response.body()?.string()
                    Log.d("API_RESULT", resultText ?: "Empty response")
                } else {
                    val errorText = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error: ${response.code()} - $errorText")
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception: ${e.localizedMessage}", e)
            }
        }


    }


    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
