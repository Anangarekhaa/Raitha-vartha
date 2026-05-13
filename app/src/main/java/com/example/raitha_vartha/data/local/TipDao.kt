package com.example.raitha_vartha.data.local

import androidx.room.*
import com.example.raitha_vartha.model.Tip
import com.example.raitha_vartha.model.UserBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {

    @Query("""
        SELECT tips.*, (user_bookmarks.tipId IS NOT NULL) AS isBookmarked 
        FROM tips 
        LEFT JOIN user_bookmarks ON tips.id = user_bookmarks.tipId AND user_bookmarks.userPhone = :userPhone
        WHERE (:cropType IS NULL OR tips.cropType = :cropType)
        AND tips.language = :language
        AND tips.isSuccessStory = :onlySuccessStories
    """)
    fun getTipsWithBookmarks(
        cropType: String?, 
        language: String, 
        userPhone: String?,
        onlySuccessStories: Int = 0
    ): Flow<List<Tip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<Tip>)

    @Query("SELECT COUNT(*) FROM tips")
    suspend fun getCount(): Int

    @Query("SELECT MAX(id) FROM tips")
    suspend fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: UserBookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: UserBookmark)
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_bookmarks WHERE tipId = :tipId AND userPhone = :userPhone)")
    suspend fun isBookmarked(tipId: Int, userPhone: String): Boolean

    @Update
    suspend fun updateTip(tip: Tip)
}
