package org.akhsaul.core.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import org.akhsaul.core.data.model.entity.RemoteKey
import org.akhsaul.core.data.model.entity.StoryEntity
import org.akhsaul.core.data.source.local.AppDatabase
import org.akhsaul.core.data.source.remote.ApiService
import org.akhsaul.core.util.DataMapper
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                remoteKey?.nextKey?.minus(1) ?: 1
            }

            LoadType.PREPEND -> {
                val remoteKey = getRemoteKeyForFirstItem(state)
                remoteKey?.prevKey
                    ?: return MediatorResult.Success(remoteKey != null)
            }

            LoadType.APPEND -> {
                val remoteKey = getRemoteKeyForLastItem(state)
                remoteKey?.nextKey
                    ?: return MediatorResult.Success(remoteKey != null)
            }
        }

        return try {
            //delay(5000)
            val result = apiService.getAllStory(page = page, size = state.config.pageSize)
            val data = result.body()?.listStory.orEmpty().map(DataMapper::responseToEntity)

            appDatabase.withTransaction {
                val prevKey = if (page == 1) null else page.minus(1)
                val nextKey = if (data.isEmpty()) null else page.plus(1)
                val keys = data.map {
                    RemoteKey(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                appDatabase.remoteKeyDao().insertAll(keys)
                appDatabase.storyDao().insertStory(data)
            }

            MediatorResult.Success(endOfPaginationReached = data.isEmpty())
        } catch (e: UnknownHostException) {
            MediatorResult.Error(Exception("No network available", e))
        } catch (e: SSLPeerUnverifiedException) {
            MediatorResult.Error(Exception("SSL error", e))
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            appDatabase.remoteKeyDao().getRemoteKeyById(data.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            appDatabase.remoteKeyDao().getRemoteKeyById(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let {
                appDatabase.remoteKeyDao().getRemoteKeyById(it)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}