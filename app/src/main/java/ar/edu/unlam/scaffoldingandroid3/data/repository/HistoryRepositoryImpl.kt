package ar.edu.unlam.scaffoldingandroid3.data.repository

import ar.edu.unlam.scaffoldingandroid3.data.local.dao.HistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import ar.edu.unlam.scaffoldingandroid3.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Implementación HistoryRepository - Historial de actividades
 * @Inject constructor(historyDao, mapper)
 * Implementa: getHistory(), saveCompletedActivity(), deleteHistoryItem()
 * Para pantalla "Tu historial de actividad"
 */

class HistoryRepositoryImpl
    @Inject
    constructor(
        private val historyDao: HistoryDao,
    ) : HistoryRepository {
        override suspend fun getHistory(): List<History> {
            return historyDao.getAllHistory().map { historyEntity ->
                historyEntity.toDomain()
            }
        }

        override suspend fun saveCompletedActivity(history: History) {
            val historyEntity = history.toEntity()
            historyDao.insertHistory(historyEntity)
        }

        override suspend fun deleteHistory(historyId: Long) {
            historyDao.deleteHistory(historyId)
        }
    }
