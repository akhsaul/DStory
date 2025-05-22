package org.akhsaul.core

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

val MediaType.Companion.PlainText: MediaType?
    get() = "text/plain".toMediaTypeOrNull()

fun Double.toRequestBody(mediaType: MediaType?) = this.toString().toRequestBody(mediaType)

fun File.toMultiPartBody(
    name: String, mediaType: MediaType?
) = MultipartBody.Part.createFormData(
    name, this.name, asRequestBody(mediaType)
)

inline fun <reified T> Response<T>.getErrorResponse(): T? {
    return runCatching {
        val stream = this.errorBody()?.charStream() ?: return null
        Gson().fromJson(stream, T::class.java)
    }.getOrNull()
}