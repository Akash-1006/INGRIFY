package com.ingrify.app

import com.google.gson.annotations.SerializedName

// This data class should match the structure of your user profile API response
data class UserProfileResponse(
    @SerializedName("name") // Use @SerializedName if your JSON key is different from your variable name
    val name: String,
    @SerializedName("age") // Use @SerializedName if your JSON key is different from your variable name
    val age: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
