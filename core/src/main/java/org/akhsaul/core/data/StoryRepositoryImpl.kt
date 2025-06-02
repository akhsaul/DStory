package org.akhsaul.core.data

import android.graphics.Bitmap
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.akhsaul.core.data.source.remote.network.ApiService
import org.akhsaul.core.domain.model.Story
import org.akhsaul.core.domain.repository.StoryRepository
import org.akhsaul.core.util.PlainText
import org.akhsaul.core.util.catchNoNetwork
import org.akhsaul.core.util.getErrorResponse
import org.akhsaul.core.util.reduceFileImage
import org.akhsaul.core.util.toMultiPartBody
import org.akhsaul.core.util.toRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class StoryRepositoryImpl : StoryRepository, KoinComponent {
    private val apiService: ApiService by inject()
    private val gson: Gson by inject()

    override fun addStory(
        photoFile: File,
        description: String,
        lat: Double,
        lon: Double
    ): Flow<Result<String>> = flow {
        val finalPhoto: File
        val photoType = when (photoFile.extension.lowercase()) {
            "png" -> {
                finalPhoto = photoFile.reduceFileImage(Bitmap.CompressFormat.PNG)
                "image/png"
            }

            "jpg", "jpeg" -> {
                finalPhoto = photoFile.reduceFileImage(Bitmap.CompressFormat.JPEG)
                "image/jpeg"
            }

            else -> throw IllegalArgumentException("Invalid image type")
        }.toMediaTypeOrNull()
        emit(finalPhoto to photoType)
    }.map { (photo, photoType) ->
        val photoPart = photo.toMultiPartBody("photo", photoType)
        val descriptionPart = description.toRequestBody(MediaType.PlainText)
        val latPart = lat.toRequestBody(MediaType.PlainText)
        val lonPart = lon.toRequestBody(MediaType.PlainText)

        val apiResult = apiService.addStory(photoPart, descriptionPart, latPart, lonPart)
        if (apiResult.isSuccessful) {
            Result.Success(apiResult.body()?.message ?: "Response is null")
        } else {
            val errorResponse = apiResult.getErrorResponse(gson)
            Result.Error(errorResponse?.message ?: apiResult.message())
        }
    }.catchNoNetwork().onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)

    override fun getAllStory(
        page: Int?,
        size: Int?, location: Int
    ): Flow<Result<List<Story>>> = flow {
        val apiResult = apiService.getAllStory(page, size, location)
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
            val errorResponse = apiResult.getErrorResponse(gson)
            emit(Result.Error(errorResponse?.message ?: apiResult.message()))
        }
    }.catchNoNetwork().onStart {
        emit(Result.Loading)
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalPagingApi::class)
    override fun getAllStoryWithPaging(pageSize: Int): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            // mediator if want use database
            // remoteMediator = StoryRemoteMediator(apiService),
            pagingSourceFactory = { StoryPagingSource(apiService, gson) }
        ).flow
    }
}
