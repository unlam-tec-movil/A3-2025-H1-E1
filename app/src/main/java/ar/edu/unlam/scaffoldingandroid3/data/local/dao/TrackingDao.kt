package ar.edu.unlam.scaffoldingandroid3.data.local.dao

import androidx.room.*
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.PhotoEntity
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.TrackingSessionEntity

/**
 * DAO para operaciones de base de datos de tracking
 * Compatible con Clean Architecture
 */
@Dao
interface TrackingDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackingSession(session: TrackingSessionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)
    
    @Query("SELECT * FROM tracking_sessions ORDER BY createdAt DESC")
    suspend fun getAllTrackingSessions(): List<TrackingSessionEntity>
    
    @Query("SELECT * FROM tracking_photos WHERE sessionId = :sessionId")
    suspend fun getPhotosForSession(sessionId: Long): List<PhotoEntity>
    
    @Query("SELECT * FROM tracking_sessions WHERE id = :sessionId")
    suspend fun getTrackingSessionById(sessionId: Long): TrackingSessionEntity?
    
    @Update
    suspend fun updateTrackingSession(session: TrackingSessionEntity)
    
    @Query("SELECT * FROM tracking_sessions WHERE endTime = 0 LIMIT 1")
    suspend fun getActiveSession(): TrackingSessionEntity?
    
    @Query("UPDATE tracking_sessions SET endTime = :endTime WHERE id = :id")
    suspend fun updateSessionEndTime(id: Long, endTime: Long)
    
    @Delete
    suspend fun deleteTrackingSession(session: TrackingSessionEntity)
    
    @Query("DELETE FROM tracking_sessions WHERE id = :sessionId")
    suspend fun deleteTrackingSessionById(sessionId: Long)
}