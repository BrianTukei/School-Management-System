@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Grade
import com.example.data.entity.Student
import com.example.ui.SchoolViewModel
import java.util.Locale

@Composable
fun PerformanceTrendsScreen(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    
    // Live database stats
    val students by viewModel.students.collectAsStateWithLifecycle(emptyList())
    val grades by viewModel.allGrades.collectAsStateWithLifecycle(emptyList())

    // UI Configuration States
    var selectedStudentId by remember { mutableStateOf<Int?>(null) } // null means "All Students (School Average)"
    var showStudentDropdown by remember { mutableStateOf(false) }

    // Active subjects filter
    val availableSubjects = listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")
    val subjectColors = mapOf(
        "Mathematics" to Color(0xFF3F51B5),       // Indigo
        "English Language" to Color(0xFFE91E63),   // Deep Pink
        "Integrated Science" to Color(0xFF009688), // Teal
        "Social Studies" to Color(0xFFFF9800)      // Amber
    )
    val activeSubjectsState = remember { mutableStateMapOf<String, Boolean>() }
    
    // Initialize active states once
    LaunchedEffect(Unit) {
        availableSubjects.forEach { sub ->
            if (!activeSubjectsState.containsKey(sub)) {
                activeSubjectsState[sub] = true
            }
        }
    }

    // Active term assessments we track
    val terms = listOf("Term Assessment 1", "Term Assessment 2", "Term Assessment 3", "Term Assessment 4")

    // Filter grades for selected student or overall average
    val relevantGrades = remember(grades, selectedStudentId) {
        if (selectedStudentId == null) {
            grades
        } else {
            grades.filter { it.studentId == selectedStudentId }
        }
    }

    // Process raw grades into trend points
    // Map terms to list of averages per subject
    val trendPoints = remember(relevantGrades, activeSubjectsState) {
        terms.map { term ->
            val gradesInTerm = relevantGrades.filter { it.examName == term }
            val subjectAverages = availableSubjects.associateWith { sub ->
                val gradesInSubAndTerm = gradesInTerm.filter { it.subjectName == sub }
                if (gradesInSubAndTerm.isEmpty()) 0f else {
                    gradesInSubAndTerm.map { it.score }.average().toFloat()
                }
            }
            term to subjectAverages
        }
    }

    // Interactive tooltip state
    var selectedTermIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Analytics Icon",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Multi-Term Academic Analytics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Interactive dashboard mimicking Recharts canvas visuals",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Selector Row for student or average
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    val activeLabel = if (selectedStudentId == null) {
                        "School Class Average Tracker"
                    } else {
                        val st = students.find { it.id == selectedStudentId }
                        st?.let { "${it.name} (${it.gradeLevel})" } ?: "Unknown Student"
                    }

                    OutlinedButton(
                        onClick = { showStudentDropdown = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("student_trend_selector"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selectedStudentId == null) Icons.Default.Language else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = activeLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = showStudentDropdown,
                        onDismissRequest = { showStudentDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("School Class Average Tracking", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                selectedStudentId = null
                                showStudentDropdown = false
                                selectedTermIndex = -1
                                Toast.makeText(context, "Showing School overall averages", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider()
                        students.forEach { std ->
                            DropdownMenuItem(
                                text = { Text("${std.name} (${std.gradeLevel})") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                onClick = {
                                    selectedStudentId = std.id
                                    showStudentDropdown = false
                                    selectedTermIndex = -1
                                    Toast.makeText(context, "Tracking academic trends for ${std.name}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            // Quick Stats summary bar
            AcademicSummaryQuickCards(relevantGrades, terms)

            // Subject Filter Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Customize Recharts Render Lines",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSubjects.forEach { subject ->
                            val color = subjectColors[subject] ?: Color.Gray
                            val isChecked = activeSubjectsState[subject] == true
                            FilterChip(
                                selected = isChecked,
                                onClick = { activeSubjectsState[subject] = !isChecked },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(subject, fontSize = 12.sp)
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.15f),
                                    selectedLabelColor = color
                                )
                            )
                        }
                    }
                }
            }

            // THE RECHARTS-INSPIRED SPLINE CANVASES MAIN CONTAINER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recharts_trend_container"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "STUDENT PERFORMANCE OVER 4 TERMS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (selectedStudentId == null) "Aggregation across all classes" else "Individual linear record path",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Main Multiline Area Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(vertical = 12.dp)
                    ) {
                        RechartsMultiLineChart(
                            terms = terms,
                            subjects = availableSubjects,
                            subjectColors = subjectColors,
                            activeSubjects = activeSubjectsState,
                            trendPoints = trendPoints,
                            selectedIndex = selectedTermIndex,
                            onPointSelected = { idx -> selectedTermIndex = idx }
                        )
                    }

                    // Legends / Tooltip HUD Container
                    RechartsInteractiveTooltipHud(
                        terms = terms,
                        subjects = availableSubjects,
                        subjectColors = subjectColors,
                        activeSubjects = activeSubjectsState,
                        trendPoints = trendPoints,
                        selectedTermIndex = selectedTermIndex
                    )
                }
            }

            // BAR CHART: growth rate comparison
            GrowthAnalysisCard(relevantGrades, availableSubjects, subjectColors, activeSubjectsState)
        }
    }
}

