package org.akhsaul.core.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import kotlinx.coroutines.delay
import org.akhsaul.core.data.source.remote.network.ApiService
import org.akhsaul.core.domain.model.Story
import org.akhsaul.core.util.catchNoNetwork
import org.akhsaul.core.util.getErrorResponse

class StoryPagingSource(
    private val apiService: ApiService,
    private val gson: Gson
) : PagingSource<Int, Story>() {
    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return runCatching {
            val page = params.key ?: 1
            Log.i("PagingSource", "fetch network $page")
            val result = apiService.getAllStory(page = page, size = params.loadSize)
            delay(5000)

            if (result.isSuccessful) {
                val data = result.body()?.listStory.orEmpty().map {
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
                LoadResult.Page(
                    data = data,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (data.isEmpty()) null else page + 1,
                )
            } else {
                val errorMessage = result.getErrorResponse(gson)?.message
                LoadResult.Error(Exception(errorMessage))
            }
        }.catchNoNetwork().getOrElse {
            LoadResult.Error(it)
        }.apply {
            Log.i("PagingSource", "getOrElse: $this")
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}