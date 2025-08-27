package com.app.tempocerto.data.repository

import com.app.tempocerto.data.model.SoureLog
import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.network.SoureFirebaseService
import com.app.tempocerto.data.network.CurucaFirebaseService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val message: String) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}

@Singleton
class MonitoringRepository @Inject constructor() {
    private val curucaService = CurucaFirebaseService
    private val soureService = SoureFirebaseService

    fun getCurucaLogsForDate(date: LocalDate): Flow<DataResult<List<CurucaLog>>> = callbackFlow {
        trySend(DataResult.Loading)
        curucaService.getCurucaLogByDate(
            targetDate = date,
            onSuccess = { logs -> trySend(DataResult.Success(logs)) },
            onFailure = { errorMsg -> trySend(DataResult.Error(errorMsg ?: "Erro ao buscar dados da Curuca.")) }
        )
        awaitClose { channel.close() }
    }

    fun getLastCurucaLog(): Flow<DataResult<CurucaLog?>> = callbackFlow {
        trySend(DataResult.Loading)
        curucaService.getLastCurucaLog(
            onSuccess = { log -> trySend(DataResult.Success(log)) },
            onFailure = { errorMsg -> trySend(DataResult.Error(errorMsg ?: "Erro ao buscar último dado da Curuca.")) }
        )
        awaitClose { channel.close() }
    }

    fun getSoureLogsForDate(date: LocalDate): Flow<DataResult<List<SoureLog>>> = flow {
        emit(DataResult.Loading)
        try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateString = date.format(dateFormatter)
            val logs = soureService.getSoureLogsForDate(dateString)
            emit(DataResult.Success(logs))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro ao buscar dados de Soure."))
        }
    }

    fun getLastSoureLog(): Flow<DataResult<SoureLog?>> = flow {
        emit(DataResult.Loading)
        try {
            val log = soureService.getLastSoureLog()
            emit(DataResult.Success(log))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro ao buscar último dado de Soure."))
        }
    }
}
