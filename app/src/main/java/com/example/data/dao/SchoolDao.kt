package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Student Queries ---
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Int): Flow<Student?>

    @Query("SELECT * FROM students WHERE gradeLevel = :gradeLevel ORDER BY name ASC")
    fun getStudentsByGrade(gradeLevel: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>


    // --- Teacher Queries ---
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    fun getTeacherById(id: Int): Flow<Teacher?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    @Query("SELECT COUNT(*) FROM teachers")
    fun getTeacherCount(): Flow<Int>


    // --- Class/Subject Queries ---
    @Query("SELECT * FROM class_subjects")
    fun getAllClassSubjects(): Flow<List<ClassSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSubject(classSubject: ClassSubject): Long

    @Delete
    suspend fun deleteClassSubject(classSubject: ClassSubject)


    // --- Attendance Queries ---
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendanceRecord(studentId: Int, date: String)

    @Transaction
    suspend fun saveAttendanceList(list: List<Attendance>) {
        for (item in list) {
            deleteAttendanceRecord(item.studentId, item.date)
            insertAttendance(item)
        }
    }


    // --- Grade Queries ---
    @Query("SELECT * FROM grades WHERE studentId = :studentId")
    fun getGradesForStudent(studentId: Int): Flow<List<Grade>>

    @Query("SELECT * FROM grades ORDER BY dateRecorded DESC")
    fun getAllGrades(): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade): Long

    @Delete
    suspend fun deleteGrade(grade: Grade)


    // --- Teacher Attendance Queries ---
    @Query("SELECT * FROM teacher_attendance ORDER BY date DESC")
    fun getAllTeacherAttendance(): Flow<List<TeacherAttendance>>

    @Query("SELECT * FROM teacher_attendance WHERE date = :date")
    fun getTeacherAttendanceByDate(date: String): Flow<List<TeacherAttendance>>

    @Query("SELECT * FROM teacher_attendance WHERE teacherId = :teacherId")
    fun getAttendanceForTeacher(teacherId: Int): Flow<List<TeacherAttendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAttendance(attendance: TeacherAttendance): Long

    @Query("DELETE FROM teacher_attendance WHERE teacherId = :teacherId AND date = :date")
    suspend fun deleteTeacherAttendanceRecord(teacherId: Int, date: String)

    @Transaction
    suspend fun saveTeacherAttendanceList(list: List<TeacherAttendance>) {
         for (item in list) {
              deleteTeacherAttendanceRecord(item.teacherId, item.date)
              insertTeacherAttendance(item)
         }
    }


    // --- Leave Request Queries ---
    @Query("SELECT * FROM leave_requests ORDER BY startDate DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE teacherId = :teacherId ORDER BY startDate DESC")
    fun getLeaveRequestsForTeacher(teacherId: Int): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leaveRequest: LeaveRequest): Long

    @Update
    suspend fun updateLeaveRequest(leaveRequest: LeaveRequest)

    @Delete
    suspend fun deleteLeaveRequest(leaveRequest: LeaveRequest)


    // --- SMS Logs Queries ---
    @Query("SELECT * FROM sms_logs ORDER BY dateSent DESC")
    fun getAllSmsLogs(): Flow<List<SmsLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(smsLog: SmsLog): Long

}
