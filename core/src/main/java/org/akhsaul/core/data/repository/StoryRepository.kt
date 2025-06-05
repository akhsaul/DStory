package org.akhsaul.core.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.akhsaul.core.data.model.domain.Story
import org.akhsaul.core.util.Result
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