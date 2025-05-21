package org.akhsaul.core.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.akhsaul.core.PlainText
import org.akhsaul.core.data.source.remote.network.ApiService
import org.akhsaul.core.domain.model.Story
import org.akhsaul.core.domain.repository.StoryRepository
import org.akhsaul.core.toMultiPartBody
import org.akhsaul.core.toRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class StoryRepositoryImpl : StoryRepository, KoinComponent {
    private val apiService: ApiService by inject()

    private suspend fun File.reduceImageSize(format: Bitmap.CompressFormat): File {
        val maxSize = 1024 * 1024
        val file = this

        if (file.length() <= maxSize) {
            return file
        }

        val qualities = (100 downTo 10 step 3)
        val resultChannel = Channel<Int>(Channel.UNLIMITED)
        val jobs = mutableListOf<Job>()

        for (quality in qualities) {
            withContext(Dispatchers.IO) {
                // limit to 3 jobs
                val semaphore = Semaphore(3)
                val job = launch {
                    semaphore.withPermit {
                        ByteArrayOutputStream().use { out ->
                            val bitmap = BitmapFactory.decodeFile(file.path)
                            bitmap.compress(format, quality, out)
                            val size = out.size()
                            if (size <= maxSize) {
                                resultChannel.trySend(quality)
                            }
                        }
                    }
                }
                jobs.add(job)
            }
        }

        // Wait for the first result, then cancel all jobs
        val foundQuality = resultChannel.receiveCatching().getOrNull() ?: 100
        jobs.forEach { it.cancel() }
        resultChannel.close()
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { out ->
                BitmapFactory.decodeFile(file.path)
                    .compress(format, foundQuality, out)
            }
        }
        return file
    }

    override fun addStory(
        photo: File,
        description: String,
        lat: Double,
        lon: Double
    ): Flow<Result<String>> = flow {
        val finalPhoto: File
        val photoType = when (photo.extension.lowercase()) {
            "png" -> {
                finalPhoto = photo.reduceImageSize(Bitmap.CompressFormat.PNG)
                "image/png"
            }

            "jpg", "jpeg" -> {
                finalPhoto = photo.reduceImageSize(Bitmap.CompressFormat.JPEG)
                "image/jpeg"
            }

            else -> throw IllegalArgumentException("Only accept image file")
        }.toMediaTypeOrNull()
        val photoPart = finalPhoto.toMultiPartBody("photo", photoType)
        val descriptionPart = description.toRequestBody(MediaType.PlainText)
        val latPart = lat.toRequestBody(MediaType.PlainText)
        val lonPart = lon.toRequestBody(MediaType.PlainText)

        val response = apiService.addStory(photoPart, descriptionPart, latPart, lonPart)
        if (response.isSuccessful) {
            emit(Result.Success(response.body()?.message ?: "Response is null"))
        } else {
            emit(Result.Error(response.message()))
        }
    }.catch {
        emit(Result.Error(it.message))
    }.onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)

    override fun getAllStory(): Flow<Result<List<Story>>> = flow {
        val apiResult = apiService.getAllStory()
        if (apiResult.isSuccessful) {
            val listStory = apiResult.body()?.listStory.orEmpty().map {
                Story(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    photoUrl = it.photoUrl,
                    createdAt = it.createdAt,
                    lat = it.lat,
                    lon = it.lon
                )
            }

            emit(Result.Success(listStory))
        } else {
            emit(Result.Error(apiResult.message()))
        }
    }.catch {
        emit(Result.Error(it.message))
    }.onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)

    override fun getDetailStory(id: String) = flow {
        val apiResult = apiService.getDetailStory(id)
        if (apiResult.isSuccessful) {
            val story = apiResult.body()?.story?.let {
                Story(
                    it.id,
                    it.name,
                    it.description,
                    it.photoUrl,
                    it.createdAt,
                    it.lat,
                    it.lon
                )
            }

            if (story == null) {
                emit(Result.Error("Story not found"))
            } else {
                emit(Result.Success(story))
            }
        } else {
            emit(Result.Error(apiResult.message()))
        }
    }.catch {
        emit(Result.Error(it.message))
    }.onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "StoryRepositoryImpl"
    }
}
