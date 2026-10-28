package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val department: String,
    val instructor: String,
    val room: String,
    val scheduleDays: String,
    val credits: Int = 3,
    val targetAttendancePercent: Int = 75,
    val isCurriculumDefault: Boolean = false,
    val isEnrolled: Boolean = true
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val date: String, // Format: YYYY-MM-DD
    val status: String, // "PRESENT", "ABSENT", "CANCELLED", "HOLIDAY"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectCode: String,
    val subjectName: String,
    val authorName: String,
    val authorRole: String, // "STUDENT", "FACULTY_ADMIN", "PEER"
    val isMyNote: Boolean,
    val isVerifiedCurriculum: Boolean = false,
    val content: String,
    val keyPoints: String,
    val attachmentName: String = "",
    val attachmentUrl: String = "",
    val rating: Float = 4.8f,
    val downloadCount: Int = 0,
    val createdDate: String
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectCode: String,
    val subjectName: String,
    val description: String,
    val dueDate: String, // YYYY-MM-DD
    val isMyAssignment: Boolean,
    val isSubmitted: Boolean = false,
    val submissionDate: String = "",
    val submissionText: String = "",
    val grade: String = "",
    val assignedBy: String,
    val priority: String = "MEDIUM" // "HIGH", "MEDIUM", "LOW"
)

@Entity(tableName = "college_events")
data class CollegeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String, // YYYY-MM-DD
    val type: String, // "EXAM", "HOLIDAY", "SYMPOSIUM", "DEADLINE"
    val description: String
)
