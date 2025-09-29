package com.app.tempocerto.data.network

import com.app.tempocerto.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import kotlin.random.Random

sealed class DataResult<out T> {
    object Loading : DataResult<Nothing>()
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val message: String) : DataResult<Nothing>()
}

class MonitoringRepository {

    fun getLastCurucaLog(): Flow<DataResult<CurucaLog>> = flow {
        emit(DataResult.Loading)
        delay(300)
        val lastLog = CurucaLog(
            date = "${LocalDate.now()}T12:00:00",
            temperature = Random.nextFloat() * 30,
            salinity = Random.nextFloat() * 35,
            pressure = 1013f
        )
        emit(DataResult.Success(lastLog))
    }

    fun getCurucaLogsForDate(date: LocalDate): Flow<DataResult<List<CurucaLog>>> = flow {
        emit(DataResult.Loading)
        delay(500)
        val logs = (0..23).map { hour ->
            CurucaLog(
                date = "${date}T${hour.toString().padStart(2,'0')}:00:00",
                temperature = Random.nextFloat() * 30,
                salinity = Random.nextFloat() * 35,
                pressure = 1010 + Random.nextFloat() * 10
            )
        }
        emit(DataResult.Success(logs))
    }

    fun getLastSoureLog(): Flow<DataResult<SoureLog>> = flow {
        emit(DataResult.Loading)
        delay(300)
        val lastLog = SoureLog(
            dt = "${LocalDate.now().dayOfMonth}/${LocalDate.now().monthValue}/${LocalDate.now().year} 12:00:00",
            ah = Random.nextFloat() * 100,
            at = Random.nextFloat() * 35,
            ap = 1010f + Random.nextFloat() * 10,
            bv = 12f,
            it = 25f,
            p = Random.nextFloat() * 10,
            wd = Random.nextInt(0, 360),
            wl = Random.nextInt(50, 150),
            wsma = Random.nextFloat() * 15,
            wsme = Random.nextFloat() * 10,
            wsmi = Random.nextFloat() * 5,
            wt = Random.nextFloat() * 30
        )
        emit(DataResult.Success(lastLog))
    }

    fun getSoureLogsForDate(date: LocalDate): Flow<DataResult<List<SoureLog>>> = flow {
        emit(DataResult.Loading)
        delay(500)
        val logs = (0..23).map { hour ->
            SoureLog(
                dt = "${date.dayOfMonth}/${date.monthValue}/${date.year} ${hour}:00:00",
                ah = Random.nextFloat() * 100,
                at = Random.nextFloat() * 35,
                ap = 1010f + Random.nextFloat() * 10,
                bv = 12f,
                it = 25f,
                p = Random.nextFloat() * 10,
                wd = Random.nextInt(0, 360),
                wl = Random.nextInt(50, 150),
                wsma = Random.nextFloat() * 15,
                wsme = Random.nextFloat() * 10,
                wsmi = Random.nextFloat() * 5,
                wt = Random.nextFloat() * 30
            )
        }
        emit(DataResult.Success(logs))
    }
}
