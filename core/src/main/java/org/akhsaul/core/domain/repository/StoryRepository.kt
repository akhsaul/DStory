package org.akhsaul.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.Story
import java.io.File

interface StoryRepository {
    fun addStory(
        photoFile: File,
        description: String,
        lat: Double,
        lon: Double
    ): Flow<Result<String>>

    fun getAllStory(
        page: Int? = null,
        size: Int? = null,
        location: Int = 0
    ): Flow<Result<List<Story>>>

    fun getAllStoryWithPaging(pageSize: Int = 10): Flow<PagingData<Story>>
}