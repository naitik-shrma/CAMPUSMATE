package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AssignmentEntity
import com.example.ui.theme.CollegeAmber
import com.example.ui.theme.CollegeGreen
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.CollegeRose
import com.example.ui.theme.CollegeTeal
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    viewModel: CollegeViewModel
) {
    val myAssignments by viewModel.myAssignments.collectAsStateWithLifecycle()
    val classAssignments by viewModel.classAssignments.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: My Assignments, 1: Class & Peer Assignments
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, SUBMITTED

    var showAddDialog by remember { mutableStateOf(false) }
    var assignmentToSubmit by remember { mutableStateOf<AssignmentEntity?>(null) }
    var selectedAssignmentDetail by remember { mutableStateOf<AssignmentEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentAssignments = if (selectedTabIndex == 0) myAssignments else classAssignments
    val filteredAssignments = currentAssignments.filter { assignment ->
        when (statusFilter) {
            "PENDING" -> !assignment.isSubmitted
            "SUBMITTED" -> assignment.isSubmitted
            else -> true
        }
    }

    val pendingCount = currentAssignments.count { !it.isSubmitted }
    val submittedCount = currentAssignments.count { it.isSubmitted }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Assignments & Submissions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Track Pending Tasks, Deadlines & Grades",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_assignment_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Assignment")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdminMode) "Post Class Task" else "Add Task",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Tabs: "My Assignments" vs "Class & Peer Assignments"
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "My Assignments (${myAssignments.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_my_assignments")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "Class & Peer Tasks (${classAssignments.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_class_assignments")
                )
            }

            // Status Filter Chips (All, Pending, Submitted)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { statusFilter = "ALL" },
                    label = { Text("All (${currentAssignments.size})") },
                    modifier = Modifier.testTag("filter_all_assignments")
                )
                FilterChip(
                    selected = statusFilter == "PENDING",
                    onClick = { statusFilter = "PENDING" },
                    label = { Text("Pending ($pendingCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = CollegeAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_pending_assignments")
                )
                FilterChip(
                    selected = statusFilter == "SUBMITTED",
                    onClick = { statusFilter = "SUBMITTED" },
                    label = { Text("Submitted ($submittedCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CollegeGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_submitted_assignments")
                )
            }

            // Assignment List
            if (filteredAssignments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No assignments in this category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You're either all caught up or no tasks are posted yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAssignments) { assignment ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAssignmentDetail = assignment }
                                .testTag("assignment_card_${assignment.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Subject, Due Date & Priority
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = assignment.subjectCode,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Status Pill
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (assignment.isSubmitted) CollegeGreen.copy(alpha = 0.15f) else CollegeAmber.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (assignment.isSubmitted) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                                    contentDescription = null,
                                                    tint = if (assignment.isSubmitted) CollegeGreen else CollegeAmber,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (assignment.isSubmitted) "Submitted" else "Pending",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (assignment.isSubmitted) CollegeGreen else CollegeAmber
                                                )
                                            }
                                        }
                                    }

                                    // Due Date
                                    Text(
                                        text = "Due: ${assignment.dueDate}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (assignment.isSubmitted) MaterialTheme.colorScheme.onSurfaceVariant else CollegeRose
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = assignment.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = assignment.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Submission details / Grade banner
                                if (assignment.isSubmitted) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CollegeGreen.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Submitted on ${assignment.submissionDate}",
                                                    fontSize = 11.sp,
                                                    color = CollegeGreen,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (assignment.submissionText.isNotBlank()) {
                                                    Text(
                                                        text = "Notes: ${assignment.submissionText}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            if (assignment.grade.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = CollegeGreen
                                                ) {
                                                    Text(
                                                        text = assignment.grade,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Bottom Row: Assigned By & Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = assignment.assignedBy,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (!assignment.isSubmitted) {
                                            Button(
                                                onClick = { assignmentToSubmit = assignment },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = CollegeGreen),
                                                modifier = Modifier.testTag("submit_assignment_button_${assignment.id}")
                                            ) {
                                                Text("Submit Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.markAssignmentPending(assignment) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Undo", fontSize = 10.sp)
                                            }
                                        }

                                        if (assignment.isMyAssignment || isAdminMode) {
                                            IconButton(
                                                onClick = { viewModel.deleteAssignment(assignment) },
                                                modifier = Modifier.size(28.dp).testTag("delete_assignment_${assignment.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit Assignment Dialog
    assignmentToSubmit?.let { assignment ->
        var submissionNotes by remember { mutableStateOf("") }
        var attachFileName by remember { mutableStateOf("${assignment.subjectCode}_Lab_Submission.pdf") }

        AlertDialog(
            onDismissRequest = { assignmentToSubmit = null },
            title = {
                Text("Submit Assignment", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Subject: ${assignment.subjectCode} - ${assignment.subjectName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = attachFileName,
                        onValueChange = { attachFileName = it },
                        label = { Text("Attached Document / Report File") },
                        modifier = Modifier.fillMaxWidth().testTag("submission_filename_input")
                    )

                    OutlinedTextField(
                        value = submissionNotes,
                        onValueChange = { submissionNotes = it },
                        label = { Text("Submission Notes / Answers") },
                        placeholder = { Text("Provide any remarks, links or solutions...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("submission_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = if (attachFileName.isNotBlank()) "$attachFileName: $submissionNotes" else submissionNotes
                        viewModel.submitAssignment(assignment, text)
                        assignmentToSubmit = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Assignment '${assignment.title}' submitted successfully!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CollegeGreen),
                    modifier = Modifier.testTag("confirm_submission_button")
                ) {
                    Text("Confirm Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { assignmentToSubmit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Assignment Dialog
    if (showAddDialog) {
        var assignTitle by remember { mutableStateOf("") }
        var selectedSubject by remember { mutableStateOf(allSubjects.firstOrNull()?.code ?: "BIO-301") }
        var assignDesc by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("2026-11-05") }
        var priority by remember { mutableStateOf("MEDIUM") }
        var isClassTask by remember { mutableStateOf(isAdminMode) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(if (isAdminMode) "Post Class Assignment" else "Add Personal Assignment", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = assignTitle,
                            onValueChange = { assignTitle = it },
                            label = { Text("Assignment Title") },
                            placeholder = { Text("e.g. Molecular Cloning Protocol Writeup") },
                            modifier = Modifier.fillMaxWidth().testTag("add_assignment_title_input")
                        )
                    }

                    item {
                        Text("Select Subject:", style = MaterialTheme.typography.labelSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(allSubjects) { sub ->
                                FilterChip(
                                    selected = selectedSubject == sub.code,
                                    onClick = { selectedSubject = sub.code },
                                    label = { Text(sub.code) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = assignDesc,
                            onValueChange = { assignDesc = it },
                            label = { Text("Instructions & Description") },
                            placeholder = { Text("Enter prompt, guidelines, and rubrics...") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("add_assignment_desc_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_assignment_due_date_input")
                        )
                    }

                    item {
                        Text("Priority:", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (assignTitle.isNotBlank()) {
                            val sub = allSubjects.find { it.code == selectedSubject }
                            viewModel.addAssignment(
                                title = assignTitle,
                                subjectCode = selectedSubject,
                                subjectName = sub?.name ?: selectedSubject,
                                description = assignDesc,
                                dueDate = dueDate,
                                isMyAssignment = !isClassTask,
                                assignedBy = if (isAdminMode) "Dean / Admin" else "Alex (Self)",
                                priority = priority
                            )
                            showAddDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Assignment created successfully!")
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_assignment_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Detail Dialog
    selectedAssignmentDetail?.let { assignment ->
        AlertDialog(
            onDismissRequest = { selectedAssignmentDetail = null },
            title = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = assignment.subjectCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(assignment.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Due: ${assignment.dueDate} • Assigned by: ${assignment.assignedBy}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(assignment.description, style = MaterialTheme.typography.bodyMedium)

                    if (assignment.isSubmitted) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CollegeGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Status: Submitted (${assignment.submissionDate})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeGreen
                                )
                                if (assignment.submissionText.isNotBlank()) {
                                    Text(text = assignment.submissionText, fontSize = 11.sp)
                                }
                                if (assignment.grade.isNotBlank()) {
                                    Text(
                                        text = "Grade: ${assignment.grade}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollegeGreen
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!assignment.isSubmitted) {
                    Button(onClick = {
                        assignmentToSubmit = assignment
                        selectedAssignmentDetail = null
                    }) {
                        Text("Submit Assignment")
                    }
                } else {
                    TextButton(onClick = { selectedAssignmentDetail = null }) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (!assignment.isSubmitted) {
                    TextButton(onClick = { selectedAssignmentDetail = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}
