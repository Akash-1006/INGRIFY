package com.ingrify.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class ScanResultFragment : Fragment() {

    private lateinit var tvOcrResultContent: TextView
    private lateinit var backButton: ImageView
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var allergenListLayout: LinearLayout
    private lateinit var allergenTitle: TextView
    private lateinit var expandAllergenToggle: ImageView
    private lateinit var allergenFound: MaterialCardView
    private lateinit var allergenWarning: TextView

    companion object {
        // Use a clear key for the Parcelable object
        private const val ARG_OCR_RESPONSE = "ocrResponseData"
        private const val ARG_ERROR_MESSAGE = "errorMessage"

        fun newInstance(ocrResponse: OcrResponse?, errorMessage: String?): ScanResultFragment {
            val fragment = ScanResultFragment()
            val args = Bundle().apply {
                putParcelable(ARG_OCR_RESPONSE, ocrResponse)
                putString(ARG_ERROR_MESSAGE, errorMessage)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_result, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ocrcard= view.findViewById<ConstraintLayout>(R.id.OCR_menu)
        val menuocr= view.findViewById<MaterialCardView>(R.id.card_ocr_results)
        tvOcrResultContent = view.findViewById(R.id.OCR_result)
        backButton = view.findViewById(R.id.iv_back_button_scan_result)
        ingredientsRecyclerView = view.findViewById(R.id.rv_ingredients)
        val expandToggleIcon = view.findViewById<ImageView>(R.id.iv_expand_toggle)
        val collapsibleDetailsLayout = view.findViewById<View>(R.id.cl_collapsible_details)
        val indicator = view.findViewById<View>(R.id.indicator)
        val hazardBar = view.findViewById<View>(R.id.hazardBar)
        allergenListLayout = view.findViewById(R.id.allergenListLayout)
        allergenTitle = view.findViewById(R.id.allergenTitle)
        expandAllergenToggle = view.findViewById(R.id.iv_expand_allergen)
        allergenFound= view.findViewById(R.id.card_allergens)
        allergenWarning= view.findViewById(R.id.allergenWarningText)

        ingredientAdapter = IngredientAdapter()
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(context)
        ingredientsRecyclerView.adapter = ingredientAdapter

        // CORRECTED: Retrieve the Parcelable object directly
        val ocrResponse = arguments?.getParcelable<OcrResponse>(ARG_OCR_RESPONSE)
        val errorMessage = arguments?.getString(ARG_ERROR_MESSAGE)

        var isExpanded = false
        var allergensExpanded = false


        if (ocrResponse != null) {
            Log.d("ScanResultFragment", "Received OcrResponse object.")
            val ingredientList = ocrResponse.analysisResult
            Log.d("ScanResultFragment", "Number of ingredients received: ${ingredientList.size}")

            ocrcard.visibility=View.VISIBLE
            menuocr.setOnClickListener {
                isExpanded = !isExpanded
                collapsibleDetailsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                expandToggleIcon.setImageResource(
                    if (isExpanded) R.drawable.ic_drop_down else R.drawable.ic_right
                )
                if (isExpanded) {
                    tvOcrResultContent.text = ocrResponse.ocrResult
                }
            }
            val safetyScore = ocrResponse.overallSafety?.score ?: -1

            val hazardScore = getHazardScoreFromAnalysis(safetyScore)

            hazardBar.post {
                val barWidth = hazardBar.width - indicator.width
                val translationX = hazardScore * barWidth
                indicator.translationX = translationX
            }
            val savedAllergens = UserSessionManager.getAllergens()
            val matchedAllergens = ingredientList.filter { ingredient ->
                savedAllergens.any { saved ->
                    saved.trim().equals(ingredient.name.trim(), ignoreCase = true)
                }
            }.map { it.name.trim() }

            if (matchedAllergens.isNotEmpty()) {
                allergenWarning.visibility = View.VISIBLE
                Log.d("AllergenCheck", "Matched Allergens: $matchedAllergens")
                allergenWarning.text = "⚠ Warning: This product contains ${matchedAllergens.size} of your saved allergens."
                allergenListLayout.removeAllViews()
                matchedAllergens.forEach { allergen ->
                    val tv = TextView(requireContext()).apply {
                        text = "• $allergen"
                        setTextColor(resources.getColor(R.color.black, null))
                        textSize = 14f
                        typeface = resources.getFont(R.font.poppins_regular)
                    }
                    allergenListLayout.addView(tv)
                }
            } else {
                allergenFound.visibility=View.GONE
                allergenListLayout.visibility = View.GONE
            }

            // Expand/Collapse Allergens card
            view.findViewById<MaterialCardView>(R.id.card_allergens).setOnClickListener {
                allergensExpanded = !allergensExpanded
                allergenListLayout.visibility = if (allergensExpanded) View.VISIBLE else View.GONE
                expandAllergenToggle.setImageResource(
                    if (allergensExpanded) R.drawable.ic_drop_down else R.drawable.ic_right
                )
            }

            // Update the adapter with the list of ingredients
            ingredientAdapter.updateData(ocrResponse.analysisResult)
        } else if (errorMessage != null) {
            Toast.makeText(requireContext(), "Please Check your Internet Connection and try again.", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            ingredientsRecyclerView.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "Server Error", Toast.LENGTH_SHORT).show()
            tvOcrResultContent.text = "No API response data available."
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    fun getHazardScoreFromAnalysis(safetyScore: Int): Float {
        val safe = safetyScore.coerceIn(0, 100)
        return safe / 100f
    }
}