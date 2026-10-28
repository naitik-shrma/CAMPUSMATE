package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AssignmentEntity
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.NoteEntity
import com.example.data.model.SubjectEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CampusMate", appName)
    }

    @Test
    fun `test attendance percentage calculation`() {
        val attended = 15
        val missed = 5
        val total = attended + missed
        val percentage = (attended.toFloat() / total.toFloat()) * 100f
        assertEquals(75.0f, percentage, 0.01f)
        assertTrue("Percentage should meet 75% target", percentage >= 75f)
    }

    @Test
    fun `test safe bunks calculation`() {
        val attended = 18
        val total = 20
        val target = 75 // 75%
        // (18 / (20 + x)) >= 0.75 => 18 / 0.75 >= 20 + x => 24 >= 20 + x => x = 4
        val maxTotal = (attended * 100) / target
        val allowedBunks = maxTotal - total
        assertEquals(4, allowedBunks)
    }

    @Test
    fun `test assignment submission state`() {
        val assignment = AssignmentEntity(
            id = 1,
            title = "CRISPR-Cas9 Analysis",
            subjectCode = "BIO-301",
            subjectName = "Molecular Biology",
            description = "Lab report writeup",
            dueDate = "2026-11-05",
            isMyAssignment = true,
            isSubmitted = false,
            assignedBy = "Dr. Vance",
            priority = "HIGH"
        )
        assertFalse(assignment.isSubmitted)

        val submitted = assignment.copy(
            isSubmitted = true,
            submissionDate = "2026-11-04 14:00",
            submissionText = "Uploaded lab report PDF."
        )
        assertTrue(submitted.isSubmitted)
        assertEquals("2026-11-04 14:00", submitted.submissionDate)
    }

    @Test
    fun `test notes separation my vs peer notes`() {
        val myNote = NoteEntity(
            id = 1,
            title = "Enzyme Kinetics",
            subjectCode = "BIO-305",
            subjectName = "Biochemistry",
            authorName = "Alex (Me)",
            authorRole = "STUDENT",
            isMyNote = true,
            content = "Michaelis Menten notes",
            keyPoints = "Km and Vmax",
            createdDate = "2026-10-25"
        )

        val peerNote = NoteEntity(
            id = 2,
            title = "Official Unit 3 Notes",
            subjectCode = "BIO-301",
            subjectName = "Molecular Biology",
            authorName = "Dr. Vance (Admin)",
            authorRole = "FACULTY_ADMIN",
            isMyNote = false,
            isVerifiedCurriculum = true,
            content = "Curriculum slides",
            keyPoints = "Exam prep",
            createdDate = "2026-10-20"
        )

        assertTrue(myNote.isMyNote)
        assertFalse(peerNote.isMyNote)
        assertTrue(peerNote.isVerifiedCurriculum)
    }
}
