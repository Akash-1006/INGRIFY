package com.ingrify.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ingrify.app.AnalysisResult
import com.google.gson.Gson // Import Gson

class SearchResultFragment : Fragment() {

    companion object {
        private const val ARG_ANALYSIS_RESULT_JSON = "analysisResultJson"
        private const val ARG_INGREDIENT_NAME = "ingredientName"

        fun newInstance(ingredientName: String, analysisResult: AnalysisResult): SearchResultFragment {
            val fragment = SearchResultFragment()
            val args = Bundle().apply {
                // Convert AnalysisResult object back to JSON string for passing
                putString(ARG_ANALYSIS_RESULT_JSON, Gson().toJson(analysisResult))
                putString(ARG_INGREDIENT_NAME, ingredientName)
            }
            fragment.arguments = args
            return fragment
        }

        // Overload for error cases where we just pass an error message
        fun newInstance(errorMessage: String): SearchResultFragment {
            val fragment = SearchResultFragment()
            val args = Bundle().apply {
                putString(ARG_ANALYSIS_RESULT_JSON, null) // No analysis result
                putString(ARG_INGREDIENT_NAME, "Error") // Indicate error for title
                putString("errorMessage", errorMessage) // A generic error message key
            }
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var tvSearchQueryDisplay: TextView
    private lateinit var tvUsesContent: TextView
    private lateinit var tvMadeFromContent: TextView
    private lateinit var tvSideEffectsContent: TextView
    private lateinit var tvAllergenContent: TextView
    private lateinit var backButton: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        tvSearchQueryDisplay = view.findViewById(R.id.tv_search_query_display)
        tvUsesContent = view.findViewById(R.id.tv_uses_content)
        tvMadeFromContent = view.findViewById(R.id.tv_made_from_content)
        tvSideEffectsContent = view.findViewById(R.id.tv_side_effects_content)
        tvAllergenContent = view.findViewById(R.id.tv_allergen_content)
        backButton = view.findViewById(R.id.iv_back_button)
        val indicator = view.findViewById<View>(R.id.indicator)
        val hazardBar = view.findViewById<View>(R.id.hazardBar)

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // Go back to the previous fragment
        }

        val errorMessage = arguments?.getString("errorMessage")
        if (errorMessage != null) {
            // Display error message prominently if there was an error
            tvSearchQueryDisplay.text = "Search Error"
            tvUsesContent.text = errorMessage
            // Hide other content views as they are not relevant for an error
            view.findViewById<View>(R.id.card_made_from).visibility = View.GONE
            view.findViewById<View>(R.id.card_side_effects).visibility = View.GONE
            view.findViewById<View>(R.id.card_allergen).visibility = View.GONE
        } else {
            // Retrieve data from arguments
            val ingredientName = arguments?.getString(ARG_INGREDIENT_NAME) ?: "Unknown Ingredient"
            val analysisResultJson = arguments?.getString(ARG_ANALYSIS_RESULT_JSON)

            tvSearchQueryDisplay.text = ingredientName.capitalizeWords() // Display the searched ingredient name

            if (!analysisResultJson.isNullOrEmpty()) {
                try {
                    val analysisResult = Gson().fromJson(analysisResultJson, AnalysisResult::class.java)

                    // Populate TextViews with parsed data
                    tvUsesContent.text = analysisResult.use
                    tvMadeFromContent.text = analysisResult.madeFrom
                    tvSideEffectsContent.text = analysisResult.sideEffects
                    tvAllergenContent.text = if (analysisResult.allergen) "Yes." else "No."
                    val safetyScore = analysisResult.score ?: -1

                    val hazardScore = getHazardScoreFromAnalysis(safetyScore)

                    hazardBar.post {
                        val barWidth = hazardBar.width - indicator.width
                        val translationX = hazardScore * barWidth
                        indicator.translationX = translationX
                    }

                } catch (e: Exception) {
                    // Handle parsing error if the JSON somehow became corrupt during transfer
                    tvSearchQueryDisplay.text = "Data Error"
                    tvUsesContent.text = "Failed to display results. Please try again."
                    view.findViewById<View>(R.id.card_made_from).visibility = View.GONE
                    view.findViewById<View>(R.id.card_side_effects).visibility = View.GONE
                    view.findViewById<View>(R.id.card_allergen).visibility = View.GONE
                }
            } else {
                // Fallback for empty/null analysis data even if no explicit error
                tvSearchQueryDisplay.text = "No Details Found"
                tvUsesContent.text = "The ingredient details could not be retrieved."
                view.findViewById<View>(R.id.card_made_from).visibility = View.GONE
                view.findViewById<View>(R.id.card_side_effects).visibility = View.GONE
                view.findViewById<View>(R.id.card_allergen).visibility = View.GONE
            }
        }

    }

    fun getHazardScoreFromAnalysis(safetyScore: Int): Float {
        // Clamp between 0 and 100 just in case
        val safe = safetyScore.coerceIn(0, 100)
        return safe / 100f  // normalize to 0..1
    }
    // Extension function to capitalize words for display purposes (optional)
    fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}