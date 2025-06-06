package org.akhsaul.core.data.source.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import org.akhsaul.core.data.model.domain.Story
import org.akhsaul.core.util.DataMapper
import org.akhsaul.core.util.getErrorResponse
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException

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
        return try {
            val page = params.key ?: 1
            val result = apiService.getAllStory(page = page, size = params.loadSize)
            if (result.isSuccessful) {
                val data = result.body()?.listStory.orEmpty().map(DataMapper::responseToDomain)
                LoadResult.Page(
                    data = data,
                    prevKey = if (page == 1) null else page.minus(1),
                    nextKey = if (data.isEmpty()) null else page.plus(1),
                )
            } else {
                val errorMessage = result.getErrorResponse(gson)?.message
                LoadResult.Error(Exception(errorMessage))
            }
        } catch (e: UnknownHostException) {
            LoadResult.Error(Exception("No Network Available", e))
        } catch (e: SSLPeerUnverifiedException) {
            LoadResult.Error(Exception("SSL Error", e))
        } catch (e: Exception) {
            Log.e("PagingSource", "error: $e")
            LoadResult.Error(Exception("Unexpected Error", e))
        }
    }
}