@Composable
fun RechartsMultiLineChart(
    terms: List<String>,
    subjects: List<String>,
    subjectColors: Map<String, Color>,
    activeSubjects: Map<String, Boolean>,
    trendPoints: List<Pair<String, Map<String, Float>>>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit
) {
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(trendPoints) {
        animationTriggered = true
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val bottomMargin = 28.dp.toPx()
            val topMargin = 12.dp.toPx()
            val chartHeight = height - bottomMargin - topMargin

            val numPoints = terms.size
            val stepX = width / (numPoints - 1).coerceAtLeast(1)

            // 1. Draw horizontal grid lines like Recharts cartesianGrid
            val gridSteps = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
            gridSteps.forEach { step ->
                val y = topMargin + chartHeight * (1f - step)
                drawLine(
                    color = gridColor.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw vertical grid alignment line for selected term index
            if (selectedIndex >= 0 && selectedIndex < numPoints) {
                val highlightX = selectedIndex * stepX
                drawLine(
                    color = labelColor.copy(alpha = 0.5f),
                    start = Offset(highlightX, topMargin),
                    end = Offset(highlightX, height - bottomMargin),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw Splines/Curves for each active subject
            subjects.forEach { subject ->
                if (activeSubjects[subject] == true) {
                    val lineColor = subjectColors[subject] ?: Color.Gray

                    val points = mutableListOf<Offset>()
                    for (i in 0 until numPoints) {
                        val score = trendPoints[i].second[subject] ?: 0f
                        val yRatio = score / 100f
                        val x = i * stepX
                        val y = topMargin + chartHeight * (1f - yRatio)
                        points.add(Offset(x, y))
                    }

                    if (points.isNotEmpty()) {
                        val linePath = Path()
                        linePath.moveTo(points[0].x, points[0].y)

                        // Draw smooth curved connection line segments
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val cpX = (prev.x + curr.x) / 2
                            linePath.quadraticBezierTo(
                                cpX, prev.y,
                                curr.x, curr.y
                            )
                        }

                        // Render lines under animation progression
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw dot indices
                        points.forEachIndexed { index, point ->
                            val isHighlighted = selectedIndex == index
                            drawCircle(
                                color = Color.White,
                                radius = if (isHighlighted) 7.dp.toPx() else 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = lineColor,
                                radius = if (isHighlighted) 5.dp.toPx() else 2.5.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
        }

        // Overlay transparent interactive clickable bars
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            for (i in terms.indices) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onPointSelected(if (selectedIndex == i) -1 else i)
                        }
                )
            }
        }
    }
}

@Composable
fun RechartsInteractiveTooltipHud(
    terms: List<String>,
    subjects: List<String>,
    subjectColors: Map<String, Color>,
    activeSubjects: Map<String, Boolean>,
    trendPoints: List<Pair<String, Map<String, Float>>>,
    selectedTermIndex: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (selectedTermIndex < 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tap on any term node in the canvas above to query dynamic Recharts hover details.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                val currentTerm = terms[selectedTermIndex]
                val currentPointMap = trendPoints[selectedTermIndex].second

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RECHARTS DATA FOR: $currentTerm",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { /* Dismiss */ }
                    ) {
                        Text(
                            text = "Interactive",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subjects.forEach { subject ->
                        if (activeSubjects[subject] == true) {
                            val color = subjectColors[subject] ?: Color.Gray
                            val score = currentPointMap[subject] ?: 0f

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = String.format(Locale.US, "%.1f%% score", score),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademicSummaryQuickCards(
    relevantGrades: List<Grade>,
    terms: List<String>
) {
    val overallAverage = remember(relevantGrades) {
        if (relevantGrades.isEmpty()) 0f else {
            relevantGrades.map { it.score }.average().toFloat()
        }
    }

    val growthRate = remember(relevantGrades, terms) {
        val term1Grades = relevantGrades.filter { it.examName == terms.first() }
        val term4Grades = relevantGrades.filter { it.examName == terms.last() }

        if (term1Grades.isEmpty() || term4Grades.isEmpty()) 0f else {
            val t1Avg = term1Grades.map { it.score }.average().toFloat()
            val t4Avg = term4Grades.map { it.score }.average().toFloat()
            t4Avg - t1Avg
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Overall average",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", overallAverage),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "GPA",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 3.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Growth T1 ➔ T4",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%+.1f%%", growthRate),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (growthRate >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                    Icon(
                        imageVector = if (growthRate >= 0) Icons.Default.ArrowOutward else Icons.Default.SouthEast,
                        contentDescription = null,
                        tint = if (growthRate >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GrowthAnalysisCard(
    relevantGrades: List<Grade>,
    subjects: List<String>,
    subjectColors: Map<String, Color>,
    activeSubjects: Map<String, Boolean>
) {
    // Process slope rate per subject
    val progressBySubject = remember(relevantGrades) {
        subjects.map { subject ->
            val subGrades = relevantGrades.filter { it.subjectName == subject }
            val t1 = subGrades.filter { it.examName == "Term Assessment 1" }.map { it.score }.average()
            val t4 = subGrades.filter { it.examName == "Term Assessment 4" }.map { it.score }.average()
            
            val t1Val = if (t1.isNaN()) 0.0 else t1
            val t4Val = if (t4.isNaN()) 0.0 else t4
            
            subject to (t4Val - t1Val).toFloat()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    text = "ACADEMIC PROGRESS VELOCITY (T1 to T4 CHANGE)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                progressBySubject.forEach { (subject, slope) ->
                    if (activeSubjects[subject] == true) {
                        val color = subjectColors[subject] ?: Color.Gray
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format(Locale.US, "%+1.1f%% improvement", slope),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (slope >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }

                            // Dynamic horizontal bar chart
                            val absSlope = kotlin.math.abs(slope).coerceIn(0f, 30f)
                            val relativeRatio = absSlope / 30f // scale comparison relative to 30% max improvement
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(relativeRatio)
                                        .clip(CircleShape)
                                        .background(
                                            if (slope >= 0) Brush.horizontalGradient(
                                                colors = listOf(color, color.copy(alpha = 0.5f))
                                            ) else Brush.horizontalGradient(
                                                colors = listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
