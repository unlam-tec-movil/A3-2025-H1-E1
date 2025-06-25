package ar.edu.unlam.scaffoldingandroid3.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.HistoryEntity

/**
 * DAO Room - Operaciones para historial de actividades
 * @Dao, métodos: @Insert, @Delete, @Query getAllHistory, getHistoryByDateRange
 * Flow<List<HistoryEntity>> para mostrar historial reactivo
 */

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY date DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getHistoryByDateRange(
        startDate: String,
        endDate: String,
    ): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :historyId")
    suspend fun deleteHistory(historyId: Long)
}
