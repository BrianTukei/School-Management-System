package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Assessment
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CentralGradeBookScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsStateWithLifecycle(emptyList())
    val grades by viewModel.allGrades.collectAsStateWithLifecycle(emptyList())

    // Class selection state
    val classLevels = listOf("All Classes", "Nursery", "Middle", "Top", "P.1", "P.2", "P.3", "P.4", "P.5", "P.6", "P.7")
    var selectedClass by remember { mutableStateOf("P.7") }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Search query state for individual matrix
    var searchQuery by remember { mutableStateOf("") }

    // Filtered students based on selection
    val filteredStudents = remember(students, selectedClass, searchQuery) {
        students.filter { s ->
            val matchClass = selectedClass == "All Classes" || s.gradeLevel == selectedClass
            val matchSearch = s.name.contains(searchQuery, ignoreCase = true) || s.rollNumber.contains(searchQuery, ignoreCase = true)
            matchClass && matchSearch
        }
    }

    // Filtered grades based on selection
    val filteredGrades = remember(grades, selectedClass) {
        if (selectedClass == "All Classes") {
            grades
        } else {
            val studentIdsInClass = students.filter { it.gradeLevel == selectedClass }.map { it.id }.toSet()
            grades.filter { it.studentId in studentIdsInClass }
        }
    }

    // Process grade distributions
    // Buckets: A (90-100), B (80-89), C (70-79), D (60-69), E (50-59), F (<50)
    val gradeDistribution = remember(filteredGrades) {
        val distribution = mutableMapOf(
            "A" to 0,
            "B" to 0,
            "C" to 0,
            "D" to 0,
            "E" to 0,
            "F" to 0
        )
        filteredGrades.forEach { g ->
            val pct = (g.score / g.maxScore) * 100.0
            when {
                pct >= 90.0 -> distribution["A"] = (distribution["A"] ?: 0) + 1
                pct >= 80.0 -> distribution["B"] = (distribution["B"] ?: 0) + 1
                pct >= 70.0 -> distribution["C"] = (distribution["C"] ?: 0) + 1
                pct >= 60.0 -> distribution["D"] = (distribution["D"] ?: 0) + 1
                pct >= 50.0 -> distribution["E"] = (distribution["E"] ?: 0) + 1
                else -> distribution["F"] = (distribution["F"] ?: 0) + 1
            }
        }
        distribution
    }

    // Calculate quick stats
    val overallAverage = remember(filteredGrades) {
        if (filteredGrades.isEmpty()) 0.0 else {
            filteredGrades.map { (it.score / it.maxScore) * 100.0 }.average()
        }
    }

    val highestScore = remember(filteredGrades) {
        if (filteredGrades.isEmpty()) 0.0 else {
            filteredGrades.maxOf { (it.score / it.maxScore) * 100.0 }
        }
    }

    val lowestScore = remember(filteredGrades) {
        if (filteredGrades.isEmpty()) 0.0 else {
            filteredGrades.minOf { (it.score / it.maxScore) * 100.0 }
        }
    }

    val passRate = remember(filteredGrades) {
        if (filteredGrades.isEmpty()) 0.0 else {
            val passed = filteredGrades.count { (it.score / it.maxScore) * 100.0 >= 50.0 }
            (passed.toDouble() / filteredGrades.size) * 100.0
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header info row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Central Grade Book & Distribution Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Real-time class-wide evaluation analysis modeled after Recharts design systems.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Dropdown & Filter Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { classDropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("gradebook_class_filter_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
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
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = if (selectedClass == "All Classes") "All Class Levels" else "Class Level: $selectedClass",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        classLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                                onClick = {
                                    selectedClass = level
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // If empty dataset, quick load UNEB-conforming marks directly
                if (grades.isEmpty()) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.seedSampleDatabase()
                            Toast.makeText(context, "Seeded compliant assessment datasets for visual analytics!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("gradebook_quick_seed_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seed Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val stats = listOf(
                    Triple("Class Average", String.format(Locale.US, "%.1f%%", overallAverage), MaterialTheme.colorScheme.primary),
                    Triple("Highest Grade", String.format(Locale.US, "%.1f%%", highestScore), Color(0xFF2E7D32)),
                    Triple("Lowest Grade", String.format(Locale.US, "%.1f%%", lowestScore), MaterialTheme.colorScheme.error),
                    Triple("Pass Rate", String.format(Locale.US, "%.1f%%", passRate), Color(0xFFE65100))
                )

                stats.forEach { (label, value, color) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (filteredGrades.isEmpty()) "—" else value,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = color
                            )
                        }
                    }
                }
            }
        }

        // RENDER 1: Recharts Canvas - Grade Distribution Bar Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gradebook_distribution_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var selectedBucket by remember { mutableStateOf<String?>(null) }

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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                text = "GRADE FREQUENCY DISTRIBUTION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        if (selectedBucket != null) {
                            val count = gradeDistribution[selectedBucket] ?: 0
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable { selectedBucket = null }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Grade $selectedBucket: $count Records",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear tooltip",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Tap columns to inspect frequencies",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    if (filteredGrades.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No assessment records for distribution calculation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // RECHARTS INTERACTIVE CANVAS IMPLEMENTATION
                        val bucketKeys = listOf("A", "B", "C", "D", "E", "F")
                        val distributionValues = bucketKeys.map { gradeDistribution[it] ?: 0 }
                        val maxFrequency = (distributionValues.maxOrNull() ?: 1).coerceAtLeast(1)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 10.dp)
                        ) {
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val accentColor = MaterialTheme.colorScheme.secondary

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val leftMargin = 85f
                                val bottomMargin = 80f
                                val rightMargin = 15f
                                val topMargin = 20f

                                val graphWidth = canvasWidth - leftMargin - rightMargin
                                val graphHeight = canvasHeight - topMargin - bottomMargin

                                // Draw Cartesian dashed guidelines (100%, 75%, 50%, 25%, 0%)
                                val divisions = 4
                                val gridColor = Color.LightGray.copy(alpha = 0.35f)
                                for (i in 0..divisions) {
                                    val y = topMargin + (graphHeight / divisions) * i
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(leftMargin, y),
                                        end = Offset(canvasWidth - rightMargin, y),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }

                                // Plot distributions
                                val countBars = bucketKeys.size
                                val totalSpacingFraction = 0.35f
                                val barWidth = (graphWidth / countBars) * (1f - totalSpacingFraction)
                                val spacing = (graphWidth / countBars) * totalSpacingFraction

                                val barBrush = Brush.linearGradient(
                                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.75f))
                                )
                                val highlightBrush = Brush.linearGradient(
                                    colors = listOf(accentColor, accentColor.copy(alpha = 0.82f))
                                )

                                for (i in 0 until countBars) {
                                    val valFreq = distributionValues[i]
                                    val barHeightFrac = valFreq.toFloat() / maxFrequency.toFloat()
                                    val finalBarHeight = graphHeight * barHeightFrac

                                    val xLeft = leftMargin + (graphWidth / countBars) * i + spacing / 2
                                    val yTop = topMargin + graphHeight - finalBarHeight

                                    val isHighlighted = selectedBucket == bucketKeys[i]

                                    // Draw rounded bar chart columns
                                    drawRoundRect(
                                        brush = if (isHighlighted) highlightBrush else barBrush,
                                        topLeft = Offset(xLeft, yTop),
                                        size = Size(barWidth, finalBarHeight),
                                        cornerRadius = CornerRadius(12f, 12f)
                                    )
                                }

                                // Base bottom line
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    start = Offset(leftMargin - 15f, topMargin + graphHeight),
                                    end = Offset(canvasWidth - rightMargin, topMargin + graphHeight),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Interactive Canvas Touch overlay layers using Row of columns
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 28.dp, bottom = 24.dp)
                            ) {
                                Spacer(modifier = Modifier.width(10.dp))
                                bucketKeys.forEachIndexed { idx, key ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable {
                                                selectedBucket = if (selectedBucket == key) null else key
                                            }
                                    )
                                }
                            }
                        }

                        // Labels Row for distributions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val aliases = listOf("A\n(90-100)", "B\n(80-89)", "C\n(70-79)", "D\n(60-69)", "E\n(50-59)", "F\n(<50)")
                            aliases.forEach { alias ->
                                Text(
                                    text = alias,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(52.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Ledger label
        item {
            Text(
                "Central Class Grade Book Matrix",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Search Bar input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name or roll number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gradebook_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (filteredStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("No student records found matching the filters.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            // Excel-style scrollable metrics sheet
            items(filteredStudents) { pupil ->
                val pupilGrades = remember(grades, pupil.id) {
                    grades.filter { g -> g.studentId == pupil.id }
                }

                val subjectAverages = remember(pupilGrades) {
                    val subjects = listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")
                    subjects.associateWith { subName ->
                        val matchingGrades = pupilGrades.filter { it.subjectName.equals(subName, ignoreCase = true) }
                        if (matchingGrades.isEmpty()) null else {
                            matchingGrades.map { (it.score / it.maxScore) * 100.0 }.average()
                        }
                    }
                }

                val computedAverage = remember(subjectAverages) {
                    val validMeans = subjectAverages.values.filterNotNull()
                    if (validMeans.isEmpty()) null else validMeans.average()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gradebook_matrix_row_${pupil.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = pupil.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Roll: ${pupil.rollNumber} • Class: ${pupil.gradeLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            computedAverage?.let { avg ->
                                Surface(
                                    color = when {
                                        avg >= 85.0 -> Color(0xFFE8F5E9)
                                        avg >= 70.0 -> Color(0xFFE3F2FD)
                                        avg >= 50.0 -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFFFEBEE)
                                    },
                                    contentColor = when {
                                        avg >= 85.0 -> Color(0xFF2E7D32)
                                        avg >= 70.0 -> Color(0xFF1565C0)
                                        avg >= 50.0 -> Color(0xFFE65100)
                                        else -> Color(0xFFC62828)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%% Avg", avg),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid of subject marks
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 4
                        ) {
                            listOf("Mathematics", "English Language", "Integrated Science", "Social Studies").forEach { subName ->
                                val subAvg = subjectAverages[subName]
                                val colorScheme = MaterialTheme.colorScheme

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (subName.contains(" ")) subName.substringBefore(" ") else subName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subAvg?.let { String.format(Locale.US, "%.0f%%", it) } ?: "—",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (subAvg != null) {
                                            if (subAvg >= 50.0) colorScheme.primary else colorScheme.error
                                        } else colorScheme.onSurface.copy(alpha = 0.4f)
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
