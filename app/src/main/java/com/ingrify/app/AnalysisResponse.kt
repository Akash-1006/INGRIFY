package com.ingrify.app

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    @SerializedName("ingredients") val ingredients: List<Ingredient>?,
    @SerializedName("overall_summary") val overallSummary: String?
)

data class Ingredient(
    @SerializedName("name") val name: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("is_allergen") val isAllergen: Boolean?,
    @SerializedName("potential_issues") val potentialIssues: List<String>?
)