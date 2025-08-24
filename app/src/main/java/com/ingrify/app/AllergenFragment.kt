package com.ingrify.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllergenFragment : Fragment() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var tilSearchInput: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var allergenAdapter: AllergenAdapter
    private val allergenList = mutableListOf<Allergen>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val token = UserSessionManager.getAuthToken()

        return if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please login to manage allergens.", Toast.LENGTH_LONG).show()

            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)

            requireActivity().finish()
            null
        } else {
            inflater.inflate(R.layout.fragment_allergen, container, false)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilSearchInput = view.findViewById(R.id.til_search_input_custom)
        searchEditText = view.findViewById(R.id.et_search_query_custom)
        progressBar = view.findViewById(R.id.progress_bar)
        searchEditText.isClickable = true
        searchEditText.isFocusable = true
        searchEditText.isFocusableInTouchMode = true

        recyclerView = view.findViewById(R.id.AllergensRecyclerView)

        // Setup RecyclerView
        allergenAdapter = AllergenAdapter(allergenList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = allergenAdapter

        fetchAllergens()

        val backButton: ImageView = view.findViewById(R.id.iv_back_button_allergen)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }




        // Send allergen when clicking end icon
        tilSearchInput.setEndIconOnClickListener {
            handleAddAllergen()
            tilSearchInput.editText?.text?.clear()
        }

        // Send allergen when pressing "done"/"enter"
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                handleAddAllergen()
                true
            } else false
        }
    }

    private fun handleAddAllergen() {
        if (progressBar.isVisible) {
            Log.d("AllergenFragment", "Request already in progress, ignoring new action.")
            return
        }

        val allergen = searchEditText.text.toString().trim()
        if (allergen.isNotEmpty()) {
            addAllergenToServer(allergen)
        } else {
            Toast.makeText(requireContext(), "Please enter an allergen.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addAllergenToServer(allergen: String) {
        hideKeyboard(searchEditText)
        progressBar.visibility = View.VISIBLE

        // Disable input while request is ongoing
        searchEditText.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = UserSessionManager.getAuthToken() // fetch stored JWT

                if (token.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please login to manage allergens.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val request = AddAllergensRequest(allergens = listOf(allergen))

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.addAllergens("Bearer ${token}", request)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if(body.allergens_added==0){
                            Toast.makeText(requireContext(),"Allergen already exists",Toast.LENGTH_LONG).show()
                        }
                        Toast.makeText(requireContext(),"Allergen saved successfully",Toast.LENGTH_LONG).show()
                        fetchAllergens()
                        Log.d("AllergenFragment", "Added ${body.allergens_added} allergens")
                    } else {
                        Toast.makeText(requireContext(), "Empty response from server.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Error: ${response.code()} - $error",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("AllergenFragment", "API Error: $error")
                }

            } catch (e: Exception) {
                Log.e("AllergenFragment", "Exception: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "Request failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                searchEditText.isEnabled = true
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun fetchAllergens() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val token = "Bearer " + UserSessionManager.getAuthToken()
                val response = RetrofitClient.apiService.getAllergens(token)

                if (response.isSuccessful) {
                    val allergenStrings = response.body()?.allergens ?: emptyList()
                    val allergens = allergenStrings.map { Allergen(it) }

                    allergenList.clear()
                    allergenList.addAll(allergens)
                    Log.d("AllergenFragment", "Allergen count: ${allergenList.size}")
                    allergenAdapter.notifyDataSetChanged()
                    UserSessionManager.saveAllergens(allergenStrings)
                } else {
                    Toast.makeText(requireContext(), "Failed to load allergens", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
