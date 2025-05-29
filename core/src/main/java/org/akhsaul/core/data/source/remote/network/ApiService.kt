package org.akhsaul.core.data.source.remote.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.akhsaul.core.data.source.remote.reponse.AllStoryResponse
import org.akhsaul.core.data.source.remote.reponse.ApiResponse
import org.akhsaul.core.data.source.remote.reponse.LoginResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    @FormUrlEncoded
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ApiResponse>

    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("stories")
    @Multipart
    suspend fun addStory(
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
    ): Response<ApiResponse>

    @GET("stories")
    suspend fun getAllStory(
        @Query("size") size: Int? = null,
    ): Response<AllStoryResponse>
}