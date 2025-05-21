package org.akhsaul.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.akhsaul.core.data.Result
import org.akhsaul.core.data.Story
import java.io.File

interface StoryRepository {
    fun addStory(photo: File, description: String, lat: Double, lon: Double): Flow<Result<String>>
    fun getAllStory(): Flow<Result<List<Story>>>
    fun getDetailStory(id: String): Flow<Result<Story>>
}