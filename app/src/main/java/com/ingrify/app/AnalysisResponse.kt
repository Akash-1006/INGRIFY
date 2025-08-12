package com.ingrify.app
import android.view.inputmethod.ExtractedText
import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.ingrify.app.Ingredient

/**
 * Represents the inner JSON object found within the 'analysis_result' field.
 * This is the detailed information about the ingredient.
 */
data class AnalysisResult(
    @SerializedName("name") val name: String,
    @SerializedName("use") val use: String,
    @SerializedName("Made from") val madeFrom: String, // Use @SerializedName for keys with spaces
    @SerializedName("side_effects") val sideEffects: String,
    @SerializedName("allergen") val allergen: Boolean
)

/**
 * Represents the top-level JSON response from your API call.
 * Note that 'analysis_result' is initially parsed as a String because it contains
 * another JSON object that needs to be parsed separately.
 */
@Parcelize
data class IngredientSearchResponse(
    @SerializedName("analysis_result") val analysisResultRaw: String, // This is a String containing JSON
    @SerializedName("ingredient") val ingredient: String,
    @SerializedName("saved") val saved: Boolean,
    @SerializedName("status") val status: String
) : Parcelable

@Parcelize
data class OcrResponse(
    val status: String,
    @SerializedName("ocr_result")
    val ocrResult: String,
    @SerializedName("analysis_result")
    val analysisResult: List<Ingredient>,
    @SerializedName("image_filename")
    val imageFilename: String,
    @SerializedName("search_reference")
    val searchReference: String
) : Parcelable