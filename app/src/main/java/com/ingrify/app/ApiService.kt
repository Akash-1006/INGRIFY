package com.ingrify.app

import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {


    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>


    @POST("signup")
    suspend fun registerUser(@Body request: RegisterRequest): Response<LoginResponse> // Assuming signup also returns a token

    @POST("single_ingredient_wo_jwt")
    suspend fun getIngredientRaw(@Body body: RequestBody): Response<ResponseBody>

    @POST("single_ingredient")
    suspend fun getSingleIngredient(
        @Header("Authorization") token: String,
        @Body body: RequestBody
    ): Response<ResponseBody>

    @GET("user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfileResponse>

    @Multipart
    @POST("ocr")
    suspend fun uploadImageForOcr(@Header("Authorization") token: String,@Part image: MultipartBody.Part): Response<OcrResponse>

    @Multipart
    @POST("ocr_wo_jwt")
    suspend fun uploadImageForOcrwojwt(@Part image: MultipartBody.Part): Response<OcrResponse>


    @POST("allergens")
    suspend fun addAllergens(
        @Header("Authorization") token: String,
        @Body request: AddAllergensRequest
    ): Response<AddAllergensResponse>

    @GET("allergens")
    suspend fun getAllergens(
        @Header("Authorization") token: String,
    ): Response<AllergenResponse>

    @HTTP(method = "DELETE", path = "allergens", hasBody = true)
    suspend fun deleteAllergen(
        @Header("Authorization") token: String,
        @Body request: DeleteAllergensRequest
    ): Response<AddAllergensResponse>


    @GET("history/search")
    suspend fun getSearch(
        @Header("Authorization") token: String,
    ): Response<SearchResponse>

    @GET("history/scan")
    suspend fun getScan(
        @Header("Authorization") token: String,
    ): Response<ScanResponse>

    @DELETE("/user/account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String
    ): retrofit2.Response<Void>

    @PUT("/user/change_password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse>


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

data class AddAllergensRequest(
    val allergens: List<String>
)
data class DeleteAllergensRequest(
    val allergen: String
)

// Response
data class AddAllergensResponse(
    val message: String,
    val allergens_added: Int
)

data class ScanResponse(
    val data: List<ScanItem>,
    val status: String,
    val type: String
)

data class ScanItem(
    val id: Int,
    val scan_name: String,
    val raw_ocr_text: String,
    val analysis: String,
    val image_filename: String,
    val created_at: String,
    val overall_safety: OverallSafety?
)
data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

data class ApiResponse(
    val message: String
)



