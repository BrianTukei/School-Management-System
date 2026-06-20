package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.SchoolDao
import com.example.data.entity.*

@Database(
    entities = [
        Student::class,
        Teacher::class,
        ClassSubject::class,
        Attendance::class,
        Grade::class,
        TeacherAttendance::class,
        LeaveRequest::class,
        SmsLog::class,
        TimetablePeriod::class,
        SchoolEvent::class,
        AppNotification::class,
        LessonTrack::class,
        FeePayment::class,
        Book::class,
        BookCheckout::class
    ],
    version = 10,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {

    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "school_manager_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
