package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Attendance
import com.example.data.entity.Grade
import com.example.data.entity.Student
import com.example.ui.SchoolViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PupilMonitoringScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    val students by viewModel.students.collectAsStateWithLifecycle(emptyList())
    val grades by viewModel.allGrades.collectAsStateWithLifecycle(emptyList())
    val attendanceRecords by viewModel.studentAttendance.collectAsStateWithLifecycle(emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedGradeFilter by remember { mutableStateOf("All Grades") }
    var selectedRiskFilter by remember { mutableStateOf("All Pupils") } // "All Pupils", "At Academic Risk", "At Attendance Risk", "Pending Fees"
    
    var isGradeDropdownExpanded by remember { mutableStateOf(false) }
    var isRiskDropdownExpanded by remember { mutableStateOf(false) }
    
    // AI analysis states
    var aiReportText by remember { mutableStateOf("") }
    var isGeneratingAiReport by remember { mutableStateOf(false) }
    
    // SnackBar notification states
    var showNotificationSnack by remember { mutableStateOf(false) }
    var notificationSnackMessage by remember { mutableStateOf("") }

    // Helper functions for live analytics computation
    fun getStudentAttendanceRate(studentId: Int): Float {
        val records = attendanceRecords.filter { it.studentId == studentId }
        if (records.isEmpty()) return 1.0f // default presentation health
        val presentNum = records.count { it.status == "Present" || it.status == "Late" }
        return presentNum.toFloat() / records.size
    }

    fun getStudentAverageScore(studentId: Int): Double {
        val studentGrades = grades.filter { it.studentId == studentId }
        if (studentGrades.isEmpty()) return 0.0
        val totalPct = studentGrades.sumOf { (it.score / it.maxScore) * 100.0 }
        return totalPct / studentGrades.size
    }

    // Determine filter groups
    val gradeFilters = listOf("All Grades") + students.map { it.gradeLevel }.distinct().sorted()
    val riskFilters = listOf("All Pupils", "At Academic Risk", "At Attendance Risk", "Pending Fees")

    // Filtered lists
    val filteredStudents = students.filter { student ->
        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) || 
                            student.rollNumber.contains(searchQuery, ignoreCase = true)
        
        val matchesGrade = if (selectedGradeFilter == "All Grades") true else student.gradeLevel == selectedGradeFilter
        
        val matchesRisk = when (selectedRiskFilter) {
            "All Pupils" -> true
            "At Academic Risk" -> {
                val avg = getStudentAverageScore(student.id)
                avg > 0 && avg < 55.0 // Scored lower than 55% average
            }
            "At Attendance Risk" -> {
                getStudentAttendanceRate(student.id) < 0.80f // Less than 80% attendance
            }
            "Pending Fees" -> {
                (student.feesTotal - student.feesPaid) > 0
            }
            else -> true
        }
        
        matchesSearch && matchesGrade && matchesRisk
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Notification SnackBar overlay
        AnimatedVisibility(
            visible = showNotificationSnack,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success Notification",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = notificationSnackMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = { showNotificationSnack = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (selectedStudent == null) {
            // ==================== LIST / SCREEN 1: OVERLOAD MONITOR ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro Panel Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Pupil Monitoring Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Track overall cohort wellness across academics, consistency of attendance registers, and outstanding dues balances in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // Controls Block: Search + Dual Dropdown Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1.3f),
                        placeholder = { Text("Search by name/ID...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Grade Filter Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { isGradeDropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedGradeFilter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = isGradeDropdownExpanded,
                            onDismissRequest = { isGradeDropdownExpanded = false }
                        ) {
                            gradeFilters.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level, fontSize = 13.sp) },
                                    onClick = {
                                        selectedGradeFilter = level
                                        isGradeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Risk Filter Dropdown
                    Box(modifier = Modifier.weight(1.1f)) {
                        Button(
                            onClick = { isRiskDropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedRiskFilter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = isRiskDropdownExpanded,
                            onDismissRequest = { isRiskDropdownExpanded = false }
                        ) {
                            riskFilters.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter, fontSize = 13.sp) },
                                    onClick = {
                                        selectedRiskFilter = filter
                                        isRiskDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Student Monitoring list
                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Text(
                                "No pupils matches the active criteria.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredStudents) { student ->
                            val attRate = getStudentAttendanceRate(student.id)
                            val avgScore = getStudentAverageScore(student.id)
                            val dues = student.feesTotal - student.feesPaid

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStudent = student
                                        aiReportText = "" // reset report
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Roll ID: ${student.rollNumber} • ${student.gradeLevel}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Outlined.Analytics,
                                            contentDescription = "Evaluate",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Metric Rows
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Attendance Indicator Item
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val badgeColor = when {
                                                attRate >= 0.90f -> Color(0xFF2E7D32)
                                                attRate >= 0.80f -> Color(0xFFF57C00)
                                                else -> Color(0xFFD32F2F)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeColor)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "Attendance: %.0f%%", attRate * 100),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        // Academic Indicator Item
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val acadColor = when {
                                                avgScore >= 80 -> Color(0xFF2E7D32)
                                                avgScore >= 55 -> Color(0xFFF57C00)
                                                avgScore > 0 -> Color(0xFFD32F2F)
                                                else -> Color.Gray
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(acadColor)
                                            )
                                            Text(
                                                text = if (avgScore > 0) String.format(Locale.US, "Average: %.1f%%", avgScore) else "No grades",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        // Financial Indicator Item
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1.2f),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            val statusText: String
                                            val duesColor: Color
                                            if (dues <= 0) {
                                                statusText = "Fees Cleared"
                                                duesColor = Color(0xFF2E7D32)
                                            } else {
                                                statusText = String.format(Locale.US, "Bal: UGX %,.0f", dues)
                                                duesColor = Color(0xFFC62828)
                                            }
                                            Text(
                                                text = statusText,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = duesColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== VIEW 2: INDIVIDUAL DETAILED PUPIL MONITORING PROFILE ====================
            val pupil = selectedStudent!!
            val attRate = getStudentAttendanceRate(pupil.id)
            val avgScore = getStudentAverageScore(pupil.id)
            val dues = pupil.feesTotal - pupil.feesPaid
            
            val pupilGrades = grades.filter { it.studentId == pupil.id }
            val pupilAttendance = attendanceRecords.filter { it.studentId == pupil.id }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { selectedStudent = null },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = pupil.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Comprehensive Assessment Suite",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Basic Metrics Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("REGISTRATION IDENTIFIER", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("Roll ID: ${pupil.rollNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Grade Level: ${pupil.gradeLevel}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("Parent Mobile: ${pupil.phone.ifBlank { "Unregistered" }}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                            }
                            // Call Parent fast shortcut
                            if (pupil.phone.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.broadcastSms(
                                                recipientName = pupil.name,
                                                phoneNumbers = listOf(pupil.phone),
                                                message = """
                                                    Dear Guardian, here is an update on ${pupil.name}: Academic Average score is ${String.format(Locale.US, "%.1f%%", avgScore)} and Registered Attendance Rate is ${String.format(Locale.US, "%.0f%%", attRate * 100)}. Please support their studies.
                                                """.trimIndent()
                                            )
                                            notificationSnackMessage = "SMS Performance Statement drafted to ${pupil.phone}!"
                                            showNotificationSnack = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.SendToMobile, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SMS Report", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Canvas & Indicators Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Consistency & Engagement Gauge", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Attendance Circular Gauge
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val ringColor = when {
                                        attRate >= 0.90f -> Color(0xFF2E7D32)
                                        attRate >= 0.80f -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    }
                                    
                                    Box(contentAlignment = Alignment.Center) {
                                        Canvas(modifier = Modifier.size(80.dp)) {
                                            // gray track line
                                            drawCircle(
                                                color = Color.LightGray.copy(alpha = 0.3f),
                                                radius = size.minDimension / 2,
                                                style = Stroke(width = 8.dp.toPx())
                                            )
                                            // progress sector fill
                                            drawArc(
                                                color = ringColor,
                                                startAngle = -90f,
                                                sweepAngle = 360f * attRate,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%.0f%%", attRate * 100),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text("Attendance Rate", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }

                                // Academic Performance Gauge
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val sliderColor = when {
                                        avgScore >= 75 -> Color(0xFF2E7D32)
                                        avgScore >= 50 -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    }
                                    
                                    Box(contentAlignment = Alignment.Center) {
                                        Canvas(modifier = Modifier.size(80.dp)) {
                                            // gray track line
                                            drawCircle(
                                                color = Color.LightGray.copy(alpha = 0.3f),
                                                radius = size.minDimension / 2,
                                                style = Stroke(width = 8.dp.toPx())
                                            )
                                            // progress sector fill
                                            drawArc(
                                                color = sliderColor,
                                                startAngle = -90f,
                                                sweepAngle = 360f * (avgScore.toFloat() / 100f),
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%.1f%%", avgScore),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text("Academic Average", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }

                                // Fees Cleared Gauge
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val percentPaid = if (pupil.feesTotal > 0) (pupil.feesPaid / pupil.feesTotal).toFloat() else 1.0f
                                    val feesIndicatorColor = if (dues <= 0) Color(0xFF2E7D32) else Color(0xFFD84315)
                                    
                                    Box(contentAlignment = Alignment.Center) {
                                        Canvas(modifier = Modifier.size(80.dp)) {
                                            drawCircle(
                                                color = Color.LightGray.copy(alpha = 0.3f),
                                                radius = size.minDimension / 2,
                                                style = Stroke(width = 8.dp.toPx())
                                            )
                                            drawArc(
                                                color = feesIndicatorColor,
                                                startAngle = -90f,
                                                sweepAngle = 360f * percentPaid,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%.0f%%", percentPaid * 100),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text("Fees Balance Paid", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Academic Records Breakdown Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Live Subjects Report Card", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            
                            if (pupilGrades.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No registered grades detected.", color = Color.Gray, fontSize = 13.sp)
                                }
                            } else {
                                pupilGrades.forEach { m ->
                                    val percent = (m.score / m.maxScore) * 100
                                    val statusColor = when {
                                        percent >= 75 -> Color(0xFF2E7D32)
                                        percent >= 50 -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(m.subjectName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${m.examName} • Registered: ${m.dateRecorded}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f / %.0f", m.score, m.maxScore),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(statusColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = String.format(Locale.US, "%.0f%%", percent),
                                                    color = statusColor,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                // Attendance Log Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Recent Attendance Transactions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            
                            if (pupilAttendance.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No registered attendance logs detected.", color = Color.Gray, fontSize = 13.sp)
                                }
                            } else {
                                val presentCount = pupilAttendance.count { it.status == "Present" }
                                val absentCount = pupilAttendance.count { it.status == "Absent" }
                                val lateCount = pupilAttendance.count { it.status == "Late" }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Present: $presentCount", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Absent: $absentCount", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Late: $lateCount", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                pupilAttendance.take(5).forEach { record ->
                                    val icon = when (record.status) {
                                        "Present" -> Icons.Default.CheckCircle
                                        "Late" -> Icons.Default.Warning
                                        else -> Icons.Default.Close
                                    }
                                    val iconColor = when (record.status) {
                                        "Present" -> Color(0xFF2E7D32)
                                        "Late" -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(record.date, fontSize = 14.sp)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconColor)
                                            Text(record.status, style = MaterialTheme.typography.bodyMedium, color = iconColor, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                // AI Holistic Pupil Assessment System (Copilot)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Copilot Analytics",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "AI Holistic Pupil Assessment",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        isGeneratingAiReport = true
                                        aiReportText = ""
                                        coroutineScope.launch {
                                            val systemPrompt = """
                                                You are an elite, modern Senior School Copilot and Child Development Psychologist. Your job is to construct a beautifully structured, encouraging, objective, and parent-friendly assessment monitoring statement of the pupil based on their registered metrics (grades, attendance, dues fees cleared status). Keep your analysis elegant, structured with crisp bullet points, professional, actionable, and warm. Outline clear action items of how parents and teachers can coordinate to support them.
                                            """.trimIndent()
                                            
                                            val gradesStr = if (pupilGrades.isEmpty()) "No grade records." else {
                                                pupilGrades.joinToString("\n") { "- ${it.subjectName} [${it.examName}]: score ${it.score}/${it.maxScore} (${String.format(Locale.US, "%.0f%%", (it.score/it.maxScore)*100)})" }
                                            }
                                            
                                            val userPrompt = """
                                                Generate an analytical report card assessment statement for candidate:
                                                - Name: ${pupil.name}
                                                - Roll Number: ${pupil.rollNumber}
                                                - Grade Level: ${pupil.gradeLevel}
                                                - Attendance rate: ${String.format(Locale.US, "%.0f%%", attRate * 100)} (${pupilAttendance.size} total tracking logs)
                                                - Academic average percentage score: ${String.format(Locale.US, "%.1f%%", avgScore)}
                                                - Registration Status: ${pupil.status}
                                                - Dues financial state: Total Term UGX ${pupil.feesTotal}, Amount Paid UGX ${pupil.feesPaid} (Dues: UGX $dues)
                                                
                                                Academic Report Card items registered:
                                                $gradesStr
                                                
                                                Produce:
                                                1. Holistic Cohort Wellness Rank & Category (Academic & Attendance Health status)
                                                2. Specific strengths observed
                                                3. Specific focus areas where they have struggled or need additional guidance
                                                4. Coordinate Strategy recommendation to parents
                                            """.trimIndent()
                                            
                                            val response = com.example.data.api.askGemini(systemPrompt, userPrompt)
                                            aiReportText = response
                                            isGeneratingAiReport = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = !isGeneratingAiReport
                                ) {
                                    if (isGeneratingAiReport) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reading Metrics...", fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Run Copilot Review", fontSize = 12.sp)
                                    }
                                }
                            }

                            if (isGeneratingAiReport) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
                                    Text("AI Copilot is analyzing raw registered class records & logs...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }

                            if (aiReportText.isNotEmpty()) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = aiReportText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.DarkGray,
                                            lineHeight = 20.sp
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
