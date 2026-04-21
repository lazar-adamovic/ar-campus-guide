@file:Suppress("SpellCheckingInspection")

package com.example.projekat.data
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PoiApiService {
    @Headers("ngrok-skip-browser-warning: true")
    @GET("POIList")
    suspend fun getAllPois(): Response<List<PoiDto>>
    @GET("GetCategories")
    suspend fun getCategories(): List<CategoryDto>
    @POST("CreatePOI")
    suspend fun createPoi(@Body poi: PoiDto): Response<ResponseBody>
    @PUT("UpdatePOI/{id}")
    suspend fun updatePoi(@Path("id") id: String, @Body poi: PoiDto): Response<Unit>
    @PUT("UpdateCoordinates/{id}")
    suspend fun updatePoiLocation(@Path("id") id: String, @Body poi: PoiDto): Response<Unit>
    @POST("AdminPassword")
    suspend fun verifyAdmin(@Body command: VerifyAdminCommand): Response<AdminResponse>
    @DELETE("Delete/{id}")
    suspend fun deletePoi(@Path("id") id: String): Response<Unit>
    @Headers("Cache-Control: no-cache")
    @GET("GetDescription/{id}")
    suspend fun getPoiDescription(@Path("id") id: String):ResponseBody
    @PUT("UpdateDescription/{id}")
    suspend fun updatePoiDescription(
        @Path("id") id: String,
        @Body content: UpdateDescriptionDto
    ): Response<Unit>
}
object RetrofitInstance {
    private const val BASE_URL = "https://34df-178-222-13-62.ngrok-free.app"

    val api: PoiApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(PoiApiService::class.java)
    }
}
