package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CollegeDatabase
import com.example.data.model.AssignmentEntity
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.CollegeEventEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SubjectEntity
import com.example.data.repository.CollegeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SubjectAttendanceStats(
    val subject: SubjectEntity,
    val attended: Int,
    val missed: Int,
    val cancelled: Int,
    val totalConducted: Int,
    val percentage: Float,
    val safeBunks: Int,
    val requiredConsecutive: Int,
    val isSafe: Boolean
)

enum class HealthStatus {
    EXCELLENT, SAFE, WARNING, DANGER
}

data class OverallAttendanceStats(
    val totalAttended: Int,
    val totalConducted: Int,
    val overallPercentage: Float,
    val health: HealthStatus,
    val statusMessage: String
)

class CollegeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CollegeRepository

    init {
        val database = CollegeDatabase.getDatabase(application, viewModelScope)
        repository = CollegeRepository(database.collegeDao())
    }

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enrolledSubjects: StateFlow<List<SubjectEntity>> = repository.enrolledSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceRecordEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myNotes: StateFlow<List<NoteEntity>> = repository.myNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peerAndAdminNotes: StateFlow<List<NoteEntity>> = repository.peerAndAdminNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myAssignments: StateFlow<List<AssignmentEntity>> = repository.myAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classAssignments: StateFlow<List<AssignmentEntity>> = repository.classAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<AssignmentEntity>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<CollegeEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin mode toggle (allows user to act as college admin or student)
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    // Selected Calendar Date (defaults to today's date)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // Computed Stats per Subject
    val subjectStats: StateFlow<List<SubjectAttendanceStats>> = combine(
        enrolledSubjects,
        allAttendance
    ) { subjects, records ->
        subjects.map { subject ->
            val subjectRecords = records.filter { it.subjectId == subject.id }
            val attended = subjectRecords.count { it.status == "PRESENT" }
            val missed = subjectRecords.count { it.status == "ABSENT" }
            val cancelled = subjectRecords.count { it.status == "CANCELLED" }
            val total = attended + missed
            val target = subject.targetAttendancePercent

            val percentage = if (total == 0) 100f else (attended.toFloat() / total.toFloat()) * 100f
            val isSafe = percentage >= target

            // Calculate safe bunks: how many classes can you miss and still stay >= target%?
            // (attended) / (total + x) >= target/100
            // attended * 100 / target >= total + x => x = floor(attended * 100 / target) - total
            val safeBunks = if (isSafe && total > 0) {
                val maxTotal = (attended * 100) / target
                val allowed = maxTotal - total
                if (allowed > 0) allowed else 0
            } else 0

            // Calculate required consecutive attendances to reach target%:
            // (attended + y) / (total + y) >= target/100
            // 100*attended + 100*y >= target*total + target*y
            // y * (100 - target) >= target*total - 100*attended
            val requiredConsecutive = if (!isSafe && total > 0) {
                val numerator = (target * total) - (100 * attended)
                val denominator = 100 - target
                if (denominator > 0 && numerator > 0) {
                    Math.ceil(numerator.toDouble() / denominator.toDouble()).toInt()
                } else 1
            } else 0

            SubjectAttendanceStats(
                subject = subject,
                attended = attended,
                missed = missed,
                cancelled = cancelled,
                totalConducted = total,
                percentage = percentage,
                safeBunks = safeBunks,
                requiredConsecutive = requiredConsecutive,
                isSafe = isSafe
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall stats
    val overallStats: StateFlow<OverallAttendanceStats> = subjectStats.combine(subjectStats) { stats, _ ->
        var totalAttended = 0
        var totalConducted = 0
        stats.forEach {
            totalAttended += it.attended
            totalConducted += it.totalConducted
        }
        val pct = if (totalConducted == 0) 100f else (totalAttended.toFloat() / totalConducted.toFloat()) * 100f
        val health = when {
            pct >= 85f -> HealthStatus.EXCELLENT
            pct >= 75f -> HealthStatus.SAFE
            pct >= 65f -> HealthStatus.WARNING
            else -> HealthStatus.DANGER
        }
        val msg = when (health) {
            HealthStatus.EXCELLENT -> "Outstanding! You are well above the 75% college requirement."
            HealthStatus.SAFE -> "Good standing. Keep attending regularly to maintain your eligibility."
            HealthStatus.WARNING -> "Caution! You are nearing the minimum 75% requirement. Avoid bunking."
            HealthStatus.DANGER -> "Critical Shortage! You are below the 75% threshold for exam hall ticket."
        }
        OverallAttendanceStats(
            totalAttended = totalAttended,
            totalConducted = totalConducted,
            overallPercentage = pct,
            health = health,
            statusMessage = msg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverallAttendanceStats(0, 0, 100f, HealthStatus.EXCELLENT, "Welcome to CampusMate!")
    )

    // Mark Attendance for subject and date
    fun markAttendance(subjectId: Long, date: String, status: String, notes: String = "") {
        viewModelScope.launch {
            repository.markAttendance(subjectId, date, status, notes)
        }
    }

    // Quick mark all today's subjects present
    fun markAllTodayPresent() {
        val today = _selectedDate.value
        val subjects = enrolledSubjects.value
        viewModelScope.launch {
            subjects.forEach { subject ->
                repository.markAttendance(subject.id, today, "PRESENT")
            }
        }
    }

    // Subject management
    fun addSubject(
        code: String,
        name: String,
        department: String,
        instructor: String,
        room: String,
        scheduleDays: String,
        credits: Int,
        targetPercent: Int = 75,
        isCurriculumDefault: Boolean = false
    ) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    code = code.trim().uppercase(),
                    name = name.trim(),
                    department = department.trim(),
                    instructor = instructor.trim(),
                    room = room.trim(),
                    scheduleDays = scheduleDays.trim(),
                    credits = credits,
                    targetAttendancePercent = targetPercent,
                    isCurriculumDefault = isCurriculumDefault,
                    isEnrolled = true
                )
            )
        }
    }

    fun removeSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun toggleEnrollment(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.toggleSubjectEnrollment(subject)
        }
    }

    // Notes management
    fun uploadNote(
        title: String,
        subjectCode: String,
        subjectName: String,
        authorName: String,
        isMyNote: Boolean,
        isVerifiedCurriculum: Boolean,
        content: String,
        keyPoints: String,
        attachmentName: String = ""
    ) {
        val today = dateFormat.format(Date())
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = title.trim(),
                    subjectCode = subjectCode.trim(),
                    subjectName = subjectName.trim(),
                    authorName = authorName.ifBlank { if (isMyNote) "Alex (Me)" else "Admin / Faculty" },
                    authorRole = if (isVerifiedCurriculum) "FACULTY_ADMIN" else if (isMyNote) "STUDENT" else "PEER",
                    isMyNote = isMyNote,
                    isVerifiedCurriculum = isVerifiedCurriculum,
                    content = content.trim(),
                    keyPoints = keyPoints.trim(),
                    attachmentName = attachmentName.ifBlank { "${title.take(15).replace(" ", "_")}.pdf" },
                    rating = 5.0f,
                    downloadCount = 1,
                    createdDate = today
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Assignment management
    fun addAssignment(
        title: String,
        subjectCode: String,
        subjectName: String,
        description: String,
        dueDate: String,
        isMyAssignment: Boolean,
        assignedBy: String,
        priority: String
    ) {
        viewModelScope.launch {
            repository.insertAssignment(
                AssignmentEntity(
                    title = title.trim(),
                    subjectCode = subjectCode.trim(),
                    subjectName = subjectName.trim(),
                    description = description.trim(),
                    dueDate = dueDate.trim(),
                    isMyAssignment = isMyAssignment,
                    isSubmitted = false,
                    assignedBy = assignedBy.trim(),
                    priority = priority
                )
            )
        }
    }

    fun submitAssignment(assignment: AssignmentEntity, submissionText: String) {
        viewModelScope.launch {
            repository.submitAssignment(assignment.id, assignment, submissionText)
        }
    }

    fun markAssignmentPending(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.markAssignmentPending(assignment)
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    // College Calendar Events
    fun addCollegeEvent(title: String, date: String, type: String, description: String) {
        viewModelScope.launch {
            repository.insertEvent(
                CollegeEventEntity(
                    title = title.trim(),
                    date = date.trim(),
                    type = type,
                    description = description.trim()
                )
            )
        }
    }

    fun deleteEvent(event: CollegeEventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
}
