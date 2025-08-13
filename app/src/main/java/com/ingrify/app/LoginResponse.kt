package com.ingrify.app

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") // e.g., "Login successful", "User registered"
    val message: String,
    @SerializedName("token") // This is crucial: the authentication token from your backend
    val token: String,
    @SerializedName("user_name") // Assuming your backend returns the user's name upon login/signup
    val userName: String? = null // It's good practice for the API to return the name immediately
)
