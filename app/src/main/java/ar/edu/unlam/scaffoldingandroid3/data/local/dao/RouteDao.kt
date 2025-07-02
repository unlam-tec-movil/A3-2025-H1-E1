package ar.edu.unlam.scaffoldingandroid3.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room - Operaciones CRUD para rutas
 * @Dao, métodos: @Insert, @Update, @Delete, @Query getAllRoutes, getRouteById, getFavoriteRoutes
 * Flow<List<RouteEntity>> para observar cambios en tiempo real
 */

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRoute(id: String): RouteEntity?

    @Query("SELECT * FROM routes")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRoute(id: String)
}
