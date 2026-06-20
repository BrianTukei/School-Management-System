package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.data.entity.Student
import com.example.data.entity.FeePayment
import com.example.data.entity.Attendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseService {
    private var firestoreInstance: com.google.firebase.firestore.FirebaseFirestore? = null

    private fun getFirestore(context: Context): com.google.firebase.firestore.FirebaseFirestore? {
        if (firestoreInstance != null) return firestoreInstance
        val (dbUrl, token, enabled) = getFirebaseConfig(context)
        if (!enabled || dbUrl.isBlank()) return null

        // Extract project ID if dbUrl looks like a URL, otherwise use it directly
        val projectId = if (dbUrl.contains(".firebaseio.com") || dbUrl.contains("http://") || dbUrl.contains("https://")) {
            dbUrl.substringAfter("://").substringBefore(".firebaseio.com").substringBefore("/")
        } else {
            dbUrl
        }

        return try {
            val app = try {
                com.google.firebase.FirebaseApp.getInstance()
            } catch (e: Exception) {
                val builder = com.google.firebase.FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApplicationId("com.aistudio.schoolmanager")
                if (token.isNotBlank()) {
                    builder.setApiKey(token)
                }
                com.google.firebase.FirebaseApp.initializeApp(context, builder.build())
            }
            firestoreInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance(app)
            firestoreInstance
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore init error", e)
            null
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Firestore task failed"))
            }
        }
    }

    fun getFirebaseConfig(context: Context): Triple<String, String, Boolean> {
        val prefs = context.getSharedPreferences("school_prefs", Context.MODE_PRIVATE)
        val url = prefs.getString("firebase_db_url", "") ?: ""
        val token = prefs.getString("firebase_auth_token", "") ?: ""
        val enabled = prefs.getBoolean("firebase_sync_enabled", true)
        return Triple(url, token, enabled)
    }

    fun saveFirebaseConfig(context: Context, url: String, token: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences("school_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("firebase_db_url", url.trim())
            .putString("firebase_auth_token", token.trim())
            .putBoolean("firebase_sync_enabled", enabled)
            .apply()
        
        // Reset cached instance on config change to re-initialize
        firestoreInstance = null
    }

    suspend fun uploadStudentToFirebase(context: Context, student: Student, studentIdOverride: Int? = null): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context) ?: return@withContext false
        val studentId = studentIdOverride ?: student.id
        val key = if (studentId == 0) "temp_${student.rollNumber}" else studentId.toString()

        val data = mapOf(
            "id" to studentId,
            "name" to student.name,
            "rollNumber" to student.rollNumber,
            "gradeLevel" to student.gradeLevel,
            "email" to student.email,
            "phone" to student.phone,
            "gender" to student.gender,
            "feesTotal" to student.feesTotal,
            "feesPaid" to student.feesPaid,
            "status" to student.status,
            "dateOfBirth" to student.dateOfBirth,
            "parentName" to student.parentName,
            "isReportCardPublished" to student.isReportCardPublished
        )

        return@withContext try {
            firestore.collection("students").document(key).set(data).awaitTask()
            Log.d("FirebaseService", "Successfully saved student to Firestore: $key")
            true
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore student save error", e)
            false
        }
    }

    suspend fun deleteStudentFromFirebase(context: Context, studentId: Int): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context) ?: return@withContext false
        val key = studentId.toString()

        return@withContext try {
            firestore.collection("students").document(key).delete().awaitTask()
            Log.d("FirebaseService", "Successfully deleted student from Firestore: $key")
            true
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore student delete error", e)
            false
        }
    }

    suspend fun uploadFeePaymentToFirebase(context: Context, payment: FeePayment): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context) ?: return@withContext false
        val key = payment.id.toString()

        val data = mapOf(
            "id" to payment.id,
            "studentId" to payment.studentId,
            "paymentDate" to payment.paymentDate,
            "amount" to payment.amount,
            "notes" to payment.notes
        )

        return@withContext try {
            firestore.collection("fee_payments").document(key).set(data).awaitTask()
            Log.d("FirebaseService", "Successfully saved fee payment to Firestore: $key")
            true
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore payment save error", e)
            false
        }
    }

    suspend fun deleteFeePaymentFromFirebase(context: Context, paymentId: Int): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context) ?: return@withContext false
        val key = paymentId.toString()

        return@withContext try {
            firestore.collection("fee_payments").document(key).delete().awaitTask()
            Log.d("FirebaseService", "Successfully deleted fee payment from Firestore: $key")
            true
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore payment delete error", e)
            false
        }
    }

    suspend fun uploadAttendanceToFirebase(context: Context, attendance: Attendance): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context) ?: return@withContext false
        val key = "${attendance.studentId}_${attendance.date}"

        val data = mapOf(
            "id" to attendance.id,
            "studentId" to attendance.studentId,
            "date" to attendance.date,
            "status" to attendance.status
        )

        return@withContext try {
            firestore.collection("attendance").document(key).set(data).awaitTask()
            Log.d("FirebaseService", "Successfully saved attendance to Firestore: $key")
            true
        } catch (e: Exception) {
            Log.e("FirebaseService", "Firestore attendance save error", e)
            false
        }
    }
}
