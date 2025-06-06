package org.akhsaul.core.data.source.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.akhsaul.core.data.model.entity.StoryEntity

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(storyList: List<StoryEntity>)

    @Query("select * from story")
    fun getAllStory(): PagingSource<Int, StoryEntity>

    @Query("delete from story")
    suspend fun deleteAllStory()
}