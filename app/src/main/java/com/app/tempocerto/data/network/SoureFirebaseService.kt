package com.app.tempocerto.data.network

import android.util.Log
import com.app.tempocerto.data.model.SoureLog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SoureFirebaseService {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("TEST-04/logs")
    private var cachedLogs: List<SoureLog>? = null

    private suspend fun fetchAndCacheLogs(): List<SoureLog> {
        cachedLogs?.let { return it }

        return try {
            val task = database.get().await()
            val allLogs = mutableListOf<SoureLog>()
            Log.d("SoureFirebaseService", "Número de pushIdSnapshot: ${task.childrenCount}")

            for (pushIdSnapshot in task.children) {
                for (logSnapshot in pushIdSnapshot.children) {
                    logSnapshot.getValue(SoureLog::class.java)?.let {
                        allLogs.add(it)
                    }
                }
            }

            cachedLogs = allLogs
            Log.d("SoureFirebaseService", "Total de logs buscados e cacheados: ${allLogs.size}")
            allLogs
        } catch (e: Exception) {
            Log.e("SoureFirebaseService", "Falha ao buscar logs", e)
            emptyList()
        }
    }

    suspend fun getLastSoureLog(): SoureLog? {
        return try {
            val allLogs = fetchAndCacheLogs()
            val inputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s")
            allLogs.maxByOrNull { log ->
                try { LocalDateTime.parse(log.dt, inputFormatter) } catch (_: Exception) { LocalDateTime.MIN }
            }
        } catch (e: Exception) {
            Log.e("SoureFirebaseService", "Erro ao buscar último log", e)
            null
        }
    }

    suspend fun getLastSoureLogDate(): String? {
        val lastLog = getLastSoureLog() ?: return null
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = LocalDateTime.parse(lastLog.dt, inputFormatter).toLocalDate()
            date.format(outputFormatter)
        } catch (e: Exception) {
            Log.e("SoureFirebaseService", "Erro ao formatar data do último log", e)
            null
        }
    }


    suspend fun getSoureLogsForDate(targetDateStr: String): List<SoureLog> {
        val allLogs = fetchAndCacheLogs()
        val inputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m:s")
        val targetFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return allLogs.filter { log ->
            try {
                val logDate = LocalDateTime.parse(log.dt, inputFormatter).toLocalDate()
                logDate.format(targetFormatter) == targetDateStr
            } catch (_: Exception) {
                false
            }
        }.sortedBy { log ->
            try { LocalDateTime.parse(log.dt, inputFormatter) } catch (_: Exception) { LocalDateTime.MAX }
        }
    }
}
