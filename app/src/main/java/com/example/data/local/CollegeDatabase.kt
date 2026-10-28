package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AssignmentEntity
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.CollegeEventEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        AttendanceRecordEntity::class,
        NoteEntity::class,
        AssignmentEntity::class,
        CollegeEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CollegeDatabase : RoomDatabase() {
    abstract fun collegeDao(): CollegeDao

    companion object {
        @Volatile
        private var INSTANCE: CollegeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CollegeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CollegeDatabase::class.java,
                    "campusmate_database"
                )
                .addCallback(CollegeDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class CollegeDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.collegeDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: CollegeDao) {
            // Pre-seed Subjects (Core STEM & Biosciences)
            val subjects = listOf(
                SubjectEntity(
                    id = 1,
                    code = "BIO-301",
                    name = "Molecular Biology & Genetics",
                    department = "Biosciences",
                    instructor = "Dr. Eleanor Vance",
                    room = "BioTech Hall 402",
                    scheduleDays = "Mon, Wed, Fri",
                    credits = 4,
                    targetAttendancePercent = 75,
                    isCurriculumDefault = true,
                    isEnrolled = true
                ),
                SubjectEntity(
                    id = 2,
                    code = "BIO-305",
                    name = "Biochemistry & Enzymology",
                    department = "Biosciences",
                    instructor = "Prof. Marcus Thorne",
                    room = "Science Annex Lab 3",
                    scheduleDays = "Tue, Thu",
                    credits = 4,
                    targetAttendancePercent = 80,
                    isCurriculumDefault = true,
                    isEnrolled = true
                ),
                SubjectEntity(
                    id = 3,
                    code = "CS-204",
                    name = "Data Structures & Algorithms",
                    department = "Computer Science",
                    instructor = "Prof. Alan Turing Jr.",
                    room = "Turing Lab 101",
                    scheduleDays = "Mon, Wed",
                    credits = 3,
                    targetAttendancePercent = 75,
                    isCurriculumDefault = true,
                    isEnrolled = true
                ),
                SubjectEntity(
                    id = 4,
                    code = "BIO-210",
                    name = "Cellular Physiology & Immunology",
                    department = "Biosciences",
                    instructor = "Dr. Maya Lin",
                    room = "Life Sciences 208",
                    scheduleDays = "Tue, Fri",
                    credits = 3,
                    targetAttendancePercent = 75,
                    isCurriculumDefault = true,
                    isEnrolled = true
                ),
                SubjectEntity(
                    id = 5,
                    code = "CHEM-202",
                    name = "Organic Chemistry & Biophysics",
                    department = "Chemistry",
                    instructor = "Dr. Robert Patel",
                    room = "Chem Auditorium B",
                    scheduleDays = "Wed, Thu",
                    credits = 3,
                    targetAttendancePercent = 75,
                    isCurriculumDefault = true,
                    isEnrolled = true
                )
            )
            dao.insertSubjects(subjects)

            // Pre-seed Attendance Records
            val attendanceRecords = listOf(
                AttendanceRecordEntity(subjectId = 1, date = "2026-10-21", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 1, date = "2026-10-23", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 1, date = "2026-10-26", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 1, date = "2026-10-28", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 1, date = "2026-10-19", status = "ABSENT"),

                AttendanceRecordEntity(subjectId = 2, date = "2026-10-20", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 2, date = "2026-10-22", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 2, date = "2026-10-27", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 2, date = "2026-10-15", status = "ABSENT"),

                AttendanceRecordEntity(subjectId = 3, date = "2026-10-21", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 3, date = "2026-10-26", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 3, date = "2026-10-28", status = "PRESENT"),

                AttendanceRecordEntity(subjectId = 4, date = "2026-10-20", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 4, date = "2026-10-23", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 4, date = "2026-10-27", status = "ABSENT"),

                AttendanceRecordEntity(subjectId = 5, date = "2026-10-21", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 5, date = "2026-10-22", status = "PRESENT"),
                AttendanceRecordEntity(subjectId = 5, date = "2026-10-28", status = "PRESENT")
            )
            dao.insertAttendanceRecords(attendanceRecords)

            // Pre-seed Notes ("My Notes" and "Peer / Admin Notes")
            val notes = listOf(
                // My Notes
                NoteEntity(
                    title = "PCR Amplification & Gel Electrophoresis Protocol",
                    subjectCode = "BIO-301",
                    subjectName = "Molecular Biology & Genetics",
                    authorName = "Alex (Me)",
                    authorRole = "STUDENT",
                    isMyNote = true,
                    isVerifiedCurriculum = false,
                    content = "Detailed laboratory notes on thermal cycling parameters (94°C Denaturation, 55°C Annealing, 72°C Extension) and agarose gel analysis for DNA fragment sizing.",
                    keyPoints = "• 30 cycles standard\n• Ethidium bromide / GelRed dye concentration\n• TAE vs TBE buffer conductivity differences",
                    attachmentName = "PCR_Lab_Protocol_Alex.pdf",
                    rating = 4.9f,
                    downloadCount = 14,
                    createdDate = "2026-10-24"
                ),
                NoteEntity(
                    title = "Enzyme Inhibition & Lineweaver-Burk Graphs",
                    subjectCode = "BIO-305",
                    subjectName = "Biochemistry & Enzymology",
                    authorName = "Alex (Me)",
                    authorRole = "STUDENT",
                    isMyNote = true,
                    isVerifiedCurriculum = false,
                    content = "Formulas and graphical comparisons for Competitive, Non-Competitive, and Uncompetitive enzyme inhibition patterns with Michaelis-Menten constants.",
                    keyPoints = "• Competitive: Km increases, Vmax unchanged\n• Non-competitive: Vmax decreases, Km unchanged\n• Uncompetitive: both Km and Vmax decrease proportionally",
                    attachmentName = "Enzyme_Kinetics_Notes.docx",
                    rating = 4.8f,
                    downloadCount = 9,
                    createdDate = "2026-10-25"
                ),
                // Peer & Admin Notes
                NoteEntity(
                    title = "Official Unit 3: CRISPR-Cas9 & Gene Editing Mechanisms",
                    subjectCode = "BIO-301",
                    subjectName = "Molecular Biology & Genetics",
                    authorName = "Dr. Eleanor Vance (Dean / Admin)",
                    authorRole = "FACULTY_ADMIN",
                    isMyNote = false,
                    isVerifiedCurriculum = true,
                    content = "Authorized departmental lecture slides and review sheets on guide RNA architecture, PAM sequences (NGG), Cas9 endonuclease cleavage, and Non-Homologous End Joining (NHEJ) vs HDR.",
                    keyPoints = "• Official Mid-Term exam syllabus coverage\n• Target recognition via protospacer adjacent motif\n• Ethics and therapeutic applications in sickle cell anemia",
                    attachmentName = "Unit3_CRISPR_Official_Syllabus.pdf",
                    rating = 5.0f,
                    downloadCount = 142,
                    createdDate = "2026-10-20"
                ),
                NoteEntity(
                    title = "Krebs Cycle & ATP Yield Summary Sheets",
                    subjectCode = "BIO-305",
                    subjectName = "Biochemistry & Enzymology",
                    authorName = "Sophia Martinez",
                    authorRole = "PEER",
                    isMyNote = false,
                    isVerifiedCurriculum = false,
                    content = "Handmade colorful summary flowchart detailing every metabolic intermediate from Citrate to Oxaloacetate with net NADH, FADH2, and GTP calculations.",
                    keyPoints = "• Net yield per glucose: 2 ATP, 6 NADH, 2 FADH2, 4 CO2\n• Key regulatory enzymes: Isocitrate dehydrogenase, Alpha-ketoglutarate dehydrogenase",
                    attachmentName = "Krebs_Summary_Handdrawn.pdf",
                    rating = 4.9f,
                    downloadCount = 88,
                    createdDate = "2026-10-22"
                ),
                NoteEntity(
                    title = "Binary Search Trees & AVL Rotations Guide",
                    subjectCode = "CS-204",
                    subjectName = "Data Structures & Algorithms",
                    authorName = "Kevin Zhao",
                    authorRole = "PEER",
                    isMyNote = false,
                    isVerifiedCurriculum = false,
                    content = "Step-by-step visual balancing rules for Left-Left, Right-Right, Left-Right, and Right-Left AVL tree rotations with code snippets.",
                    keyPoints = "• Balance factor must remain -1, 0, or +1\n• O(log N) lookup and insertion guarantees",
                    attachmentName = "AVL_Tree_Rotations_Explained.pdf",
                    rating = 4.7f,
                    downloadCount = 65,
                    createdDate = "2026-10-18"
                )
            )
            dao.insertNotes(notes)

            // Pre-seed Assignments (Pending & Submitted, My vs Class)
            val assignments = listOf(
                // My Assignments - Pending
                AssignmentEntity(
                    title = "Bioscience Lab Report: DNA Extraction from Onion Tissue",
                    subjectCode = "BIO-301",
                    subjectName = "Molecular Biology & Genetics",
                    description = "Submit formal scientific write-up following standard lab report format (Hypothesis, Materials, Procedure, Gel Band Analysis, and Discussion).",
                    dueDate = "2026-10-31",
                    isMyAssignment = true,
                    isSubmitted = false,
                    assignedBy = "Dr. Eleanor Vance (Faculty)",
                    priority = "HIGH"
                ),
                AssignmentEntity(
                    title = "Lineweaver-Burk Curve Fitting Problem Set",
                    subjectCode = "BIO-305",
                    subjectName = "Biochemistry & Enzymology",
                    description = "Solve questions 1 through 8 calculating Km and Vmax with double-reciprocal plots under presence of competitive inhibitors.",
                    dueDate = "2026-11-04",
                    isMyAssignment = true,
                    isSubmitted = false,
                    assignedBy = "Prof. Marcus Thorne",
                    priority = "MEDIUM"
                ),
                // My Assignments - Submitted
                AssignmentEntity(
                    title = "Cellular Mitosis vs Meiosis Comparative Essay",
                    subjectCode = "BIO-210",
                    subjectName = "Cellular Physiology & Immunology",
                    description = "1500-word essay evaluating evolutionary advantages of homologous recombination and crossing over during Prophase I.",
                    dueDate = "2026-10-25",
                    isMyAssignment = true,
                    isSubmitted = true,
                    submissionDate = "2026-10-24 16:30",
                    submissionText = "Completed and submitted via student portal. Included comparative karyotype analysis.",
                    grade = "A (94/100)",
                    assignedBy = "Dr. Maya Lin",
                    priority = "LOW"
                ),
                // Peer & Class Assignments (Curriculum posted by Faculty/Admin)
                AssignmentEntity(
                    title = "Departmental Project: Synthetic Biology Proposal",
                    subjectCode = "BIO-301",
                    subjectName = "Molecular Biology & Genetics",
                    description = "Group project designing an engineered microbial pathway for sustainable bioplastic degradation.",
                    dueDate = "2026-11-15",
                    isMyAssignment = false,
                    isSubmitted = false,
                    assignedBy = "Dean Academic Affairs (Admin)",
                    priority = "HIGH"
                ),
                AssignmentEntity(
                    title = "Red-Black Tree Implementation Benchmark",
                    subjectCode = "CS-204",
                    subjectName = "Data Structures & Algorithms",
                    description = "Implement custom Red-Black Tree in Kotlin or Java and benchmark versus standard Library TreeSet with 100,000 random elements.",
                    dueDate = "2026-11-08",
                    isMyAssignment = false,
                    isSubmitted = false,
                    assignedBy = "Prof. Alan Turing Jr.",
                    priority = "MEDIUM"
                )
            )
            dao.insertAssignments(assignments)

            // Pre-seed College Calendar Events
            val events = listOf(
                CollegeEventEntity(
                    title = "Bioscience Annual Symposium 2026",
                    date = "2026-10-29",
                    type = "SYMPOSIUM",
                    description = "Keynote address by Nobel Laureate on RNA Therapeutics. Attendance awarded 2 bonus credits."
                ),
                CollegeEventEntity(
                    title = "Mid-Semester Examinations Commence",
                    date = "2026-11-09",
                    type = "EXAM",
                    description = "Centralized college exams. 75% attendance threshold mandatory for hall ticket eligibility."
                ),
                CollegeEventEntity(
                    title = "Fall Reading & Study Recess",
                    date = "2026-11-12",
                    type = "HOLIDAY",
                    description = "Campus closed for revision and library open 24/7."
                )
            )
            dao.insertEvents(events)
        }
    }
}
