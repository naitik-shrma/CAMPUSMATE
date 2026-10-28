package com.example.data.repository

import com.example.data.local.CollegeDao
import com.example.data.model.AssignmentEntity
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.CollegeEventEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.flow.Flow

class CollegeRepository(private val dao: CollegeDao) {

    // Subjects
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val enrolledSubjects: Flow<List<SubjectEntity>> = dao.getEnrolledSubjects()

    suspend fun insertSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)
    suspend fun deleteSubjectById(id: Long) = dao.deleteSubjectById(id)

    suspend fun toggleSubjectEnrollment(subject: SubjectEntity) {
        dao.updateSubject(subject.copy(isEnrolled = !subject.isEnrolled))
    }

    // Attendance
    val allAttendance: Flow<List<AttendanceRecordEntity>> = dao.getAllAttendance()

    fun getAttendanceForSubject(subjectId: Long): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceForSubject(subjectId)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceForDate(date)

    suspend fun markAttendance(subjectId: Long, date: String, status: String, notes: String = "") {
        dao.deleteAttendanceForSubjectAndDate(subjectId, date)
        if (status.isNotBlank()) {
            dao.insertAttendance(
                AttendanceRecordEntity(
                    subjectId = subjectId,
                    date = date,
                    status = status,
                    notes = notes
                )
            )
        }
    }

    suspend fun deleteAttendance(record: AttendanceRecordEntity) = dao.deleteAttendance(record)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val myNotes: Flow<List<NoteEntity>> = dao.getMyNotes()
    val peerAndAdminNotes: Flow<List<NoteEntity>> = dao.getPeerAndAdminNotes()

    suspend fun insertNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)

    // Assignments
    val allAssignments: Flow<List<AssignmentEntity>> = dao.getAllAssignments()
    val myAssignments: Flow<List<AssignmentEntity>> = dao.getMyAssignments()
    val classAssignments: Flow<List<AssignmentEntity>> = dao.getClassAssignments()

    suspend fun insertAssignment(assignment: AssignmentEntity): Long = dao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: AssignmentEntity) = dao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: AssignmentEntity) = dao.deleteAssignment(assignment)

    suspend fun submitAssignment(assignmentId: Long, current: AssignmentEntity, submissionText: String) {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val updated = current.copy(
            isSubmitted = true,
            submissionDate = now,
            submissionText = submissionText,
            grade = if (current.grade.isBlank()) "Submitted - Pending Review" else current.grade
        )
        dao.updateAssignment(updated)
    }

    suspend fun markAssignmentPending(assignment: AssignmentEntity) {
        val updated = assignment.copy(
            isSubmitted = false,
            submissionDate = "",
            submissionText = ""
        )
        dao.updateAssignment(updated)
    }

    // Events
    val allEvents: Flow<List<CollegeEventEntity>> = dao.getAllEvents()
    suspend fun insertEvent(event: CollegeEventEntity): Long = dao.insertEvent(event)
    suspend fun deleteEvent(event: CollegeEventEntity) = dao.deleteEvent(event)
}
