package org.akhsaul.core

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.exifinterface.media.ExifInterface
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.UnknownHostException
import java.util.Locale

val MediaType.Companion.PlainText: MediaType?
    get() = "text/plain".toMediaTypeOrNull()

fun Double.toRequestBody(mediaType: MediaType?) = this.toString().toRequestBody(mediaType)

fun File.toMultiPartBody(
    name: String, mediaType: MediaType?
) = MultipartBody.Part.createFormData(
    name, this.name, asRequestBody(mediaType)
)

inline fun <reified T> Response<T>.getErrorResponse(gson: Gson): T? {
    return runCatching {
        val stream = this.errorBody()?.charStream() ?: return null
        gson.fromJson(stream, T::class.java)
    }.getOrNull()
}

/**
 * Convert [UnknownHostException] into [Result.Error] with message `No network available`.
 *
 * if there's another Exception, then [otherAction] will be invoked.
 * */
fun <T> Flow<Result<T>>.catchNoNetwork(
    otherAction: suspend FlowCollector<Result<T>>.(cause: Throwable) -> Unit = {}
): Flow<Result<T>> = this.catch {
    if (it is UnknownHostException) {
        emit(Result.Error("No network available"))
    } else {
        otherAction.invoke(this, it)
    }
}

fun Uri.toFile(context: Context): File {
    val tmpFile = File.createTempFile("d_story_", ".jpg", context.externalCacheDir)
    context.contentResolver.openInputStream(this)?.use { inStream ->
        tmpFile.outputStream().use { outStream ->
            val buf = ByteArray(1024)
            var n: Int
            while (inStream.read(buf).also { n = it } != -1) {
                outStream.write(buf, 0, n)
            }
        }
    }
    return tmpFile
}

fun File.reduceFileImage(format: Bitmap.CompressFormat, maxSize: Int = (1024 * 1024)): File {
    val file = this
    if (file.length() <= maxSize) {
        return file
    }

    val bitmap = BitmapFactory.decodeFile(file.path)?.getRotatedBitmap(file)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap?.compress(format, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > maxSize)
    bitmap?.compress(format, compressQuality, FileOutputStream(file))
    return file
}

fun Bitmap.getRotatedBitmap(file: File): Bitmap {
    val orientation = ExifInterface(file).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
    )
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270F)
        ExifInterface.ORIENTATION_NORMAL -> this
        else -> this
    }
}

fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height, matrix, true
    )
}

fun setAppDarkMode(isDark: Boolean) {
    val compatDelegate = if (isDark) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_NO
    }
    AppCompatDelegate.setDefaultNightMode(compatDelegate)
}

fun applyAppLanguage(languageCode: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
}

fun isSystemInDarkMode(resources: Resources): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> false
    }
}

fun getSupportedSystemLocale(
    resources: Resources,
    supportedLocales: Array<String>,
    defaultLocale: Locale = Locale("en")
): Locale {
    return resources.configuration.locales.getFirstMatch(supportedLocales) ?: defaultLocale
}