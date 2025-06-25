package ar.edu.unlam.scaffoldingandroid3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.LocationPointEntity

/**
 * DAO Room - Operaciones para puntos GPS
 */
@Dao
interface LocationPointDao {
    /**
     * Inserta múltiples puntos GPS (batch insert para performance)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoints(points: List<LocationPointEntity>)

    /**
     * Obtiene todos los puntos GPS de una sesión ordenados por timestamp
     */
    @Query("SELECT * FROM location_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getLocationPointsBySession(sessionId: Long): List<LocationPointEntity>

    /**
     * Cuenta el número de puntos GPS de una sesión
     */
    @Query("SELECT COUNT(*) FROM location_points WHERE sessionId = :sessionId")
    suspend fun getPointsCountBySession(sessionId: Long): Int

    /**
     * Elimina todos los puntos GPS de una sesión
     */
    @Query("DELETE FROM location_points WHERE sessionId = :sessionId")
    suspend fun deleteLocationPointsBySession(sessionId: Long)
}
