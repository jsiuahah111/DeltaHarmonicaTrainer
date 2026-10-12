package com.delta.harmonica.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY id DESC")
    fun getAll(): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScoreEntity)

    @Delete
    suspend fun delete(entity: ScoreEntity)

    @Query("DELETE FROM scores")
    suspend fun clearAll()
}
