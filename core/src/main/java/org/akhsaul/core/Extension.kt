package org.akhsaul.core

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.akhsaul.core.data.Result
import retrofit2.Response
import java.io.File
import java.net.UnknownHostException

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

/**
 * Convert [UnknownHostException] into [Result.Error] with message `No internet available`.
 *
 * if there's another Exception, then [otherAction] will be invoked.
 * */
fun <T> Flow<Result<T>>.catchNoInternet(
    otherAction: suspend FlowCollector<Result<T>>.(cause: Throwable) -> Unit
): Flow<Result<T>> = this.catch {
    if (it is UnknownHostException) {
        emit(Result.Error("No internet available"))
    } else {
        otherAction.invoke(this, it)
    }
}
