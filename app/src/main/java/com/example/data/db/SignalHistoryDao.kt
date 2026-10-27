package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SignalHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalHistoryDao {
    @Query("SELECT * FROM signal_history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<SignalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signal: SignalHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signals: List<SignalHistoryEntity>)

    @Query("UPDATE signal_history SET outcome = :outcome WHERE id = :id")
    suspend fun updateOutcome(id: String, outcome: String)

    @Query("DELETE FROM signal_history")
    suspend fun clearAll()
}
