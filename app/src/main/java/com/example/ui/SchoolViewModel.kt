package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.SchoolDatabase
import com.example.data.entity.*
import com.example.data.repository.SchoolRepository
import com.example.data.api.askGemini
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    init {
        val db = SchoolDatabase.getDatabase(application)
        repository = SchoolRepository(db.schoolDao())
        
        // Auto pre-populate database if empty on launch
        viewModelScope.launch {
            repository.studentCount.first().let { count ->
                if (count == 0) {
                    prePopulateData()
                }
            }
        }
    }

    // --- State Observables ---
    val students: StateFlow<List<Student>> = repository.allItemsStateFlow { repository.allStudents }
    val teachers: StateFlow<List<Teacher>> = repository.allItemsStateFlow { repository.allTeachers }
    val classSubjects: StateFlow<List<ClassSubject>> = repository.allItemsStateFlow { repository.allClassSubjects }
    val allGrades: StateFlow<List<Grade>> = repository.allItemsStateFlow { repository.allGrades }
    val leaveRequests: StateFlow<List<LeaveRequest>> = repository.allItemsStateFlow { repository.allLeaveRequests }
    val studentAttendance: StateFlow<List<Attendance>> = repository.allItemsStateFlow { repository.allAttendance }
    val teacherAttendanceList: StateFlow<List<TeacherAttendance>> = repository.allItemsStateFlow { repository.allTeacherAttendance }
    val smsLogs: StateFlow<List<SmsLog>> = repository.allItemsStateFlow { repository.allSmsLogs }

    val studentCount: StateFlow<Int> = repository.studentCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val teacherCount: StateFlow<Int> = repository.teacherCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Helper extension to keep flows alive reactively
    private fun <T> SchoolRepository.allItemsStateFlow(query: () -> Flow<List<T>>): StateFlow<List<T>> {
        return query().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // --- State Projections (Dashboard Metrics) ---
    val totalFeesStats = students.map { list ->
        val totalExpected = list.sumOf { it.feesTotal }
        val totalCollected = list.sumOf { it.feesPaid }
        val remainingBalance = totalExpected - totalCollected
        FeesStats(totalExpected, totalCollected, remainingBalance)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeesStats(0.0, 0.0, 0.0)
    )

    val averagePerformanceScore = allGrades.map { list ->
        if (list.isEmpty()) 0.0
        else list.sumOf { (it.score / it.maxScore) * 100 } / list.size
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    // --- Attendance Editing State ---
    private val _attendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val attendanceDate: StateFlow<String> = _attendanceDate.asStateFlow()

    private val _attendanceGrade = MutableStateFlow("Grade 10-A")
    val attendanceGrade: StateFlow<String> = _attendanceGrade.asStateFlow()

    // Temporary session cache for attendance editing, studentId -> status ("Present", "Absent", "Late")
    private val _attendanceEditingMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val attendanceEditingMap: StateFlow<Map<Int, String>> = _attendanceEditingMap.asStateFlow()

    // Query active attendance of selected date
    val activeAttendanceRecord: StateFlow<List<Attendance>> = _attendanceDate
        .flatMapLatest { date -> repository.getAttendanceByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered students for editing attendance
    val attendanceStudents: StateFlow<List<Student>> = combine(students, _attendanceGrade) { list, grade ->
        list.filter { it.gradeLevel == grade && it.status == "Active" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Teacher Attendance Editing State ---
    private val _teacherAttendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val teacherAttendanceDate: StateFlow<String> = _teacherAttendanceDate.asStateFlow()

    private val _teacherAttendanceEditingMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val teacherAttendanceEditingMap: StateFlow<Map<Int, String>> = _teacherAttendanceEditingMap.asStateFlow()

    val activeTeacherAttendanceRecord: StateFlow<List<TeacherAttendance>> = _teacherAttendanceDate
        .flatMapLatest { date -> repository.getTeacherAttendanceByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTeacherAttendanceDate(dateString: String) {
        _teacherAttendanceDate.value = dateString
        syncTeacherAttendanceDetails()
    }

    fun syncTeacherAttendanceDetails() {
        viewModelScope.launch {
            val date = _teacherAttendanceDate.value
            val currentInDb = repository.getTeacherAttendanceByDate(date).first()
            val teachersList = teachers.first()
            
            val initialMap = mutableMapOf<Int, String>()
            for (teacher in teachersList) {
                val match = currentInDb.find { it.teacherId == teacher.id }
                initialMap[teacher.id] = match?.status ?: "Present"
            }
            _teacherAttendanceEditingMap.value = initialMap
        }
    }

    fun updateTeacherAttendanceStatus(teacherId: Int, status: String) {
        val currentMap = _teacherAttendanceEditingMap.value.toMutableMap()
        currentMap[teacherId] = status
        _teacherAttendanceEditingMap.value = currentMap
    }

    fun saveTeacherAttendance() {
        viewModelScope.launch {
            val date = _teacherAttendanceDate.value
            val attendanceList = _teacherAttendanceEditingMap.value.map { (teacherId, status) ->
                TeacherAttendance(teacherId = teacherId, date = date, status = status)
            }
            repository.saveTeacherAttendanceList(attendanceList)
            syncTeacherAttendanceDetails()
        }
    }

    fun setAttendanceDate(dateString: String) {
        _attendanceDate.value = dateString
        syncAttendanceDetails()
    }

    fun setAttendanceGrade(grade: String) {
        _attendanceGrade.value = grade
        syncAttendanceDetails()
    }

    // Reset Map based on what exists in the DB or default to "Present"
    fun syncAttendanceDetails() {
        viewModelScope.launch {
            val date = _attendanceDate.value
            val currentInDb = repository.getAttendanceByDate(date).first()
            val classStudentsList = attendanceStudents.first()
            
            val initialMap = mutableMapOf<Int, String>()
            for (student in classStudentsList) {
                val match = currentInDb.find { it.studentId == student.id }
                initialMap[student.id] = match?.status ?: "Present"
            }
            _attendanceEditingMap.value = initialMap
        }
    }

    fun updateAttendanceStatus(studentId: Int, status: String) {
        val currentMap = _attendanceEditingMap.value.toMutableMap()
        currentMap[studentId] = status
        _attendanceEditingMap.value = currentMap
    }

    fun saveAttendance() {
        viewModelScope.launch {
            val date = _attendanceDate.value
            val attendanceList = _attendanceEditingMap.value.map { (studentId, status) ->
                Attendance(studentId = studentId, date = date, status = status)
            }
            repository.saveAttendanceList(attendanceList)
            syncAttendanceDetails()
        }
    }


    // --- Student Actions ---
    fun getStudentProfile(studentId: Int): Flow<Student?> = repository.getStudentById(studentId)
    fun getGradesOfStudent(studentId: Int): Flow<List<Grade>> = repository.getGradesForStudent(studentId)
    fun getAttendanceOfStudent(studentId: Int): Flow<List<Attendance>> = repository.getAttendanceForStudent(studentId)

    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun addFeePayment(studentId: Int, amount: Double) {
        viewModelScope.launch {
            val s = repository.getStudentById(studentId).first()
            if (s != null) {
                val newPaid = (s.feesPaid + amount).coerceAtMost(s.feesTotal)
                repository.updateStudent(s.copy(feesPaid = newPaid))
            }
        }
    }


    // --- Teacher Actions ---
    fun getTeacherProfile(teacherId: Int): Flow<Teacher?> = repository.getTeacherById(teacherId)

    fun saveTeacher(teacher: Teacher) {
        viewModelScope.launch {
            if (teacher.id == 0) {
                repository.insertTeacher(teacher)
            } else {
                repository.updateTeacher(teacher)
            }
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
        }
    }


    // --- Leave Request Actions ---
    fun saveLeaveRequest(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            if (leaveRequest.id == 0) {
                repository.insertLeaveRequest(leaveRequest)
            } else {
                repository.updateLeaveRequest(leaveRequest)
            }
        }
    }

    fun deleteLeaveRequest(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            repository.deleteLeaveRequest(leaveRequest)
        }
    }


    // --- Class Subject Actions ---
    fun saveClassSubject(className: String, subjectName: String, teacherId: Int) {
        viewModelScope.launch {
            repository.insertClassSubject(
                ClassSubject(className = className, subjectName = subjectName, teacherId = teacherId)
            )
        }
    }

    fun removeClassSubject(classSubject: ClassSubject) {
        viewModelScope.launch {
            repository.deleteClassSubject(classSubject)
        }
    }


    // --- Grade Actions ---
    fun insertGrade(studentId: Int, subjectName: String, examName: String, score: Double, maxScore: Double) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertGrade(
                Grade(
                    studentId = studentId,
                    subjectName = subjectName,
                    examName = examName,
                    score = score,
                    maxScore = maxScore,
                    dateRecorded = today
                )
            )
        }
    }

    fun removeGrade(grade: Grade) {
        viewModelScope.launch {
            repository.deleteGrade(grade)
        }
    }


    // --- Prepopulation helper ---
    private suspend fun prePopulateData() {
        // 1. Teachers
        val tId1 = repository.insertTeacher(Teacher(
            name = "Mrs. Florence Namaganda", 
            email = "florence.n@pearl.ac.ug", 
            phone = "+256 772 400101", 
            subjectSpecialty = "Mathematics", 
            status = "Active",
            qualifications = "Diploma in Primary Education (Kyambogo Univ.)",
            assignedRole = "Senior Math Teacher",
            dateOfBirth = "1983-05-12",
            address = "Kireka, Kampala"
        ))
        val tId2 = repository.insertTeacher(Teacher(
            name = "Mr. Okello John", 
            email = "john.o@pearl.ac.ug", 
            phone = "+256 701 500200", 
            subjectSpecialty = "Integrated Science", 
            status = "Active",
            qualifications = "B.Education (Hons), Makerere University",
            assignedRole = "Science Coordinator",
            dateOfBirth = "1988-10-24",
            address = "Nansana, Wakiso"
        ))
        val tId3 = repository.insertTeacher(Teacher(
            name = "Miss Nabakooza Sarah", 
            email = "sarah.n@pearl.ac.ug", 
            phone = "+256 782 112233", 
            subjectSpecialty = "English Language", 
            status = "Active",
            qualifications = "M.Ed in Educational Management (Makerere)",
            assignedRole = "Head Teacher",
            dateOfBirth = "1979-01-15",
            address = "Kololo, Kampala"
        ))
        val tId4 = repository.insertTeacher(Teacher(
            name = "Mr. Mukasa Ronald", 
            email = "ronald.m@pearl.ac.ug", 
            phone = "+256 754 443322", 
            subjectSpecialty = "Social Studies", 
            status = "Active",
            qualifications = "B.Science with Education (Kyambogo)",
            assignedRole = "SST Department Head",
            dateOfBirth = "1985-07-07",
            address = "Seeta, Mukono"
        ))
        val tId5 = repository.insertTeacher(Teacher(
            name = "Mrs. Atwine Brenda", 
            email = "brenda.a@pearl.ac.ug", 
            phone = "+256 773 889900", 
            subjectSpecialty = "Nursery Specialist", 
            status = "On Leave",
            qualifications = "Diploma in Early Childhood Development",
            assignedRole = "Top Class Director",
            dateOfBirth = "1991-12-03",
            address = "Ntinda, Kampala"
        ))

        // 2. Class Subject setup
        repository.insertClassSubject(ClassSubject(className = "Primary Seven (P.7)", subjectName = "Mathematics", teacherId = tId1.toInt()))
        repository.insertClassSubject(ClassSubject(className = "Primary Seven (P.7)", subjectName = "English Language", teacherId = tId3.toInt()))
        repository.insertClassSubject(ClassSubject(className = "Primary Seven (P.7)", subjectName = "Integrated Science", teacherId = tId2.toInt()))
        repository.insertClassSubject(ClassSubject(className = "Primary Seven (P.7)", subjectName = "Social Studies", teacherId = tId4.toInt()))
        repository.insertClassSubject(ClassSubject(className = "Top Class", subjectName = "Literacy & Numeracy", teacherId = tId5.toInt()))

        // 3. Students (with fees in Ugandan Shillings)
        val sId1 = repository.insertStudent(Student(name = "Ssenyonjo Brian", rollNumber = "S1001", gradeLevel = "Primary Seven (P.7)", email = "brian.s@pearl.ac.ug", phone = "+256 772 110998", gender = "Male", feesTotal = 850000.0, feesPaid = 600000.0))
        val sId2 = repository.insertStudent(Student(name = "Nalwadda Esther", rollNumber = "S1002", gradeLevel = "Primary Seven (P.7)", email = "esther.n@pearl.ac.ug", phone = "+256 701 445566", gender = "Female", feesTotal = 850000.0, feesPaid = 850000.0))
        val sId3 = repository.insertStudent(Student(name = "Kato Joseph", rollNumber = "S1003", gradeLevel = "Primary One (P.1)", email = "joseph.k@pearl.ac.ug", phone = "+256 782 558899", gender = "Male", feesTotal = 650000.0, feesPaid = 350000.0))
        val sId4 = repository.insertStudent(Student(name = "Babirye Sandra", rollNumber = "S1004", gradeLevel = "Top Class", email = "sandra.b@pearl.ac.ug", phone = "+256 774 121212", gender = "Female", feesTotal = 500000.0, feesPaid = 500000.0))
        val sId5 = repository.insertStudent(Student(name = "Akena Emmanuel", rollNumber = "S1005", gradeLevel = "Primary Seven (P.7)", email = "emmanuel.a@pearl.ac.ug", phone = "+256 752 998877", gender = "Male", feesTotal = 850000.0, feesPaid = 0.0))

        // 4. Grades
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        repository.insertGrade(Grade(studentId = sId1.toInt(), subjectName = "Mathematics", examName = "Term Assessment 1", score = 82.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId1.toInt(), subjectName = "English Language", examName = "Term Assessment 1", score = 76.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId1.toInt(), subjectName = "Integrated Science", examName = "Term Assessment 1", score = 89.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId1.toInt(), subjectName = "Social Studies", examName = "Term Assessment 1", score = 91.0, dateRecorded = today))

        repository.insertGrade(Grade(studentId = sId2.toInt(), subjectName = "Mathematics", examName = "Term Assessment 1", score = 94.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId2.toInt(), subjectName = "English Language", examName = "Term Assessment 1", score = 88.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId2.toInt(), subjectName = "Integrated Science", examName = "Term Assessment 1", score = 92.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId2.toInt(), subjectName = "Social Studies", examName = "Term Assessment 1", score = 85.0, dateRecorded = today))

        repository.insertGrade(Grade(studentId = sId5.toInt(), subjectName = "Mathematics", examName = "Term Assessment 1", score = 45.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId5.toInt(), subjectName = "English Language", examName = "Term Assessment 1", score = 52.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId5.toInt(), subjectName = "Integrated Science", examName = "Term Assessment 1", score = 60.0, dateRecorded = today))
        repository.insertGrade(Grade(studentId = sId5.toInt(), subjectName = "Social Studies", examName = "Term Assessment 1", score = 41.0, dateRecorded = today))

        repository.insertGrade(Grade(studentId = sId4.toInt(), subjectName = "Literacy & Numeracy", examName = "Nursery Assesment", score = 90.0, dateRecorded = today))

        // 5. Setup initial attendance record
        repository.saveAttendanceList(listOf(
            Attendance(studentId = sId2.toInt(), date = today, status = "Present"),
            Attendance(studentId = sId3.toInt(), date = today, status = "Absent"),
            Attendance(studentId = sId1.toInt(), date = today, status = "Present")
        ))

        // 6. Setup initial teacher attendance & leave records
        repository.saveTeacherAttendanceList(listOf(
            TeacherAttendance(teacherId = tId1.toInt(), date = today, status = "Present"),
            TeacherAttendance(teacherId = tId2.toInt(), date = today, status = "Present"),
            TeacherAttendance(teacherId = tId3.toInt(), date = today, status = "Present"),
            TeacherAttendance(teacherId = tId4.toInt(), date = today, status = "Present"),
            TeacherAttendance(teacherId = tId5.toInt(), date = today, status = "On Leave")
        ))

        repository.insertLeaveRequest(LeaveRequest(
            teacherId = tId5.toInt(),
            startDate = today,
            endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L)),
            reason = "Maternity and childhood support leave",
            status = "Approved",
            leaveType = "Maternity/Paternity"
        ))
        repository.insertLeaveRequest(LeaveRequest(
            teacherId = tId1.toInt(),
            startDate = today,
            endDate = today,
            reason = "Attending UNEB senior math examiners meeting",
            status = "Pending",
            leaveType = "Casual Leave"
        ))
    }

    // --- AI Chat States & Integrations ---
    data class ChatMessage(val isUser: Boolean, val message: String, val timestamp: Long = System.currentTimeMillis())

    private val _aiChatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val aiChatHistory: StateFlow<List<ChatMessage>> = _aiChatHistory.asStateFlow()

    private val _aiIsLoading = MutableStateFlow(false)
    val aiIsLoading: StateFlow<Boolean> = _aiIsLoading.asStateFlow()

    fun clearAiChat() {
        _aiChatHistory.value = emptyList()
    }

    fun sendPromptToAI(promptText: String) {
        if (promptText.isBlank()) return
        
        // Add user message to history
        val userMsg = ChatMessage(isUser = true, message = promptText)
        _aiChatHistory.value = _aiChatHistory.value + userMsg
        _aiIsLoading.value = true

        viewModelScope.launch {
            try {
                val dbState = getSystemContext()
                val systemPrompt = """
                    You are Pearl Junior School's AI Senior Copilot, an expert educational administrator assistant.
                    You have complete real-time visibility and query access across all modules in this school management system.
                    
                    Operational Guidelines:
                    1. Leverage the rich actual school data payload below to answer any user questions perfectly without referencing fake files or hardcoded stubs.
                    2. Analyze averages, trends, sums, balances, and counts accurately. Speak with authority using dates/times and specific statistics.
                    3. If the user asks about a specific person (student or teacher), list their full directory details, contacts, performance grades, attendance logs, and fee standings.
                    4. Conduct calculations elegantly (e.g. attendance percentage = Present Days / Total Days).
                    5. Format response beautifully with bullet points, dividers, or clear lists. Speak politely, professionally, and clearly.
                    
                    School Database Context Payload:
                    $dbState
                """.trimIndent()
                
                val aiResponse = askGemini(systemPrompt, promptText)
                _aiChatHistory.value = _aiChatHistory.value + ChatMessage(isUser = false, message = aiResponse)
            } catch (e: Exception) {
                _aiChatHistory.value = _aiChatHistory.value + ChatMessage(isUser = false, message = "Error: ${e.message}")
            } finally {
                _aiIsLoading.value = false
            }
        }
    }

    fun getSystemContext(): String {
        val s = students.value
        val t = teachers.value
        val c = classSubjects.value
        val g = allGrades.value
        val l = leaveRequests.value
        val sa = studentAttendance.value
        val ta = teacherAttendanceList.value

        val builder = StringBuilder()
        builder.append("Current Live School Database State:\n\n")

        builder.append("--- STUDENTS (${s.size}) ---\n")
        s.forEach { student ->
            builder.append("ID: ${student.id}, Name: ${student.name}, Roll: ${student.rollNumber}, Grade: ${student.gradeLevel}, Email: ${student.email}, Phone: ${student.phone}, Gender: ${student.gender}, FeesExpected: ${student.feesTotal}, FeesPaid: ${student.feesPaid}, FeesRemaining: ${student.feesTotal - student.feesPaid}, Status: ${student.status}\n")
        }

        builder.append("\n--- FACULTY/TEACHERS (${t.size}) ---\n")
        t.forEach { teacher ->
            builder.append("ID: ${teacher.id}, Name: ${teacher.name}, Specialty: ${teacher.subjectSpecialty}, Email: ${teacher.email}, Phone: ${teacher.phone}, Role: ${teacher.assignedRole}, Qualifications: ${teacher.qualifications}, Status: ${teacher.status}, DOB: ${teacher.dateOfBirth}, Address: ${teacher.address}\n")
        }

        builder.append("\n--- CLASS & SUBJECT ASSIGNMENTS (${c.size}) ---\n")
        c.forEach { cs ->
            val teacherName = t.find { it.id == cs.teacherId }?.name ?: "Unknown Teacher"
            builder.append("Class: ${cs.className}, Subject: ${cs.subjectName}, Teacher: $teacherName (ID: ${cs.teacherId})\n")
        }

        builder.append("\n--- STUDENT ACADEMIC GRADES (${g.size}) ---\n")
        g.forEach { grade ->
            val studentName = s.find { it.id == grade.studentId }?.name ?: "Unknown Student"
            builder.append("Student: $studentName (ID: ${grade.studentId}), Subject: ${grade.subjectName}, Exam: ${grade.examName}, Score: ${grade.score}/${grade.maxScore}, Date: ${grade.dateRecorded}\n")
        }

        builder.append("\n--- FACULTY LEAVE REQUESTS (${l.size}) ---\n")
        l.forEach { req ->
            val teacherName = t.find { it.id == req.teacherId }?.name ?: "Unknown Teacher"
            builder.append("Teacher: $teacherName (ID: ${req.teacherId}), Leave Type: ${req.leaveType}, Reason: ${req.reason}, Start: ${req.startDate}, End: ${req.endDate}, Status: ${req.status}\n")
        }

        builder.append("\n--- STUDENT DAILY ATTENDANCE (${sa.size} records) ---\n")
        sa.forEach { att ->
            val studentName = s.find { it.id == att.studentId }?.name ?: "Unknown Student"
            builder.append("Date: ${att.date}, Student: $studentName (ID: ${att.studentId}), Status: ${att.status}\n")
        }

        builder.append("\n--- FACULTY DAILY ATTENDANCE (${ta.size} records) ---\n")
        ta.forEach { att ->
            val teacherName = t.find { it.id == att.teacherId }?.name ?: "Unknown Teacher"
            builder.append("Date: ${att.date}, Teacher: $teacherName (ID: ${att.teacherId}), Status: ${att.status}\n")
        }

        val sms = smsLogs.value
        builder.append("\n--- SENT SMS BROADCAST LOGS (${sms.size} records) ---\n")
        sms.forEach { log ->
            builder.append("ID: ${log.id}, SentTo: ${log.recipientName}, Phone: ${log.phoneNumber}, Message: ${log.message}, SentAt: ${log.dateSent}, Status: ${log.status}\n")
        }

        return builder.toString()
    }

    fun broadcastSms(recipientName: String, phoneNumbers: List<String>, message: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            try {
                val smsManager: android.telephony.SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    getApplication<Application>().getSystemService(android.telephony.SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    android.telephony.SmsManager.getDefault()
                }
                phoneNumbers.forEach { number ->
                    if (number.isNotBlank()) {
                        smsManager.sendTextMessage(number, null, message, null, null)
                    }
                }
            } catch (e: Exception) {
                // Ignore failure on devices without carrier network, fall back to DB log only
            }

            // Save database log entry
            repository.insertSmsLog(SmsLog(
                recipientName = recipientName,
                phoneNumber = phoneNumbers.filter { it.isNotBlank() }.joinToString(", "),
                message = message,
                dateSent = dateStr,
                status = "Sent"
            ))
        }
    }
}

data class FeesStats(
    val totalExpected: Double,
    val totalCollected: Double,
    val remainingBalance: Double
)

class SchoolViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
