package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.SubjectEntity
import com.example.ui.theme.CollegeAmber
import com.example.ui.theme.CollegeGreen
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.CollegeTeal
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    viewModel: CollegeViewModel,
    onNavigateBack: () -> Unit
) {
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    var selectedDeptFilter by remember { mutableStateOf("ALL") }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val departments = remember(allSubjects) {
        listOf("ALL") + allSubjects.map { it.department }.distinct()
    }

    val filteredSubjects = allSubjects.filter {
        selectedDeptFilter == "ALL" || it.department == selectedDeptFilter
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "College Subjects Catalog",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Curriculum Courses & Enrollment Management",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_from_subjects_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSubjectDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_subject_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdminMode) "Add Curriculum Course" else "Add Custom Subject",
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
            // Department Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(departments) { dept ->
                    FilterChip(
                        selected = selectedDeptFilter == dept,
                        onClick = { selectedDeptFilter = dept },
                        label = { Text(if (dept == "ALL") "All Departments" else dept) }
                    )
                }
            }

            // Subjects list
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSubjects) { subject ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("subject_catalog_card_${subject.code}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            text = subject.code,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${subject.credits} Credits",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Enrolled Switch
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (subject.isEnrolled) "Enrolled" else "Dropped",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (subject.isEnrolled) CollegeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = subject.isEnrolled,
                                        onCheckedChange = {
                                            viewModel.toggleEnrollment(subject)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (!subject.isEnrolled) "Enrolled in ${subject.code}" else "Dropped ${subject.code} from tracking"
                                                )
                                            }
                                        },
                                        modifier = Modifier.testTag("enroll_switch_${subject.code}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Dept: ${subject.department} • Instructor: ${subject.instructor}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Room: ${subject.room} • Days: ${subject.scheduleDays}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CollegeNavy.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "Target Attendance: ${subject.targetAttendancePercent}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollegeNavy,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                if (isAdminMode || !subject.isCurriculumDefault) {
                                    IconButton(
                                        onClick = {
                                            viewModel.removeSubject(subject)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Removed subject ${subject.code}")
                                            }
                                        },
                                        modifier = Modifier.size(32.dp).testTag("delete_subject_${subject.code}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Subject",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
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

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var code by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Biosciences") }
        var instructor by remember { mutableStateOf("") }
        var room by remember { mutableStateOf("Hall 101") }
        var scheduleDays by remember { mutableStateOf("Mon, Wed, Fri") }
        var credits by remember { mutableIntStateOf(3) }
        var targetPercent by remember { mutableIntStateOf(75) }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = {
                Text(if (isAdminMode) "Add Curriculum Subject" else "Add New Subject", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text("Subject Code") },
                            placeholder = { Text("e.g. BIO-310") },
                            modifier = Modifier.fillMaxWidth().testTag("add_subject_code_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Subject Full Name") },
                            placeholder = { Text("e.g. Immunology & Serology") },
                            modifier = Modifier.fillMaxWidth().testTag("add_subject_name_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = dept,
                            onValueChange = { dept = it },
                            label = { Text("Department") },
                            placeholder = { Text("e.g. Biosciences, CS, Math") },
                            modifier = Modifier.fillMaxWidth().testTag("add_subject_dept_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = instructor,
                            onValueChange = { instructor = it },
                            label = { Text("Professor / Instructor") },
                            placeholder = { Text("e.g. Dr. Emily Hayes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = room,
                                onValueChange = { room = it },
                                label = { Text("Room / Lab") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = scheduleDays,
                                onValueChange = { scheduleDays = it },
                                label = { Text("Days") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = credits.toString(),
                                onValueChange = { credits = it.toIntOrNull() ?: 3 },
                                label = { Text("Credits") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = targetPercent.toString(),
                                onValueChange = { targetPercent = it.toIntOrNull() ?: 75 },
                                label = { Text("Target %") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank() && name.isNotBlank()) {
                            viewModel.addSubject(
                                code = code,
                                name = name,
                                department = dept,
                                instructor = instructor.ifBlank { "Faculty Staff" },
                                room = room,
                                scheduleDays = scheduleDays,
                                credits = credits,
                                targetPercent = targetPercent,
                                isCurriculumDefault = isAdminMode
                            )
                            showAddSubjectDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Subject $code added to catalog!")
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_subject_button")
                ) {
                    Text("Add Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
