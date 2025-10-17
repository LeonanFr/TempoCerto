package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.CurucaLog
import com.app.tempocerto.data.model.SoureLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

sealed class DataResult<out T> {
    object Loading : DataResult<Nothing>()
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val message: String) : DataResult<Nothing>()
}

@Singleton
class MonitoringRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getLastSoureLog(): Flow<DataResult<SoureLog?>> = flow {
        emit(DataResult.Loading)
        try {
            val apiDto = apiService.getLatestLog("soure")
            emit(DataResult.Success(apiDto?.toSoureLog()))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro desconhecido"))
        }
    }

    fun getSoureLogsForDate(date: LocalDate): Flow<DataResult<List<SoureLog>>> = flow {
        emit(DataResult.Loading)
        try {
            val dtoList = apiService.getLogsForDate("soure", date.format(dateFormatter))
            emit(DataResult.Success(dtoList.map { it.toSoureLog() }))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro desconhecido"))
        }
    }

    fun getSoureLogsForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<DataResult<List<SoureLog>>> = flow {
        emit(DataResult.Loading)
        try {
            val formattedStart = startDate.format(dateFormatter)
            val formattedEnd = endDate.format(dateFormatter)
            val dtoList = apiService.getLogsForDateRange("soure", formattedStart, formattedEnd)
            emit(DataResult.Success(dtoList.map { it.toSoureLog() }))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro desconhecido"))
        }
    }

    fun getLastCurucaLog(): Flow<DataResult<CurucaLog?>> = flow {
        emit(DataResult.Loading)
        try {
            val apiDto = apiService.getLatestLog("curuca")
            emit(DataResult.Success(apiDto?.toCurucaLog()))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro desconhecido"))
        }
    }

    fun getCurucaLogsForDate(date: LocalDate): Flow<DataResult<List<CurucaLog>>> = flow {
        emit(DataResult.Loading)
        try {
            val dtoList = apiService.getLogsForDate("curuca", date.format(dateFormatter))
            emit(DataResult.Success(dtoList.map { it.toCurucaLog() }))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro desconhecido"))
        }
    }

    fun getCurucaLogsForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<DataResult<List<CurucaLog>>> = flow {
        emit(DataResult.Loading)
        try {
            val formattedStart = startDate.format(dateFormatter)
            val formattedEnd = endDate.format(dateFormatter)
            val dtoList = apiService.getLogsForDateRange("curuca", formattedStart, formattedEnd)
            emit(DataResult.Success(dtoList.map { it.toCurucaLog() }))
        } catch (e: Exception) {
            emit(DataResult.Error(e.message ?: "Erro desconhecido"))
        }
    }
}