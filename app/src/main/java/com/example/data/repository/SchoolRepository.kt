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

    // Timetable
    val allTimetablePeriods: Flow<List<TimetablePeriod>> = schoolDao.getAllTimetablePeriods()
    suspend fun insertTimetablePeriod(period: TimetablePeriod): Long = schoolDao.insertTimetablePeriod(period)
    suspend fun deleteTimetablePeriod(id: Int) = schoolDao.deleteTimetablePeriod(id)
    suspend fun deleteAllTimetablePeriods() = schoolDao.deleteAllTimetablePeriods()

    // School Events
    val allSchoolEvents: Flow<List<SchoolEvent>> = schoolDao.getAllSchoolEvents()
    suspend fun insertSchoolEvent(event: SchoolEvent): Long = schoolDao.insertSchoolEvent(event)
    suspend fun deleteSchoolEvent(id: Int) = schoolDao.deleteSchoolEvent(id)

    // App Notifications
    val allAppNotifications: Flow<List<AppNotification>> = schoolDao.getAllAppNotifications()
    suspend fun insertAppNotification(notification: AppNotification): Long = schoolDao.insertAppNotification(notification)
    suspend fun markAllAppNotificationsAsRead() = schoolDao.markAllAppNotificationsAsRead()
    suspend fun clearAllAppNotifications() = schoolDao.clearAllAppNotifications()

    // Lesson Tracks
    val allLessonTracks: Flow<List<LessonTrack>> = schoolDao.getAllLessonTracks()
    suspend fun insertLessonTrack(lessonTrack: LessonTrack): Long = schoolDao.insertLessonTrack(lessonTrack)
    suspend fun deleteLessonTrack(id: Int) = schoolDao.deleteLessonTrack(id)

    // Fee Payments
    val allFeePayments: Flow<List<FeePayment>> = schoolDao.getAllFeePayments()
    suspend fun insertFeePayment(payment: FeePayment): Long = schoolDao.insertFeePayment(payment)
    suspend fun deleteFeePayment(id: Int) = schoolDao.deleteFeePayment(id)

    // Library - Books
    val allBooks: Flow<List<Book>> = schoolDao.getAllBooks()
    fun getBookById(id: Int): Flow<Book?> = schoolDao.getBookById(id)
    suspend fun insertBook(book: Book): Long = schoolDao.insertBook(book)
    suspend fun updateBook(book: Book) = schoolDao.updateBook(book)
    suspend fun deleteBook(book: Book) = schoolDao.deleteBook(book)

    // Library - Checkouts
    val allCheckouts: Flow<List<BookCheckout>> = schoolDao.getAllCheckouts()
    fun getCheckoutsForStudent(studentId: Int): Flow<List<BookCheckout>> = schoolDao.getCheckoutsForStudent(studentId)
    suspend fun insertCheckout(checkout: BookCheckout): Long = schoolDao.insertCheckout(checkout)
    suspend fun updateCheckout(checkout: BookCheckout) = schoolDao.updateCheckout(checkout)
    suspend fun deleteCheckout(checkout: BookCheckout) = schoolDao.deleteCheckout(checkout)

}
