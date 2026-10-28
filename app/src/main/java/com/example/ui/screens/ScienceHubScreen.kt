package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CollegeAmber
import com.example.ui.theme.CollegeGreen
import com.example.ui.theme.CollegeNavy
import com.example.ui.theme.CollegeRose
import com.example.ui.theme.CollegeTeal
import com.example.ui.viewmodel.CollegeViewModel
import kotlinx.coroutines.launch

data class ScienceFlashcard(
    val id: Int,
    val topic: String,
    val question: String,
    val answer: String,
    val keyTakeaway: String
)

data class ScienceQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScienceHubScreen(
    viewModel: CollegeViewModel
) {
    var selectedSectionIndex by remember { mutableIntStateOf(0) } // 0: Flashcards, 1: Lab Report Assistant, 2: Pathways, 3: Quiz

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Flashcards Data
    val flashcards = remember {
        listOf(
            ScienceFlashcard(
                id = 1,
                topic = "Molecular Biology",
                question = "What is the function of the PAM sequence (5'-NGG-3') in CRISPR-Cas9 genome editing?",
                answer = "The Protospacer Adjacent Motif (PAM) is a 2-6 base pair sequence immediately following the target DNA sequence. It is mandatory for Cas9 binding; without PAM recognition, Cas9 will not cleave the target DNA.",
                keyTakeaway = "Ensures self vs non-self distinction and prevents cutting of the CRISPR bacterial array."
            ),
            ScienceFlashcard(
                id = 2,
                topic = "Biochemistry",
                question = "How do Competitive vs Non-Competitive Enzyme Inhibitors affect Km and Vmax in Lineweaver-Burk plots?",
                answer = "Competitive Inhibitors bind the active site: Km INCREASES (lower affinity), Vmax is UNCHANGED.\n\nNon-Competitive Inhibitors bind an allosteric site: Km is UNCHANGED, Vmax DECREASES.",
                keyTakeaway = "High substrate concentration overcomes competitive inhibition, but cannot overcome non-competitive."
            ),
            ScienceFlashcard(
                id = 3,
                topic = "Cellular Respiration",
                question = "What is the net yield of ATP, NADH, and FADH2 per molecule of glucose oxidized through Glycolysis and the Krebs Cycle?",
                answer = "Net Glycolysis: 2 ATP + 2 NADH.\nPyruvate Oxidation: 2 NADH.\nKrebs Cycle (2 turns per glucose): 2 ATP (or GTP), 6 NADH, 2 FADH2.\nTotal net: 4 ATP directly + 10 NADH + 2 FADH2 feeding Oxidative Phosphorylation (~30-32 total ATP).",
                keyTakeaway = "Oxygen acts as the final electron acceptor at Complex IV of the ETC, forming water."
            ),
            ScienceFlashcard(
                id = 4,
                topic = "Genetics & Replication",
                question = "Why do Okazaki fragments form on the lagging strand during DNA replication?",
                answer = "DNA Polymerase III can only synthesize in the 5' to 3' direction. Because the replication fork unzips antiparallel DNA, the lagging strand template runs 5' to 3', requiring discontinuous synthesis of short Okazaki fragments sealed by DNA Ligase.",
                keyTakeaway = "RNA primers synthesized by Primase initiate each Okazaki fragment."
            )
        )
    }

    var currentFlashcardIndex by remember { mutableIntStateOf(0) }
    var isFlashcardFlipped by remember { mutableStateOf(false) }

    // Quiz Questions Data
    val quizQuestions = remember {
        listOf(
            ScienceQuizQuestion(
                id = 1,
                question = "Which enzyme unwinds the double helix at the replication fork?",
                options = listOf("DNA Polymerase I", "DNA Helicase", "Topoisomerase", "RNA Primase"),
                correctIndex = 1,
                explanation = "DNA Helicase breaks the hydrogen bonds between nitrogenous bases to unzip the two strands."
            ),
            ScienceQuizQuestion(
                id = 2,
                question = "In the Krebs cycle, what 4-carbon compound condenses with Acetyl-CoA (2C) to form Citrate (6C)?",
                options = listOf("Succinate", "Malate", "Oxaloacetate", "Fumarate"),
                correctIndex = 2,
                explanation = "Oxaloacetate (4C) reacts with Acetyl-CoA catalyzed by Citrate Synthase to generate Citrate."
            ),
            ScienceQuizQuestion(
                id = 3,
                question = "What is the primary role of SDS in SDS-PAGE protein electrophoresis?",
                options = listOf(
                    "Stains protein bands blue",
                    "Denatures proteins and coats them with uniform negative charge",
                    "Acts as a gel buffer only",
                    "Catalyzes peptide bond formation"
                ),
                correctIndex = 1,
                explanation = "SDS is an anionic detergent that linearizes proteins and confers uniform negative charge per mass."
            )
        )
    }

    var quizCurrentIndex by remember { mutableIntStateOf(0) }
    var quizSelectedAnswer by remember { mutableStateOf<Int?>(null) }
    var quizScore by remember { mutableIntStateOf(0) }
    var quizAnswerSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CollegeTeal,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bioscience Skills Suite",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Study Flashcards, Lab Report Builder & Concepts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedSectionIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CollegeTeal
            ) {
                Tab(
                    selected = selectedSectionIndex == 0,
                    onClick = { selectedSectionIndex = 0 },
                    text = { Text("Flashcards", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("science_tab_flashcards")
                )
                Tab(
                    selected = selectedSectionIndex == 1,
                    onClick = { selectedSectionIndex = 1 },
                    text = { Text("Lab Report", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("science_tab_lab_report")
                )
                Tab(
                    selected = selectedSectionIndex == 2,
                    onClick = { selectedSectionIndex = 2 },
                    text = { Text("Pathways", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("science_tab_pathways")
                )
                Tab(
                    selected = selectedSectionIndex == 3,
                    onClick = { selectedSectionIndex = 3 },
                    text = { Text("Quiz", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("science_tab_quiz")
                )
            }

            // Body Content based on tab
            when (selectedSectionIndex) {
                0 -> {
                    // Flashcards View
                    val card = flashcards[currentFlashcardIndex]
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Card ${currentFlashcardIndex + 1} of ${flashcards.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CollegeTeal.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = card.topic,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollegeTeal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!isFlashcardFlipped) MaterialTheme.colorScheme.surface else CollegeNavy.copy(alpha = 0.08f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isFlashcardFlipped = !isFlashcardFlipped }
                                    .testTag("flashcard_interactive_box")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (!isFlashcardFlipped) Icons.Default.HelpOutline else Icons.Default.Biotech,
                                            contentDescription = null,
                                            tint = if (!isFlashcardFlipped) MaterialTheme.colorScheme.primary else CollegeTeal,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (!isFlashcardFlipped) "QUESTION" else "ANSWER & MECHANISM",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isFlashcardFlipped) MaterialTheme.colorScheme.primary else CollegeTeal
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = if (!isFlashcardFlipped) card.question else card.answer,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = if (!isFlashcardFlipped) 17.sp else 15.sp,
                                            fontWeight = if (!isFlashcardFlipped) FontWeight.SemiBold else FontWeight.Normal,
                                            lineHeight = 24.sp
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (isFlashcardFlipped) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = CollegeAmber.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = CollegeAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Exam Tip: ${card.keyTakeaway}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF92400E),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "Tap card to flip between Question and Answer",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        isFlashcardFlipped = false
                                        currentFlashcardIndex = (currentFlashcardIndex - 1 + flashcards.size) % flashcards.size
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Previous")
                                }

                                Button(
                                    onClick = { isFlashcardFlipped = !isFlashcardFlipped },
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeTeal),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("flip_card_button")
                                ) {
                                    Icon(imageVector = Icons.Default.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Flip Card")
                                }

                                Button(
                                    onClick = {
                                        isFlashcardFlipped = false
                                        currentFlashcardIndex = (currentFlashcardIndex + 1) % flashcards.size
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("next_card_button")
                                ) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Lab Report Builder View
                    var reportTitle by remember { mutableStateOf("Plasmid DNA Isolation & Spectrophotometry Assay") }
                    var hypothesis by remember { mutableStateOf("Alkaline lysis will release intact circular plasmid DNA while precipitating genomic DNA and cellular debris.") }
                    var materials by remember { mutableStateOf("E. coli culture, Resuspension Buffer (P1), Lysis Solution (P2), Neutralization Buffer (P3), Spin Columns, UV-Vis NanoDrop spectrophotometer.") }
                    var procedure by remember { mutableStateOf("1. Centrifuge 5ml bacterial culture at 6000 rpm for 5 min.\n2. Resuspend pellet in 250µl P1 buffer with RNase A.\n3. Add 250µl P2 lysis buffer and gently invert 4-6 times.\n4. Add 350µl P3 neutralization buffer, centrifuge 10 min at 13,000 rpm.\n5. Transfer supernatant to silica spin column, wash with PE buffer, and elute in 50µl EB.") }
                    var results by remember { mutableStateOf("Absorbance A260/A280 ratio measured: 1.84 (indicates pure DNA without protein contamination). Yield: 142 ng/µL.") }
                    var conclusion by remember { mutableStateOf("Alkaline miniprep yielded high-purity plasmid DNA verified by UV absorbance; suitable for downstream restriction digestion and PCR.") }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CollegeTeal.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Biotech, contentDescription = null, tint = CollegeTeal)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Standard Bioscience Lab Report Template", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("Format ready for college coursework submission and notes archive.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = reportTitle,
                                onValueChange = { reportTitle = it },
                                label = { Text("Experiment Title") },
                                modifier = Modifier.fillMaxWidth().testTag("lab_report_title_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = hypothesis,
                                onValueChange = { hypothesis = it },
                                label = { Text("Hypothesis & Objectives") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = materials,
                                onValueChange = { materials = it },
                                label = { Text("Materials & Reagents") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = procedure,
                                onValueChange = { procedure = it },
                                label = { Text("Standard Procedure / Protocol") },
                                minLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = results,
                                onValueChange = { results = it },
                                label = { Text("Observations, Data & Gel Banding") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = conclusion,
                                onValueChange = { conclusion = it },
                                label = { Text("Discussion & Scientific Conclusion") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    val summaryText = "Hypothesis: $hypothesis\n\nProcedure:\n$procedure\n\nResults:\n$results\n\nConclusion:\n$conclusion"
                                    val keyPointsText = "• A260/A280 purity analysis\n• Protocol verified with negative control"
                                    viewModel.uploadNote(
                                        title = "Lab Report: $reportTitle",
                                        subjectCode = "BIO-301",
                                        subjectName = "Molecular Biology & Genetics",
                                        authorName = "Alex (Student Lab)",
                                        isMyNote = true,
                                        isVerifiedCurriculum = false,
                                        content = summaryText,
                                        keyPoints = keyPointsText,
                                        attachmentName = "${reportTitle.take(12).replace(" ", "_")}_LabReport.pdf"
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Lab report saved directly to 'My Notes'!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CollegeTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("save_lab_report_to_notes_button")
                            ) {
                                Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Lab Report to My Notes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                2 -> {
                    // Pathways & Concepts Explorer
                    val pathways = listOf(
                        Triple(
                            "Glycolysis (EMP Pathway)",
                            "Cytosol",
                            "1 Glucose (6C) -> 2 Pyruvate (3C)\n• Rate Limiting Step: Phosphofructokinase-1 (PFK-1) converting Fructose-6-P to Fructose-1,6-bisP.\n• Net ATP: 2 ATP + 2 NADH.\n• Substrate-level phosphorylation catalyzed by Phosphoglycerate Kinase & Pyruvate Kinase."
                        ),
                        Triple(
                            "Polymerase Chain Reaction (PCR)",
                            "Thermal Cycler",
                            "3 Distinct Temperature Phases:\n1. Denaturation (94-98°C): Breaks hydrogen bonds between DNA strands.\n2. Annealing (50-65°C): Primers bind complementary sequences.\n3. Extension (72°C): Taq Polymerase synthesizes new strand.\n• Yield: 2^n exponential copies."
                        ),
                        Triple(
                            "Enzyme Inhibition Models",
                            "Enzymology",
                            "• Competitive: Inhibitor resembles substrate; overcomes with excess substrate. Km increases; Vmax constant.\n• Noncompetitive: Binds allosteric site; reduces turnover. Vmax decreases; Km constant.\n• Uncompetitive: Binds only ES complex; both Km and Vmax decrease proportionally."
                        )
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(pathways) { (name, location, details) ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CollegeNavy.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = location,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CollegeNavy,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = details,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Quiz Mode
                    val question = quizQuestions[quizCurrentIndex]

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Question ${quizCurrentIndex + 1} of ${quizQuestions.size}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "Score: $quizScore",
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeGreen
                                )
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = question.question,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    question.options.forEachIndexed { index, option ->
                                        val isSelected = quizSelectedAnswer == index
                                        val isCorrect = index == question.correctIndex
                                        val color = when {
                                            !quizAnswerSubmitted -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            isCorrect -> CollegeGreen.copy(alpha = 0.2f)
                                            isSelected && !isCorrect -> CollegeRose.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = color,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable(enabled = !quizAnswerSubmitted) {
                                                    quizSelectedAnswer = index
                                                }
                                                .testTag("quiz_option_$index")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = ('A' + index).toString(),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(text = option, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    if (quizAnswerSubmitted) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Explanation:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(question.explanation, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            if (!quizAnswerSubmitted) {
                                Button(
                                    onClick = {
                                        if (quizSelectedAnswer != null) {
                                            quizAnswerSubmitted = true
                                            if (quizSelectedAnswer == question.correctIndex) {
                                                quizScore += 1
                                            }
                                        }
                                    },
                                    enabled = quizSelectedAnswer != null,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("submit_quiz_answer_button")
                                ) {
                                    Text("Check Answer")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (quizCurrentIndex + 1 < quizQuestions.size) {
                                            quizCurrentIndex += 1
                                            quizSelectedAnswer = null
                                            quizAnswerSubmitted = false
                                        } else {
                                            // Reset quiz
                                            quizCurrentIndex = 0
                                            quizSelectedAnswer = null
                                            quizAnswerSubmitted = false
                                            quizScore = 0
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("next_quiz_question_button")
                                ) {
                                    Text(if (quizCurrentIndex + 1 < quizQuestions.size) "Next Question" else "Restart Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
