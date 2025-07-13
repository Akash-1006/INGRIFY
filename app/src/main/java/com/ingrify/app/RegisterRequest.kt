package com.ingrify.app

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("name") // Assuming your backend takes a 'name' field for registration
    val name: String,
    @SerializedName("age")
    val age: Int, // Assuming age is an integer
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
