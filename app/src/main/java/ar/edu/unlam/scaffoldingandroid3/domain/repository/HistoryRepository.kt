package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.History

/**
 * TODO: Puerto de salida - Gestión del historial de actividades
 * Define métodos para obtener, filtrar y eliminar actividades completadas
 */

interface HistoryRepository {
    suspend fun getHistory(): List<History>

    suspend fun saveCompletedActivity(history: History)

    suspend fun deleteHistory(historyId: Long)
}
