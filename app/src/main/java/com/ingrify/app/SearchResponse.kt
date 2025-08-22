package com.ingrify.app

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val data: List<SearchItem>,
    val status: String,
    val type: String
)

// Each item
data class SearchItem(
    val id: Int,
    val query: String,
    val raw_ocr_text: String?,
    val created_at: String,
    val analysis: String
)

data class Analysis(
    val name: String,
    val use: String,
    @SerializedName("Made from") val madeFrom: String,
    val side_effects: String,
    val allergen: Boolean
)
