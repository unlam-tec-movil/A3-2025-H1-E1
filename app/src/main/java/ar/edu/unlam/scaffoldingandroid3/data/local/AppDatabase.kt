package ar.edu.unlam.scaffoldingandroid3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ar.edu.unlam.scaffoldingandroid3.data.local.converters.RouteConverters
import ar.edu.unlam.scaffoldingandroid3.data.local.dao.RouteDao
import ar.edu.unlam.scaffoldingandroid3.data.local.entity.RouteEntity

/**
 * Database Room - Configuración principal de la base de datos
 * @Database con todas las entities, @TypeConverters, version = 1
 * Abstract methods para todos los DAOs: routeDao(), photoDao(), etc.
 * Singleton pattern con Room.databaseBuilder
 */

@Database(entities = [RouteEntity::class], version = 1, exportSchema = false)
@TypeConverters(RouteConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}
