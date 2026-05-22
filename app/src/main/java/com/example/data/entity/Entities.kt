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
