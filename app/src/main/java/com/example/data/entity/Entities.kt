package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rollNumber: String,
    val gradeLevel: String,
    val email: String,
    val phone: String,
    val gender: String,
    val feesTotal: Double = 1200.0,
    val feesPaid: Double = 0.0,
    val status: String = "Active"
) : Serializable

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val subjectSpecialty: String,
    val status: String = "Active",
    val qualifications: String = "",
    val assignedRole: String = "Teacher",
    val dateOfBirth: String = "",
    val address: String = ""
) : Serializable

@Entity(tableName = "teacher_attendance")
data class TeacherAttendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherId: Int,
    val date: String, // "yyyy-MM-dd"
    val status: String // "Present", "Absent", "Late", "On Leave"
) : Serializable

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherId: Int,
    val startDate: String, // "yyyy-MM-dd"
    val endDate: String, // "yyyy-MM-dd"
    val reason: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val leaveType: String = "Sick Leave" // "Sick Leave", "Casual Leave", "Annual Leave", etc.
) : Serializable

@Entity(tableName = "class_subjects")
data class ClassSubject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String, // e.g. "Grade 10-A"
    val subjectName: String, // e.g. "Mathematics"
    val teacherId: Int // Reference to Teacher table
) : Serializable

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val date: String, // "yyyy-MM-dd"
    val status: String // "Present", "Absent", "Late"
)

@Entity(tableName = "grades")
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val subjectName: String, // e.g. "Mathematics"
    val examName: String, // e.g. "Midterm Exam"
    val score: Double,
    val maxScore: Double = 100.0,
    val dateRecorded: String
)

@Entity(tableName = "sms_logs")
data class SmsLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipientName: String,
    val phoneNumber: String,
    val message: String,
    val dateSent: String,
    val status: String = "Sent"
) : Serializable

@Entity(tableName = "timetable_periods")
data class TimetablePeriod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String, // e.g. "P.7", "Top"
    val subjectName: String,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val startTime: String, // "08:30"
    val endTime: String, // "09:30"
    val teacherName: String
) : Serializable

@Entity(tableName = "school_events")
data class SchoolEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val eventDate: String, // "yyyy-MM-dd"
    val description: String,
    val audience: String, // "All", "Parents", "Teachers"
    val priority: String // "High", "Medium", "Low"
) : Serializable

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String, // "Event", "Timetable", "Leave", "Grade", "Fees", "Lesson"
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
) : Serializable

@Entity(tableName = "lesson_tracks")
data class LessonTrack(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timetablePeriodId: Int,
    val className: String,
    val subjectName: String,
    val teacherName: String,
    val trackDate: String, // "yyyy-MM-dd"
    val status: String, // "Taught", "Missed", "Substitute Assigned", "Cancelled"
    val substituteTeacherName: String = "",
    val notes: String = "",
    val punctuality: String = "Punctual" // "Punctual", "Late", "N/A"
) : Serializable
