package com.example.data.repository

import com.example.data.dao.SchoolDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

class SchoolRepository(private val schoolDao: SchoolDao) {

    // Students
    val allStudents: Flow<List<Student>> = schoolDao.getAllStudents()
    val studentCount: Flow<Int> = schoolDao.getStudentCount()

    fun getStudentById(id: Int): Flow<Student?> = schoolDao.getStudentById(id)
    fun getStudentsByGrade(grade: String): Flow<List<Student>> = schoolDao.getStudentsByGrade(grade)

    suspend fun insertStudent(student: Student): Long = schoolDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = schoolDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = schoolDao.deleteStudent(student)


    // Teachers
    val allTeachers: Flow<List<Teacher>> = schoolDao.getAllTeachers()
    val teacherCount: Flow<Int> = schoolDao.getTeacherCount()

    fun getTeacherById(id: Int): Flow<Teacher?> = schoolDao.getTeacherById(id)

    suspend fun insertTeacher(teacher: Teacher): Long = schoolDao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: Teacher) = schoolDao.updateTeacher(teacher)
    suspend fun deleteTeacher(teacher: Teacher) = schoolDao.deleteTeacher(teacher)


    // Class subjects
    val allClassSubjects: Flow<List<ClassSubject>> = schoolDao.getAllClassSubjects()

    suspend fun insertClassSubject(classSubject: ClassSubject): Long = schoolDao.insertClassSubject(classSubject)
    suspend fun deleteClassSubject(classSubject: ClassSubject) = schoolDao.deleteClassSubject(classSubject)


    // Attendance
    val allAttendance: Flow<List<Attendance>> = schoolDao.getAllAttendance()
    fun getAttendanceByDate(date: String): Flow<List<Attendance>> = schoolDao.getAttendanceByDate(date)
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>> = schoolDao.getAttendanceForStudent(studentId)

    suspend fun saveAttendanceList(list: List<Attendance>) = schoolDao.saveAttendanceList(list)


    // Grades
    val allGrades: Flow<List<Grade>> = schoolDao.getAllGrades()

    fun getGradesForStudent(studentId: Int): Flow<List<Grade>> = schoolDao.getGradesForStudent(studentId)

    suspend fun insertGrade(grade: Grade): Long = schoolDao.insertGrade(grade)
    suspend fun deleteGrade(grade: Grade) = schoolDao.deleteGrade(grade)


    // Teacher Attendance
    val allTeacherAttendance: Flow<List<TeacherAttendance>> = schoolDao.getAllTeacherAttendance()
    fun getTeacherAttendanceByDate(date: String): Flow<List<TeacherAttendance>> = schoolDao.getTeacherAttendanceByDate(date)
    fun getAttendanceForTeacher(teacherId: Int): Flow<List<TeacherAttendance>> = schoolDao.getAttendanceForTeacher(teacherId)

    suspend fun saveTeacherAttendanceList(list: List<TeacherAttendance>) = schoolDao.saveTeacherAttendanceList(list)


    // Leave Requests
    val allLeaveRequests: Flow<List<LeaveRequest>> = schoolDao.getAllLeaveRequests()

    fun getLeaveRequestsForTeacher(teacherId: Int): Flow<List<LeaveRequest>> = schoolDao.getLeaveRequestsForTeacher(teacherId)

    suspend fun insertLeaveRequest(leaveRequest: LeaveRequest): Long = schoolDao.insertLeaveRequest(leaveRequest)
    suspend fun updateLeaveRequest(leaveRequest: LeaveRequest) = schoolDao.updateLeaveRequest(leaveRequest)
    suspend fun deleteLeaveRequest(leaveRequest: LeaveRequest) = schoolDao.deleteLeaveRequest(leaveRequest)


    // SMS Logs
    val allSmsLogs: Flow<List<SmsLog>> = schoolDao.getAllSmsLogs()
    suspend fun insertSmsLog(smsLog: SmsLog): Long = schoolDao.insertSmsLog(smsLog)

}
