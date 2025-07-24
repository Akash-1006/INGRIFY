package com.ingrify.app

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    // Example login endpoint (adjust to your actual endpoint)
    @POST("login") // Replace with your actual login endpoint path
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>


    @POST("signup") // Replace with your actual signup endpoint path
    suspend fun registerUser(@Body request: RegisterRequest): Response<LoginResponse> // Assuming signup also returns a token

    @POST("single_ingredient")
    suspend fun getIngredientRaw(@Body body: RequestBody): Response<ResponseBody>
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

data class IngredientResponse(
    val name: String,
    val uses: String,
    val sideEffects: String,
    val isAllergen: Boolean
)

