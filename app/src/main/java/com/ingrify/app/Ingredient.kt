package com.ingrify.app

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ingredient(
    val name: String,
    val use: String,

    // Use @SerializedName to map the JSON key "Made from" to the property "madeFrom"
    @SerializedName("Made from")
    val madeFrom: String,

    // Use @SerializedName to map the JSON key "side_effects" to the property "sideEffects"
    @SerializedName("side_effects")
    val sideEffects: String,

    val allergen: Boolean,

    // This property is for the UI state and is not from the API
    var isExpanded: Boolean = false
) : Parcelable