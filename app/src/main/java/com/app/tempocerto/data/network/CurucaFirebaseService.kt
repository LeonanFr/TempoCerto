package com.app.tempocerto.data.network

import android.util.Log
import com.app.tempocerto.data.model.CurucaLog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CurucaFirebaseService {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("resex")

    fun getLastCurucaLog(onSuccess: (CurucaLog?) -> Unit, onFailure: (String?) -> Unit) {
        database
            .orderByChild("date")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastLogSnapshot = snapshot.children.firstOrNull()
                        val curucaLog = lastLogSnapshot?.getValue(CurucaLog::class.java)
                        Log.d("Data", curucaLog.toString())
                        onSuccess(curucaLog)
                    } else {
                        onFailure("Nenhum log encontrado.")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    fun getCurucaLogByDate(
        targetDate: LocalDate,
        onSuccess: (List<CurucaLog>) -> Unit,
        onFailure: (String?) -> Unit
    ) {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val startString = targetDate.atStartOfDay().format(formatter)
        val endString = targetDate.atTime(23, 59, 59).format(formatter)

        database
            .orderByChild("date")
            .startAt(startString)
            .endAt(endString)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val curucaLogs = mutableListOf<CurucaLog>()
                    if (snapshot.exists()) {
                        for (logSnapshot in snapshot.children) {
                            val log = logSnapshot.getValue(CurucaLog::class.java)
                            log?.let { curucaLogs.add(it) }
                        }
                    }
                    onSuccess(curucaLogs)
                }
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }
}