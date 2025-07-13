package com.ingrify.app

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    // Example login endpoint (adjust to your actual endpoint)
    @POST("api/login") // Replace with your actual login endpoint path
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>


    @POST("api/signup") // Replace with your actual signup endpoint path
    suspend fun registerUser(@Body request: RegisterRequest): Response<LoginResponse> // Assuming signup also returns a token

    // Endpoint to get user profile, requires an Authorization header
    @GET("api/user/profile") // Replace with your actual user profile endpoint path
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfileResponse>

  //  @GET("search_suggestions")
    //suspend fun getSearchSuggestions(@Header("Authorization") authToken: String, @Query("q") query: String): Response<SuggestionsResponse>

   // @GET("allergen_suggestions")
   // suspend fun getAllergenSuggestions(@Header("Authorization") authToken: String, @Query("q") query: String): Response<SuggestionsResponse>
}
data class SuggestionsResponse(
    val suggestions: List<String>
)
