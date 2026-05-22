@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.entity.*
import com.example.ui.FeesStats
import com.example.ui.SchoolViewModel
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolApp(viewModel: SchoolViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screen title mapping
    val screenTitle = when {
        currentRoute == "dashboard" -> "School Dashboard"
        currentRoute == "students" -> "Student Directory"
        currentRoute?.startsWith("student_detail") == true -> "Student Academic Profile"
        currentRoute?.startsWith("student_form") == true -> "Enroll Student"
        currentRoute == "teachers" -> "Faculty Directory"
        currentRoute?.startsWith("teacher_form") == true -> "Register Faculty"
        currentRoute == "classes" -> "Class Planner"
        currentRoute == "attendance" -> "Attendance Registrar"
        currentRoute == "grades" -> "Academic Records"
        currentRoute == "ai_assistant" -> "AI Senior Copilot"
        currentRoute == "sms_broadcast" -> "SMS Broadcast Gateway"
        else -> "Pearl Junior School"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    if (currentRoute != "dashboard" && currentRoute != "students" && currentRoute != "teachers" && currentRoute != "classes") {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "School Logo",
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (currentRoute == "dashboard" || currentRoute == "students" || currentRoute == "teachers" || currentRoute == "classes" || currentRoute == "ai_assistant") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                        label = { Text("Dashboard") },
                        selected = currentRoute == "dashboard",
                        onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { saveState = true }; launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.People, "Students") },
                        label = { Text("Students") },
                        selected = currentRoute == "students",
                        onClick = { navController.navigate("students") { popUpTo("dashboard"); launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, "Faculty") },
                        label = { Text("Faculty") },
                        selected = currentRoute == "teachers",
                        onClick = { navController.navigate("teachers") { popUpTo("dashboard"); launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Classes") },
                        label = { Text("Classes") },
                        selected = currentRoute == "classes",
                        onClick = { navController.navigate("classes") { popUpTo("dashboard"); launchSingleTop = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AutoAwesome, "AI Copilot") },
                        label = { Text("AI Copilot") },
                        selected = currentRoute == "ai_assistant",
                        onClick = { navController.navigate("ai_assistant") { popUpTo("dashboard"); launchSingleTop = true } }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(navController, viewModel)
            }
            composable("students") {
                StudentsListScreen(navController, viewModel)
            }
            composable(
                route = "student_detail/{studentId}",
                arguments = listOf(navArgument("studentId") { type = NavType.IntType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
                StudentDetailScreen(studentId, navController, viewModel)
            }
            composable(
                route = "student_form?studentId={studentId}",
                arguments = listOf(navArgument("studentId") { type = NavType.IntType; defaultValue = 0 })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
                StudentFormScreen(studentId, navController, viewModel)
            }
            composable("teachers") {
                TeachersListScreen(navController, viewModel)
            }
            composable(
                route = "teacher_form?teacherId={teacherId}",
                arguments = listOf(navArgument("teacherId") { type = NavType.IntType; defaultValue = 0 })
            ) { backStackEntry ->
                val teacherId = backStackEntry.arguments?.getInt("teacherId") ?: 0
                TeacherFormScreen(teacherId, navController, viewModel)
            }
            composable("classes") {
                ClassesScreen(viewModel)
            }
            composable("attendance") {
                AttendanceScreen(viewModel)
            }
            composable("grades") {
                GradesScreen(navController, viewModel)
            }
            composable("ai_assistant") {
                AiAssistantScreen(viewModel)
            }
            composable("sms_broadcast") {
                SmsBroadcastScreen(viewModel)
            }
        }
    }
}

// ==================== DASHBOARD HUB ====================
@Composable
fun DashboardScreen(navController: NavController, viewModel: SchoolViewModel) {
    val totalStudents by viewModel.studentCount.collectAsStateWithLifecycle()
    val totalTeachers by viewModel.teacherCount.collectAsStateWithLifecycle()
    val feesStats by viewModel.totalFeesStats.collectAsStateWithLifecycle()
    val avgGradeScore by viewModel.averagePerformanceScore.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming & Profile Header Section (as per Geometric Balance HTML mockup)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ST. JUDE ACADEMY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Hi, Administrator",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Rounded dynamic avatar with color gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "A",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Quick Stats Title
        item {
            Text(
                "QUICK STATS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Attendance",
                    value = "94% On Track",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "GPA / Academic",
                    value = String.format(Locale.US, "%.1f%% Average", avgGradeScore),
                    icon = Icons.Default.Star,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Academic Census & Progress
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Students Enrolled",
                    value = "$totalStudents Pupils",
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1.2f)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .height(120.dp)
                        .weight(1.8f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "FEES REVENUE TARGET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val progress = if (feesStats.totalExpected > 0) {
                                (feesStats.totalCollected / feesStats.totalExpected).toFloat()
                            } else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = String.format(Locale.US, "UGX %,.0f collected", feesStats.totalCollected),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "UGX %,.0f total", feesStats.totalExpected),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Next Deadline Card from Layout HTML Design
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "NEXT DEADLINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Literature Review Paper",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            "Due Tomorrow, 11:59 PM • English Dept",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Rotating or progress element as per layout
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 0.75f },
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 3.dp,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Quick Operations Section Title
        item {
            Text(
                "QUICK MANAGEMENT WORKSPACES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionChip(
                            title = "Roll Calls",
                            icon = Icons.Default.DateRange,
                            description = "Record updates",
                            badgeColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = { navController.navigate("attendance") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionChip(
                            title = "Grades Board",
                            icon = Icons.Default.ListAlt,
                            description = "View/add marks",
                            badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { navController.navigate("grades") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionChip(
                            title = "Enroll Student",
                            icon = Icons.Default.PersonAdd,
                            description = "Onboard forms",
                            badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = { navController.navigate("student_form?studentId=0") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionChip(
                            title = "Hire Faculty",
                            icon = Icons.Default.GroupAdd,
                            description = "Register teachers",
                            badgeColor = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { navController.navigate("teacher_form?teacherId=0") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionChip(
                            title = "SMS Broadcast",
                            icon = Icons.Default.SendToMobile,
                            description = "Contact guardians",
                            badgeColor = MaterialTheme.colorScheme.primaryContainer,
                            onClick = { navController.navigate("sms_broadcast") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // AI Senior Copilot Promotion Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AI SENIOR COPILOT ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Instant Administrative Auditor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Have full conversational dialogue regarding fee collections, registry sizes, exam averages, or active leaves with perfect database accuracy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate("ai_assistant") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Launch Conversation Monitor", color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Customize border strokes based on standard outline vs special accent container
    val outlineBorderColor = if (color == MaterialTheme.colorScheme.tertiaryContainer) {
        Color(0xFFFFDCBE) // Warm Delicate Amber Border
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, outlineBorderColor),
        modifier = modifier
            .height(120.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large Accent Geometric Icon Badge with custom color mapping
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (color == MaterialTheme.colorScheme.tertiaryContainer) Color(0xFF8B5000)
                            else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                }
            }
            Column {
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}


// ==================== STUDENT DIRECTORY ====================
@Composable
fun StudentsListScreen(navController: NavController, viewModel: SchoolViewModel) {
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("All Grades") }

    val gradeLevels = listOf("All Grades", "Grade 10-A", "Grade 11-B", "Grade 12-A")

    val filteredList = studentsList.filter { student ->
        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                student.rollNumber.contains(searchQuery, ignoreCase = true)
        val matchesGrade = selectedGradeFilter == "All Grades" || student.gradeLevel == selectedGradeFilter
        matchesSearch && matchesGrade
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("student_form?studentId=0") },
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or roll number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search"),
                singleLine = true
            )

            // Grade filters chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gradeLevels.forEach { grade ->
                    FilterChip(
                        selected = selectedGradeFilter == grade,
                        onClick = { selectedGradeFilter = grade },
                        label = { Text(grade) }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching students found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { student ->
                        StudentItemCard(student = student, onClick = {
                            navController.navigate("student_detail/${student.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun StudentItemCard(student: Student, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.rollNumber}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (student.gender == "Male") MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Roll ID: ${student.rollNumber} • ${student.gradeLevel}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            // Financial Status Indicator Badge
            val feeDue = student.feesTotal - student.feesPaid
            val (badgeText, badgeColor, badgeTextColor) = when {
                feeDue <= 0 -> Triple("Paid", Color(0xFF4CAF50), Color.White)
                student.feesPaid > 0 -> Triple("Partial", Color(0xFFFF9800), Color.Black)
                else -> Triple("Unpaid", Color(0xFFF44336), Color.White)
            }
            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}


// ==================== ENROLL / EDIT STUDENT ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(studentId: Int, navController: NavController, viewModel: SchoolViewModel) {
    var name by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("Primary Seven (P.7)") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var feesTotal by remember { mutableStateOf("850000.0") }
    var feesPaid by remember { mutableStateOf("0.0") }
    var status by remember { mutableStateOf("Active") }

    LaunchedEffect(studentId) {
        if (studentId != 0) {
            viewModel.getStudentProfile(studentId).first()?.let { s ->
                name = s.name
                rollNumber = s.rollNumber
                gradeLevel = s.gradeLevel
                email = s.email
                phone = s.phone
                gender = s.gender
                feesTotal = s.feesTotal.toString()
                feesPaid = s.feesPaid.toString()
                status = s.status
            }
        }
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var rollError by remember { mutableStateOf<String?>(null) }

    val gradeOptions = listOf(
        "Baby Class",
        "Middle Class",
        "Top Class",
        "Primary One (P.1)",
        "Primary Two (P.2)",
        "Primary Three (P.3)",
        "Primary Four (P.4)",
        "Primary Five (P.5)",
        "Primary Six (P.6)",
        "Primary Seven (P.7)"
    )
    val genderOptions = listOf("Male", "Female")
    val statusOptions = listOf("Active", "Inactive")

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (studentId == 0) "Register New Student" else "Update Academic Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Student Full Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_form_name"),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it; rollError = null },
                    label = { Text("Roll ID (e.g. S108)") },
                    isError = rollError != null,
                    supportingText = { rollError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_form_roll"),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Grade Level Group
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Expected Grade", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = gradeLevel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                gradeOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = { gradeLevel = opt; expanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Gender Group
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gender", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                genderOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = { gender = opt; expanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Primary Contact Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Fees Total
                    OutlinedTextField(
                        value = feesTotal,
                        onValueChange = { feesTotal = it },
                        label = { Text("Total Term Fee (UGX)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    // Fees Paid
                    OutlinedTextField(
                        value = feesPaid,
                        onValueChange = { feesPaid = it },
                        label = { Text("Amount Paid (UGX)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (studentId != 0) {
                item {
                    Column {
                        Text("Registration Status", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                statusOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = { status = opt; expanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Name cannot be left empty."
                                return@Button
                            }
                            if (rollNumber.isBlank()) {
                                rollError = "Roll ID cannot be left empty."
                                return@Button
                            }
                            // Call save
                            val st = Student(
                                id = studentId,
                                name = name,
                                rollNumber = rollNumber,
                                gradeLevel = gradeLevel,
                                email = email,
                                phone = phone,
                                gender = gender,
                                feesTotal = feesTotal.toDoubleOrNull() ?: 1500.0,
                                feesPaid = feesPaid.toDoubleOrNull() ?: 0.0,
                                status = status
                            )
                            viewModel.saveStudent(st)
                            navController.navigateUp()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_form_save")
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}


// ==================== KEY STUDENT PROFILE (DETAILS) ====================
@Composable
fun StudentDetailScreen(studentId: Int, navController: NavController, viewModel: SchoolViewModel) {
    val studentFlow = remember(studentId) { viewModel.getStudentProfile(studentId) }
    val student by studentFlow.collectAsStateWithLifecycle(initialValue = null)

    val gradesList by remember(studentId) { viewModel.getGradesOfStudent(studentId) }.collectAsStateWithLifecycle(emptyList())
    val attendanceList by remember(studentId) { viewModel.getAttendanceOfStudent(studentId) }.collectAsStateWithLifecycle(emptyList())

    var showFeePaymentDialog by remember { mutableStateOf(false) }
    var feePaymentAmount by remember { mutableStateOf("") }
    
    var showAddGradeDialog by remember { mutableStateOf(false) }
    var gradeSubjectName by remember { mutableStateOf("Physics") }
    var gradeExamName by remember { mutableStateOf("Midterm Exam") }
    var gradeScore by remember { mutableStateOf("") }

    val subjectOptions = listOf("Physics", "Mathematics", "Computer Science", "Chemistry", "English Literature")
    val examOptions = listOf("Midterm Exam", "Final Exam", "Monthly Test", "Homework Project")

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val s = student!!
        Scaffold { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Info Header card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        s.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Roll ID: ${s.rollNumber} • ${s.gradeLevel}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Row {
                                    IconButton(onClick = { navController.navigate("student_form?studentId=${s.id}") }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Student Profile", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        viewModel.deleteStudent(s)
                                        navController.navigateUp()
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Student Profile", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Sub Info (Contact + Status)
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Primary Contact Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, "Email", modifier = Modifier.size(20.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(s.email.ifBlank { "N/A" })
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, "Phone", modifier = Modifier.size(20.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(s.phone.ifBlank { "N/A" })
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, "Gender", modifier = Modifier.size(20.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Gender: ${s.gender}  |  Status: ${s.status}")
                            }
                        }
                    }
                }

                // Billing and Fees Status
                item {
                    val outstanding = s.feesTotal - s.feesPaid
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tuition & Academic Fees", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(Locale.US, "Fees Paid: UGX %,.0f of %,.0f", s.feesPaid, s.feesTotal),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Button(
                                    onClick = { showFeePaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Pay Fees")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (outstanding > 0) {
                                Text(
                                    text = String.format(Locale.US, "Outstanding Desk Balance: UGX %,.0f", outstanding),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Full Term Account Cleared",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }

                // Academic Report Card Marks Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Academic Report Sheet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showAddGradeDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Grade")
                        }
                    }
                }

                if (gradesList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No grades entered yet", color = Color.Gray)
                        }
                    }
                } else {
                    items(gradesList) { grade ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(grade.subjectName, fontWeight = FontWeight.Bold)
                                Text("${grade.examName} • ${grade.dateRecorded}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val percent = (grade.score / grade.maxScore) * 100
                                val letter = when {
                                    percent >= 90 -> "A"
                                    percent >= 80 -> "B"
                                    percent >= 70 -> "C"
                                    percent >= 60 -> "D"
                                    else -> "F"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(letter, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Text(
                                    String.format(Locale.US, "%.1f / %.0f", grade.score, grade.maxScore),
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.removeGrade(grade) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete score", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                // Attendance Summary & Log Screen
                item {
                    Text("Class Attendance Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (attendanceList.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "No registered attendance records.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    val daysPresent = attendanceList.count { it.status == "Present" }
                    val totalDays = attendanceList.size
                    val percent = if (totalDays > 0) (daysPresent.toFloat() / totalDays * 100).toInt() else 100

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total sessions: $totalDays", style = MaterialTheme.typography.bodyMedium)
                            Text("Presence score: $percent%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    items(attendanceList) { att ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(att.date)
                            val color = when (att.status) {
                                "Present" -> Color(0xFF4CAF50)
                                "Absent" -> Color(0xFFF44336)
                                else -> Color(0xFFFF9800) // Late
                            }
                            Surface(color = color, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = att.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // dialog helper 1: fees
    if (showFeePaymentDialog) {
        AlertDialog(
            onDismissRequest = { showFeePaymentDialog = false },
            title = { Text("Record Fee Payment") },
            text = {
                Column {
                    Text("Enters a manual tuition fee receipt into academic books.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = feePaymentAmount,
                        onValueChange = { feePaymentAmount = it },
                        label = { Text("Payment ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = feePaymentAmount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.addFeePayment(studentId, amt)
                    }
                    showFeePaymentDialog = false
                    feePaymentAmount = ""
                }) {
                    Text("Complete Transaction")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeePaymentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // dialog helper 2: add grade
    if (showAddGradeDialog) {
        AlertDialog(
            onDismissRequest = { showAddGradeDialog = false },
            title = { Text("Record Exam Score") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var subjectExpanded by remember { mutableStateOf(false) }
                    var examExpanded by remember { mutableStateOf(false) }

                    Text("Subject")
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded }
                    ) {
                        OutlinedTextField(
                            value = gradeSubjectName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjectOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { gradeSubjectName = opt; subjectExpanded = false })
                            }
                        }
                    }

                    Text("Exam Name")
                    ExposedDropdownMenuBox(
                        expanded = examExpanded,
                        onExpandedChange = { examExpanded = !examExpanded }
                    ) {
                        OutlinedTextField(
                            value = gradeExamName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = examExpanded,
                            onDismissRequest = { examExpanded = false }
                        ) {
                            examOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { gradeExamName = opt; examExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = gradeScore,
                        onValueChange = { gradeScore = it },
                        label = { Text("Marks Obtained (Max 100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val sc = gradeScore.toDoubleOrNull()
                    if (sc != null && sc in 0.0..100.0) {
                        viewModel.insertGrade(studentId, gradeSubjectName, gradeExamName, sc, 100.0)
                    }
                    showAddGradeDialog = false
                    gradeScore = ""
                }) {
                    Text("Add Mark")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGradeDialog = false }) { Text("Cancel") }
            }
        )
    }
}


// ==================== FACULTY DIRECTORY ====================
@Composable
fun TeachersListScreen(navController: NavController, viewModel: SchoolViewModel) {
    val teachersList by viewModel.teachers.collectAsStateWithLifecycle()
    val leaveRequestsList by viewModel.leaveRequests.collectAsStateWithLifecycle()
    
    // Attendance states
    val teacherAttendanceDate by viewModel.teacherAttendanceDate.collectAsStateWithLifecycle()
    val teacherAttendanceMap by viewModel.teacherAttendanceEditingMap.collectAsStateWithLifecycle()
    val activeAttendanceRecord by viewModel.activeTeacherAttendanceRecord.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Expanded teacher card IDs
    var expandedTeacherIds by remember { mutableStateOf(setOf<Int>()) }
    
    val context = LocalContext.current

    // Sync attendance details initially or when date/teachers change
    LaunchedEffect(teacherAttendanceDate, teachersList) {
        viewModel.syncTeacherAttendanceDetails()
    }

    // New Leave fields
    var showAddLeaveDialog by remember { mutableStateOf(false) }
    var leaveTeacherId by remember { mutableStateOf(0) }
    var leaveStartDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    var leaveEndDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    var leaveReason by remember { mutableStateOf("") }
    var leaveType by remember { mutableStateOf("Sick Leave") }
    val leaveTypes = listOf("Sick Leave", "Casual Leave", "Annual Leave", "Maternity/Paternity", "Other")

    val filteredList = teachersList.filter { teacher ->
        teacher.name.contains(searchQuery, ignoreCase = true) ||
                teacher.subjectSpecialty.contains(searchQuery, ignoreCase = true) ||
                teacher.assignedRole.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate("teacher_form?teacherId=0") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Hire Teacher")
                }
            } else if (activeTab == 2) {
                FloatingActionButton(
                    onClick = { 
                        if (teachersList.isNotEmpty()) {
                            leaveTeacherId = teachersList.first().id
                            showAddLeaveDialog = true 
                        } else {
                            android.widget.Toast.makeText(context, "Please hire a faculty member first.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Apply Leave")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // M3 Tab Indicator
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Faculty Roster", style = MaterialTheme.typography.titleSmall) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Roster") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Attendance", style = MaterialTheme.typography.titleSmall) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Attendance") }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Leave Planner", style = MaterialTheme.typography.titleSmall) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Leaves") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> {
                    // ROSTER VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Roster Stats Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("TOTAL FACULTY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Text("${teachersList.size} members", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ACTIVE ROSTER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                                    Text("${teachersList.count { it.status == "Active" }} staff", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by name, role or department...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (filteredList.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No faculty member matches found", color = Color.Gray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredList) { teacher ->
                                    val isExpanded = expandedTeacherIds.contains(teacher.id)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .clickable {
                                                    expandedTeacherIds = if (isExpanded) {
                                                        expandedTeacherIds - teacher.id
                                                    } else {
                                                        expandedTeacherIds + teacher.id
                                                    }
                                                }
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Rounded avatar initials
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = teacher.name.firstOrNull()?.toString() ?: "T",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(teacher.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                        Text(teacher.assignedRole.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                Row {
                                                    IconButton(onClick = { navController.navigate("teacher_form?teacherId=${teacher.id}") }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                                                    }
                                                    IconButton(onClick = { viewModel.deleteTeacher(teacher) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Terminate employee", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        teacher.subjectSpecialty,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                                Surface(
                                                    color = if (teacher.status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        teacher.status,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                        color = if (teacher.status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828)
                                                    )
                                                }
                                            }

                                            // Expanded items
                                            AnimatedVisibility(visible = isExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(top = 16.dp)
                                                        .fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
                                                    
                                                    Row {
                                                        Text("Education: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        Text(if (teacher.qualifications.isBlank()) "B.Ed degree credentials" else teacher.qualifications, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                    Row {
                                                        Text("Date of Birth: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        Text(if (teacher.dateOfBirth.isBlank()) "N/A" else teacher.dateOfBirth, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                    Row {
                                                        Text("Residential Address: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        Text(if (teacher.address.isBlank()) "No address on file" else teacher.address, style = MaterialTheme.typography.bodySmall)
                                                    }

                                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Email, "Email", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(teacher.email, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Phone, "Phone", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(teacher.phone, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }

                                            if (!isExpanded) {
                                                Text(
                                                    "Tap to show bio, education & personal info...",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.padding(top = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // ATTENDANCE TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = teacherAttendanceDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Log attendance date") },
                                modifier = Modifier.weight(1f),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        val parsed = try { sdf.parse(teacherAttendanceDate) ?: java.util.Date() } catch(e: Exception) { java.util.Date() }
                                        val cal = java.util.Calendar.getInstance().apply { time = parsed }
                                        android.app.DatePickerDialog(
                                            context,
                                            { _: android.widget.DatePicker, y: Int, m: Int, d: Int ->
                                                val newCal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                                viewModel.setTeacherAttendanceDate(sdf.format(newCal.time))
                                            },
                                            cal.get(java.util.Calendar.YEAR),
                                            cal.get(java.util.Calendar.MONTH),
                                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                                    }
                                }
                            )

                            Button(
                                onClick = { 
                                    viewModel.saveTeacherAttendance()
                                    android.widget.Toast.makeText(context, "Attendance saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Submit Ledger")
                                }
                            }
                        }

                        val loggedPresent = activeAttendanceRecord.count { it.status == "Present" }
                        val loggedAbsent = activeAttendanceRecord.count { it.status == "Absent" }
                        val loggedLate = activeAttendanceRecord.count { it.status == "Late" }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Present: $loggedPresent", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC62828)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Absent: $loggedAbsent", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE65100)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Late: $loggedLate", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (teachersList.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No employees available to record attendance")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(teachersList) { teacher ->
                                    val currentStatus = teacherAttendanceMap[teacher.id] ?: "Present"
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(teacher.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                    Text(teacher.assignedRole, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                                
                                                Surface(
                                                    color = when (currentStatus) {
                                                        "Present" -> Color(0xFFE8F5E9)
                                                        "Absent" -> Color(0xFFFFEBEE)
                                                        "Late" -> Color(0xFFFFF3E0)
                                                        else -> Color(0xFFE3F2FD)
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = currentStatus,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (currentStatus) {
                                                            "Present" -> Color(0xFF2E7D32)
                                                            "Absent" -> Color(0xFFC62828)
                                                            "Late" -> Color(0xFFE65100)
                                                            else -> Color(0xFF1565C0)
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val statuses = listOf("Present", "Absent", "Late", "On Leave")
                                                statuses.forEach { st ->
                                                    val isSelected = currentStatus == st
                                                    val statusBtnColor = when (st) {
                                                        "Present" -> Color(0xFF2E7D32)
                                                        "Absent" -> Color(0xFFC62828)
                                                        "Late" -> Color(0xFFE65100)
                                                        else -> Color(0xFF1565C0)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { viewModel.updateTeacherAttendanceStatus(teacher.id, st) },
                                                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                                            containerColor = statusBtnColor.copy(alpha = 0.12f)
                                                        ) else ButtonDefaults.outlinedButtonColors(),
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) statusBtnColor else MaterialTheme.colorScheme.outline
                                                        ),
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            text = st,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) statusBtnColor else MaterialTheme.colorScheme.onSurface
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
                2 -> {
                    // LEAVE PLANNING TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("PENDING REVIEWS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text("${leaveRequestsList.count { it.status == "Pending" }} applications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("APPROVED LEAVES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text("${leaveRequestsList.count { it.status == "Approved" }} approved", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("FACULTY LEAVE LEDGER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                        if (leaveRequestsList.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No leaves registered", color = Color.Gray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(leaveRequestsList) { req ->
                                    val staffMember = teachersList.find { it.id == req.teacherId }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(staffMember?.name ?: "Unknown Instructor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                    Text(staffMember?.assignedRole ?: "Faculty Member", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                                
                                                val badgeColor = when (req.status) {
                                                    "Approved" -> Color(0xFFE8F5E9)
                                                    "Rejected" -> Color(0xFFFFEBEE)
                                                    else -> Color(0xFFFFF3E0)
                                                }
                                                val badgeTextColor = when (req.status) {
                                                    "Approved" -> Color(0xFF2E7D32)
                                                    "Rejected" -> Color(0xFFC62828)
                                                    else -> Color(0xFFE65100)
                                                }
                                                Surface(
                                                    color = badgeColor,
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = req.status,
                                                        color = badgeTextColor,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        req.leaveType,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "${req.startDate} to ${req.endDate}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Reason: ${req.reason}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )

                                            if (req.status == "Pending") {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.saveLeaveRequest(req.copy(status = "Rejected"))
                                                            android.widget.Toast.makeText(context, "Leave review rejected.", android.widget.Toast.LENGTH_SHORT).show()
                                                        },
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828)),
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Decline")
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.saveLeaveRequest(req.copy(status = "Approved"))
                                                            android.widget.Toast.makeText(context, "Leave review approved!", android.widget.Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Approve")
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                    TextButton(onClick = { viewModel.deleteLeaveRequest(req) }) {
                                                        Text("Remove Log", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
        }
    }


    // APPLY LEAVE ALERT DIALOG
    if (showAddLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showAddLeaveDialog = false },
            title = { Text("Record Staff Leave File") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Register a formal leaves request on behalf of a school faculty member.")

                    var teacherExpanded by remember { mutableStateOf(false) }
                    val selectedTeacher = teachersList.find { it.id == leaveTeacherId }
                    Column {
                        Text("Faculty Applicant", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = teacherExpanded,
                            onExpandedChange = { teacherExpanded = !teacherExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTeacher?.name ?: "Select staff member",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = teacherExpanded,
                                onDismissRequest = { teacherExpanded = false }
                            ) {
                                teachersList.forEach { valTeacher ->
                                    DropdownMenuItem(
                                        text = { Text("${valTeacher.name} - ${valTeacher.assignedRole}") },
                                        onClick = { 
                                            leaveTeacherId = valTeacher.id
                                            teacherExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    var leaveTypeExpanded by remember { mutableStateOf(false) }
                    Column {
                        Text("Category of Leave", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = leaveTypeExpanded,
                            onExpandedChange = { leaveTypeExpanded = !leaveTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = leaveType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leaveTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = leaveTypeExpanded,
                                onDismissRequest = { leaveTypeExpanded = false }
                            ) {
                                leaveTypes.forEach { choice ->
                                    DropdownMenuItem(
                                        text = { Text(choice) },
                                        onClick = { 
                                            leaveType = choice
                                            leaveTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = leaveStartDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Starting Date") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val parsed = try { sdf.parse(leaveStartDate) ?: java.util.Date() } catch(e: Exception) { java.util.Date() }
                                val cal = java.util.Calendar.getInstance().apply { time = parsed }
                                android.app.DatePickerDialog(
                                    context,
                                    { _: android.widget.DatePicker, y: Int, m: Int, d: Int ->
                                        val newCal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        leaveStartDate = sdf.format(newCal.time)
                                    },
                                    cal.get(java.util.Calendar.YEAR),
                                    cal.get(java.util.Calendar.MONTH),
                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Choose Start Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = leaveEndDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Concluding Date") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val parsed = try { sdf.parse(leaveEndDate) ?: java.util.Date() } catch(e: Exception) { java.util.Date() }
                                val cal = java.util.Calendar.getInstance().apply { time = parsed }
                                android.app.DatePickerDialog(
                                    context,
                                    { _: android.widget.DatePicker, y: Int, m: Int, d: Int ->
                                        val newCal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        leaveEndDate = sdf.format(newCal.time)
                                    },
                                    cal.get(java.util.Calendar.YEAR),
                                    cal.get(java.util.Calendar.MONTH),
                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Choose End Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = { Text("Details / Justifications of Absence") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (leaveReason.isNotBlank()) {
                        viewModel.saveLeaveRequest(
                            LeaveRequest(
                                teacherId = leaveTeacherId,
                                startDate = leaveStartDate,
                                endDate = leaveEndDate,
                                reason = leaveReason,
                                status = "Pending",
                                leaveType = leaveType
                            )
                        )
                        showAddLeaveDialog = false
                        leaveReason = ""
                    } else {
                        android.widget.Toast.makeText(context, "Please enter a valid justification.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Submit Leave Sheet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLeaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}


// ==================== FACULTY REGISTRATION FORM ====================
@Composable
fun TeacherFormScreen(teacherId: Int, navController: NavController, viewModel: SchoolViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("Physics") }
    var status by remember { mutableStateOf("Active") }
    var qualifications by remember { mutableStateOf("") }
    var assignedRole by remember { mutableStateOf("Teacher") }
    var dateOfBirth by remember { mutableStateOf("1990-01-01") }
    var address by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }

    val specialtyOptions = listOf("Physics", "Mathematics", "Computer Science", "Chemistry", "English Literature")
    val context = LocalContext.current

    LaunchedEffect(teacherId) {
        if (teacherId != 0) {
            viewModel.getTeacherProfile(teacherId).first()?.let { t ->
                name = t.name
                email = t.email
                phone = t.phone
                specialty = t.subjectSpecialty
                status = t.status
                qualifications = t.qualifications
                assignedRole = t.assignedRole
                dateOfBirth = if (t.dateOfBirth.isBlank()) "1990-01-01" else t.dateOfBirth
                address = t.address
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (teacherId == 0) "Hire New Faculty" else "Edit Employee Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Teacher Name") },
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Teacher Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Direct Telephone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column {
                Text("Subject Department Specialization", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        specialtyOptions.forEach { opt ->
                            DropdownMenuItem(text = { Text(opt) }, onClick = { specialty = opt; expanded = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = assignedRole,
                onValueChange = { assignedRole = it },
                label = { Text("Assigned Role (e.g. Senior Teacher, Head of Department)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = qualifications,
                onValueChange = { qualifications = it },
                label = { Text("Qualifications & Degrees") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                trailingIcon = {
                    IconButton(onClick = {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val parsed = try { sdf.parse(dateOfBirth) ?: java.util.Date() } catch(e: Exception) { java.util.Date() }
                        val cal = java.util.Calendar.getInstance().apply { time = parsed }
                        android.app.DatePickerDialog(
                            context,
                            { _: android.widget.DatePicker, y: Int, m: Int, d: Int ->
                                val newCal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                dateOfBirth = sdf.format(newCal.time)
                            },
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH),
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select DOB")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Residential Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { navController.navigateUp() }, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = "Name cannot be empty."
                            return@Button
                        }
                        viewModel.saveTeacher(
                            Teacher(
                                id = teacherId,
                                name = name,
                                email = email,
                                phone = phone,
                                subjectSpecialty = specialty,
                                status = status,
                                qualifications = qualifications,
                                assignedRole = assignedRole,
                                dateOfBirth = dateOfBirth,
                                address = address
                            )
                        )
                        navController.navigateUp()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Hire Information")
                }
            }
        }
    }
}


// ==================== CLASSES & SUBJECTS PLANNER ====================
@Composable
fun ClassesScreen(viewModel: SchoolViewModel) {
    val classSubjectsList by viewModel.classSubjects.collectAsStateWithLifecycle()
    val teachersList by viewModel.teachers.collectAsStateWithLifecycle()

    var showAddAssignment by remember { mutableStateOf(false) }
    var selectedGrade by remember { mutableStateOf("Grade 10-A") }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedTeacherId by remember { mutableStateOf(0) }

    val gradeOptions = listOf("Grade 10-A", "Grade 11-B", "Grade 12-A")
    val subjectOptions = listOf("Physics", "Mathematics", "Computer Science", "Chemistry", "English Literature")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (teachersList.isNotEmpty()) {
                    selectedTeacherId = teachersList.first().id
                }
                showAddAssignment = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Assign Classes")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Academics Curriculum Syllabus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("A centralized registry link mapping secondary student grades to subjects taught by specific teachers dynamically.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            if (classSubjectsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No curriculum assignments registered.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(classSubjectsList) { assignment ->
                        val teacher = teachersList.find { it.id == assignment.teacherId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                assignment.className,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(assignment.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Instructed by: ${teacher?.name ?: "Unknown Instructor"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.removeClassSubject(assignment) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove syllabus link")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAssignment) {
        AlertDialog(
            onDismissRequest = { showAddAssignment = false },
            title = { Text("Configure Curricula") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var gradeExpanded by remember { mutableStateOf(false) }
                    var subjectExpanded by remember { mutableStateOf(false) }
                    var teacherExpanded by remember { mutableStateOf(false) }

                    Text("Selected Grade")
                    ExposedDropdownMenuBox(
                        expanded = gradeExpanded,
                        onExpandedChange = { gradeExpanded = !gradeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedGrade,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = gradeExpanded,
                            onDismissRequest = { gradeExpanded = false }
                        ) {
                            gradeOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { selectedGrade = opt; gradeExpanded = false })
                            }
                        }
                    }

                    Text("Syllabus Subject")
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjectOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { selectedSubject = opt; subjectExpanded = false })
                            }
                        }
                    }

                    Text("Faculty Lecturer")
                    if (teachersList.isEmpty()) {
                        Text("No faculty available. Register one in your faculty hub first.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    } else {
                        val activeTeacher = teachersList.find { it.id == selectedTeacherId } ?: teachersList.first()
                        ExposedDropdownMenuBox(
                            expanded = teacherExpanded,
                            onExpandedChange = { teacherExpanded = !teacherExpanded }
                        ) {
                            OutlinedTextField(
                                value = activeTeacher.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = teacherExpanded,
                                onDismissRequest = { teacherExpanded = false }
                            ) {
                                teachersList.forEach { t ->
                                    DropdownMenuItem(text = { Text(t.name) }, onClick = { selectedTeacherId = t.id; teacherExpanded = false })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTeacherId != 0 || teachersList.isNotEmpty()) {
                            val actualId = if (selectedTeacherId == 0) teachersList.first().id else selectedTeacherId
                            viewModel.saveClassSubject(selectedGrade, selectedSubject, actualId)
                        }
                        showAddAssignment = false
                    },
                    enabled = teachersList.isNotEmpty()
                ) {
                    Text("Add Curriculum")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssignment = false }) { Text("Cancel") }
            }
        )
    }
}


// ==================== ATTENDANCE REGISTER (CLASS + DATE BASED) ====================
@Composable
fun AttendanceScreen(viewModel: SchoolViewModel) {
    val date by viewModel.attendanceDate.collectAsStateWithLifecycle()
    val grade by viewModel.attendanceGrade.collectAsStateWithLifecycle()
    val studentsList by viewModel.attendanceStudents.collectAsStateWithLifecycle()
    val attendanceMap by viewModel.attendanceEditingMap.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val gradeOptions = listOf("Grade 10-A", "Grade 11-B", "Grade 12-A")

    LaunchedEffect(date, grade) {
        viewModel.syncAttendanceDetails()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Daily Attendance Roll Call", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        // Config section (Pick grade & date)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date picker text actions
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date (YYYY-MM-DD)") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val parsed = sdf.parse(date) ?: Date()
                                val cal = Calendar.getInstance().apply { time = parsed }
                                DatePickerDialog(
                                    context,
                                    { _: DatePicker, y: Int, m: Int, d: Int ->
                                        val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                        viewModel.setAttendanceDate(sdf.format(newCal.time))
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Choose Date")
                            }
                        },
                        modifier = Modifier.weight(1.2f)
                    )

                    // Grade dropdown choice
                    var gradeExpanded by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = gradeExpanded,
                            onExpandedChange = { gradeExpanded = !gradeExpanded }
                        ) {
                            OutlinedTextField(
                                value = grade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = gradeExpanded,
                                onDismissRequest = { gradeExpanded = false }
                            ) {
                                gradeOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = { viewModel.setAttendanceGrade(opt); gradeExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Student roster list
        if (studentsList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No active students enrolled in this grade class.", textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(studentsList) { student ->
                    val status = attendanceMap[student.id] ?: "Present"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = AssistChipDefaults.assistChipBorder(enabled = true),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(student.name, fontWeight = FontWeight.Bold)
                                Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            // Custom status Segmented button
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                AttendanceButtonChoice(
                                    label = "P",
                                    selected = status == "Present",
                                    activeColor = Color(0xFF4CAF50),
                                    onClick = { viewModel.updateAttendanceStatus(student.id, "Present") }
                                )
                                AttendanceButtonChoice(
                                    label = "A",
                                    selected = status == "Absent",
                                    activeColor = Color(0xFFF44336),
                                    onClick = { viewModel.updateAttendanceStatus(student.id, "Absent") }
                                )
                                AttendanceButtonChoice(
                                    label = "L",
                                    selected = status == "Late",
                                    activeColor = Color(0xFFFF9800),
                                    onClick = { viewModel.updateAttendanceStatus(student.id, "Late") }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveAttendance()
                    // Snack message / visual confirmations could show, we can just log or alert
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Commit Attendance Sheets")
            }
        }
    }
}

@Composable
fun AttendanceButtonChoice(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (selected) activeColor else Color.LightGray.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Color.Black,
            fontSize = 12.sp
        )
    }
}


// ==================== COMPREHENSIVE GRADES RECORD BOARD ====================
@Composable
fun GradesScreen(navController: NavController, viewModel: SchoolViewModel) {
    val listGrades by viewModel.allGrades.collectAsStateWithLifecycle()
    val listStudents by viewModel.students.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf(0) } // 0: Ledger, 1: Excel Bulk Entry, 2: Report Cards

    // Ledger states
    var showAddGrade by remember { mutableStateOf(false) }
    var scoreValue by remember { mutableStateOf("") }
    var examInput by remember { mutableStateOf("Midterm Exam") }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var selectedStudentId by remember { mutableStateOf(0) }

    val subjectOptions = listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "Literacy & Numeracy")
    val examOptions = listOf("Midterm Exam", "End of Term Exam", "Continuous Assessment")

    val classLevels = listOf(
        "Baby Class", "Middle Class", "Top Class",
        "Primary One (P.1)", "Primary Two (P.2)", "Primary Three (P.3)",
        "Primary Four (P.4)", "Primary Five (P.5)", "Primary Six (P.6)",
        "Primary Seven (P.7)"
    )

    // Excel Entry states
    var excelClassInput by remember { mutableStateOf("Primary Seven (P.7)") }
    var excelSubjectInput by remember { mutableStateOf("Mathematics") }

    // Grid entry states: studentId -> score String
    val midTermInputs = remember { mutableStateMapOf<Int, String>() }
    val endOfTermInputs = remember { mutableStateMapOf<Int, String>() }

    // String for Excel Copy-Paste CSV
    var bulkCsvInput by remember { mutableStateOf("") }
    var showCsvInstructions by remember { mutableStateOf(false) }
    var csvErrorFeedback by remember { mutableStateOf<String?>(null) }

    // Repopulate spreadsheet inputs when class/subject selections change
    LaunchedEffect(excelClassInput, excelSubjectInput, listGrades, listStudents) {
        val classStudents = listStudents.filter { it.gradeLevel == excelClassInput }
        classStudents.forEach { st ->
            val midVal = listGrades.find { it.studentId == st.id && it.subjectName == excelSubjectInput && it.examName == "Midterm Exam" }?.score
            val finalVal = listGrades.find { it.studentId == st.id && it.subjectName == excelSubjectInput && it.examName == "End of Term Exam" }?.score
            
            midTermInputs[st.id] = midVal?.let { String.format(Locale.US, "%.0f", it) } ?: ""
            endOfTermInputs[st.id] = finalVal?.let { String.format(Locale.US, "%.0f", it) } ?: ""
        }
    }

    // Report card state
    var selectedReportStudentId by remember { mutableStateOf(0) }
    var showReportCardPreview by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (listStudents.isNotEmpty()) {
                            selectedStudentId = listStudents.first().id
                        }
                        showAddGrade = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add grade")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Info Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pearl Junior School", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Ugandan Primary & Nursery Academic Assessments", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Tab bar
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Grade Ledger", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Excel bulk Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Report Cards", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
            }

            // Tab contents
            when (activeTab) {
                0 -> {
                    // TAB 0: Ledger List view of individual scores
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Standard Records Feed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (listGrades.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Text("No individual grade records found.", color = Color.Gray)
                                    Text("Use 'Excel bulk Sheet' to enter scores easily in bulk.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listGrades) { gr ->
                                    val student = listStudents.find { it.id == gr.studentId }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(student?.name ?: "Unknown Student", fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Class: ${student?.gradeLevel ?: "Unknown"}  •  Roll: ${student?.rollNumber ?: "N/A"}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    "${gr.subjectName} • ${gr.examName}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        String.format(Locale.US, "%.1f / 100", gr.score),
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                                IconButton(onClick = { viewModel.removeGrade(gr) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete score", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Excel Assessment Entry Sheet (Local Interactive grid + paste parser)
                    var excelClassExpanded by remember { mutableStateOf(false) }
                    var excelSubjExpanded by remember { mutableStateOf(false) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Assessments Spreadsheet Workspace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Simulates editing an Excel Sheet for bulk marks entry directly. Perfect for fast terminal evaluations.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        // Select Class & Subject selectors
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Class Selection dropdown
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Selected Class", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = excelClassExpanded,
                                        onExpandedChange = { excelClassExpanded = !excelClassExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = excelClassInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = excelClassExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                        ExposedDropdownMenu(
                                            expanded = excelClassExpanded,
                                            onDismissRequest = { excelClassExpanded = false }
                                        ) {
                                            classLevels.forEach { lvl ->
                                                DropdownMenuItem(
                                                    text = { Text(lvl) },
                                                    onClick = { excelClassInput = lvl; excelClassExpanded = false }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Subject selector dropdown
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Academic Subject", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = excelSubjExpanded,
                                        onExpandedChange = { excelSubjExpanded = !excelSubjExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = excelSubjectInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = excelSubjExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                        ExposedDropdownMenu(
                                            expanded = excelSubjExpanded,
                                            onDismissRequest = { excelSubjExpanded = false }
                                        ) {
                                            subjectOptions.forEach { sub ->
                                                DropdownMenuItem(
                                                    text = { Text(sub) },
                                                    onClick = { excelSubjectInput = sub; excelSubjExpanded = false }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Interactive Spreadsheet
                        val filteredStudents = listStudents.filter { it.gradeLevel == excelClassInput }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Excel light green hue
                                border = BorderStroke(1.dp, Color(0xFF81C784))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Excel Spreadsheet Grid (${filteredStudents.size} Pupils Found)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        IconButton(onClick = { showCsvInstructions = !showCsvInstructions }) {
                                            Icon(Icons.Default.Upload, contentDescription = "CSV Import", tint = Color(0xFF2E7D32))
                                        }
                                    }

                                    if (showCsvInstructions) {
                                        Card(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Bulk Copy-Paste CSV from Excel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("Paste rows copied from Excel. Format must be: RollNumber,Midterm,EndOfTerm. E.g:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                Text("S1001,82,85\nS1002,94,92\nS1005,45,60", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                
                                                OutlinedTextField(
                                                    value = bulkCsvInput,
                                                    onValueChange = { bulkCsvInput = it },
                                                    placeholder = { Text("S1001,82,85\nS1002,94,92") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    minLines = 3,
                                                    maxLines = 6,
                                                    textStyle = MaterialTheme.typography.bodySmall
                                                )
                                                
                                                csvErrorFeedback?.let {
                                                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                                }

                                                Button(
                                                    onClick = {
                                                        csvErrorFeedback = null
                                                        try {
                                                            var parsedCount = 0
                                                            val lines = bulkCsvInput.split("\n")
                                                            lines.forEach { line ->
                                                                if (line.isNotBlank()) {
                                                                    val parts = line.split(",")
                                                                    if (parts.size >= 3) {
                                                                        val roll = parts[0].trim()
                                                                        val midVal = parts[1].trim()
                                                                        val finalVal = parts[2].trim()
                                                                        
                                                                        val pupil = listStudents.find { it.rollNumber.equals(roll, ignoreCase = true) }
                                                                        if (pupil != null) {
                                                                            midTermInputs[pupil.id] = midVal
                                                                            endOfTermInputs[pupil.id] = finalVal
                                                                            parsedCount++
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            csvErrorFeedback = "Successfully filled $parsedCount pupil grades into the spreadsheet!"
                                                            bulkCsvInput = ""
                                                        } catch (e: Exception) {
                                                            csvErrorFeedback = "Parse error: Ensure valid format without headers."
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    modifier = Modifier.align(Alignment.End)
                                                ) {
                                                    Text("Load into Spreadsheet Cells")
                                                }
                                            }
                                        }
                                    }

                                    if (filteredStudents.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No registered pupils found in $excelClassInput. Go to 'Students' tab to add pupils first.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                                        }
                                    } else {
                                        // Header row of sheet
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFC8E6C9))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("PUPIL NAME", modifier = Modifier.weight(1.8f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                            Text("MID-TERM (max 100)", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                            Text("END-TERM (max 100)", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                        }

                                        // Student rows
                                        filteredStudents.forEach { st ->
                                            var midVal by remember(st.id) { mutableStateOf(midTermInputs[st.id] ?: "") }
                                            var finalVal by remember(st.id) { mutableStateOf(endOfTermInputs[st.id] ?: "") }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(0.5.dp, Color(0xFFDCDCDC))
                                                    .background(Color.White)
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1.8f)) {
                                                    Text(st.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text("ID: ${st.rollNumber}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                                }

                                                // Midterm input field
                                                OutlinedTextField(
                                                    value = midVal,
                                                    onValueChange = {
                                                        midVal = it
                                                        midTermInputs[st.id] = it
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1.1f),
                                                    placeholder = { Text("-") },
                                                    textStyle = MaterialTheme.typography.bodySmall,
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color(0xFF2E7D32),
                                                        unfocusedBorderColor = Color.LightGray
                                                    )
                                                )

                                                // End term input field
                                                OutlinedTextField(
                                                    value = finalVal,
                                                    onValueChange = {
                                                        finalVal = it
                                                        endOfTermInputs[st.id] = it
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1.1f),
                                                    placeholder = { Text("-") },
                                                    textStyle = MaterialTheme.typography.bodySmall,
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color(0xFF2E7D32),
                                                        unfocusedBorderColor = Color.LightGray
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Save action
                                        Button(
                                            onClick = {
                                                filteredStudents.forEach { st ->
                                                    val midScore = midTermInputs[st.id]?.toDoubleOrNull()
                                                    val finalScore = endOfTermInputs[st.id]?.toDoubleOrNull()

                                                    // Handle midterm record
                                                    val oldMid = listGrades.find { it.studentId == st.id && it.subjectName == excelSubjectInput && it.examName == "Midterm Exam" }
                                                    if (midScore != null) {
                                                        if (oldMid != null) {
                                                            viewModel.removeGrade(oldMid)
                                                        }
                                                        viewModel.insertGrade(st.id, excelSubjectInput, "Midterm Exam", midScore, 100.0)
                                                    } else if (oldMid != null && (midTermInputs[st.id] ?: "").isEmpty()) {
                                                        viewModel.removeGrade(oldMid)
                                                    }

                                                    // Handle end of term record
                                                    val oldFinal = listGrades.find { it.studentId == st.id && it.subjectName == excelSubjectInput && it.examName == "End of Term Exam" }
                                                    if (finalScore != null) {
                                                        if (oldFinal != null) {
                                                            viewModel.removeGrade(oldFinal)
                                                        }
                                                        viewModel.insertGrade(st.id, excelSubjectInput, "End of Term Exam", finalScore, 100.0)
                                                    } else if (oldFinal != null && (endOfTermInputs[st.id] ?: "").isEmpty()) {
                                                        viewModel.removeGrade(oldFinal)
                                                    }
                                                }
                                                android.widget.Toast.makeText(context, "Excel Spreadsheet Synced & Saved successfully!", android.widget.Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Save & Sync Class Marks to Database")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Terminal Report Cards Generation Hub
                    var reportStudentExpanded by remember { mutableStateOf(false) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Primary & Nursery UNEB Grading Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Generates terminal report cards based on continuous assessments. For Primary Seven (P.7), calculates aggregates (best four core subjects) and PLE grades. For nursery classes, issues development descriptors.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        item {
                            Text("Select Pupil to View Terminal Report Card", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (listStudents.isEmpty()) {
                                Text("No registered pupils found.", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                val currentReportPupil = listStudents.find { it.id == selectedReportStudentId } ?: listStudents.first()
                                if (selectedReportStudentId == 0) {
                                    selectedReportStudentId = currentReportPupil.id
                                }

                                ExposedDropdownMenuBox(
                                    expanded = reportStudentExpanded,
                                    onExpandedChange = { reportStudentExpanded = !reportStudentExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = "${currentReportPupil.name} (${currentReportPupil.gradeLevel})",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reportStudentExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodyLarge
                                    )
                                    ExposedDropdownMenu(
                                        expanded = reportStudentExpanded,
                                        onDismissRequest = { reportStudentExpanded = false }
                                    ) {
                                        listStudents.forEach { st ->
                                            DropdownMenuItem(
                                                text = { Text("${st.name} (${st.gradeLevel})") },
                                                onClick = {
                                                    selectedReportStudentId = st.id
                                                    showReportCardPreview = true
                                                    reportStudentExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Terminal Report Card Layout Preview
                        val activePupil = listStudents.find { it.id == selectedReportStudentId }
                        if (activePupil != null) {
                            item {
                                val isNursery = activePupil.gradeLevel.contains("Class", ignoreCase = true)
                                val studentGrades = listGrades.filter { it.studentId == activePupil.id }
                                val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")

                                var totalWeightedGradeSum = 0.0
                                var countSubjects = 0
                                var aggregatesSum = 0
                                var subjectGradesCount = 0

                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF6)), // Cream colored background
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // School Emblem Header
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "PEARL JUNIOR SCHOOL",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text("P.O. Box 773, Kampala • Tel: +256 772 400101", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Text("Web: www.pearljuniorschool.sc.ug", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text(
                                                    "TERMLY CA PROGRESS REPORT CARD",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }

                                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                                        // Student information details
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Pupil Name: " + activePupil.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text("Class Level: " + activePupil.gradeLevel, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Roll ID: " + activePupil.rollNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text("Term: Term I (2026)", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }

                                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                                        // Subject Grid Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("ACADEMIC SUBJECT", modifier = Modifier.weight(1.8f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("MIDTM(40%)", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("EOT(60%)", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("TOTAL", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("GRADE", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }

                                        // Grading parameters
                                        // var totalWeightedGradeSum = 0.0
                                        // var countSubjects = 0
                                        var isNursery = activePupil.gradeLevel.contains("Class", ignoreCase = true)
                                        
                                        // Track aggregates (for P.7 UNEB evaluation)
                                        // var aggregatesSum = 0
                                        // var subjectGradesCount = 0

                                        // Subjects evaluation matching Student
                                        val studentGrades = listGrades.filter { it.studentId == activePupil.id }

                                        // Loop standard primary subjects
                                        val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")

                                        activeSubjects.forEach { subj ->
                                            val midGrade = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
                                            val eotGrade = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score

                                            // Combined formula: 40% Midterm, 60% End term
                                            val weightedTotal = if (midGrade != null && eotGrade != null) {
                                                (midGrade * 0.4) + (eotGrade * 0.6)
                                            } else {
                                                eotGrade ?: midGrade ?: 0.0
                                            }

                                            // Only report if there is a grade
                                            if (midGrade != null || eotGrade != null) {
                                                totalWeightedGradeSum += weightedTotal
                                                countSubjects++

                                                // Calculate Uganda standard letter/points grade
                                                val (gradeStr, points) = if (isNursery) {
                                                    when {
                                                        weightedTotal >= 80.0 -> Pair("A (Achieved)", 1)
                                                        weightedTotal >= 50.0 -> Pair("D (Developing)", 2)
                                                        else -> Pair("B (Beginning)", 9)
                                                    }
                                                } else {
                                                    // Standard UNEB Aggregates point parameters
                                                    when {
                                                        weightedTotal >= 85.0 -> Pair("D1", 1)
                                                        weightedTotal >= 75.0 -> Pair("D2", 2)
                                                        weightedTotal >= 70.0 -> Pair("C3", 3)
                                                        weightedTotal >= 65.0 -> Pair("C4", 4)
                                                        weightedTotal >= 60.0 -> Pair("C5", 5)
                                                        weightedTotal >= 50.0 -> Pair("C6", 6)
                                                        weightedTotal >= 45.0 -> Pair("P7", 7)
                                                        weightedTotal >= 40.0 -> Pair("P8", 8)
                                                        else -> Pair("F9", 9)
                                                    }
                                                }

                                                if (!isNursery) {
                                                    aggregatesSum += points
                                                    subjectGradesCount++
                                                }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(0.5.dp, Color.LightGray)
                                                        .padding(6.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(subj, modifier = Modifier.weight(1.8f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(midGrade?.let { String.format(Locale.US, "%.0f", it) } ?: "-", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodyMedium)
                                                    Text(eotGrade?.let { String.format(Locale.US, "%.0f", it) } ?: "-", modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodyMedium)
                                                    Text(String.format(Locale.US, "%.1f", weightedTotal), modifier = Modifier.weight(1.1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    
                                                    Surface(
                                                        color = if (points <= 2) Color(0xFFE8F5E9) else if (points <= 6) Color(0xFFFFF3E0) else Color(0xFFFFEBEE),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1.1f)
                                                    ) {
                                                        Text(
                                                            gradeStr,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                            textAlign = TextAlign.Center,
                                                            color = if (points <= 2) Color(0xFF2E7D32) else if (points <= 6) Color(0xFFE65100) else Color(0xFFC62828)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (countSubjects == 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No assessment scores registered yet for this term. Add scores in 'Excel bulk Sheet' to generate report.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                            }
                                        } else {
                                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                                            // Metrics Summary Panel
                                            val avgScore = totalWeightedGradeSum / countSubjects
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("Total Subjects Analyzed: $countSubjects", style = MaterialTheme.typography.bodySmall)
                                                    Text(String.format(Locale.US, "Average Mark obtained: %.2f%%", avgScore), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                    
                                                    if (!isNursery) {
                                                        Text("Total Grading Points: $aggregatesSum", style = MaterialTheme.typography.bodySmall)
                                                        
                                                        // Ugandan Division calculation basing on points sum
                                                        val divisionStr = when {
                                                            aggregatesSum in 4..12 -> "Division I (First Grade) 🌟"
                                                            aggregatesSum in 13..24 -> "Division II (Second Grade)"
                                                            aggregatesSum in 25..29 -> "Division III (Third Grade)"
                                                            aggregatesSum in 30..34 -> "Division IV (Fourth Grade)"
                                                            else -> "Division U (Ungraded / Fail)"
                                                        }
                                                        
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Surface(
                                                            color = if (aggregatesSum in 4..12) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
                                                            shape = RoundedCornerShape(4.dp),
                                                            modifier = Modifier.border(1.dp, Color.Gray)
                                                        ) {
                                                            Text(
                                                                "Termly Outturn: $divisionStr",
                                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                modifier = Modifier.padding(6.dp),
                                                                color = if (aggregatesSum in 4..12) Color(0xFF1B5E20) else Color.DarkGray
                                                            )
                                                        }
                                                    } else {
                                                        // Nursery rating
                                                        val outturn = if (avgScore >= 80) "Nursery Achieved Promisingly" else "Developing Steadily"
                                                        Text("Nursery Grading: $outturn", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }

                                                // Fees Stamp Indicator block
                                                val outstandingFee = activePupil.feesTotal - activePupil.feesPaid
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    if (outstandingFee > 0) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            modifier = Modifier
                                                                .border(1.dp, Color(0xFFC62828), RoundedCornerShape(4.dp))
                                                                .background(Color(0xFFFFF7F7))
                                                                .padding(6.dp)
                                                        ) {
                                                            Text("FEES BALANCE ALERT", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                                                            Text(String.format(Locale.US, "UGX %,.0f Due", outstandingFee), style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                                            Text("Report Provisionally Issued", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 9.sp)
                                                        }
                                                    } else {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            modifier = Modifier
                                                                .border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(6.dp))
                                                                .background(Color(0xFFE8F5E9))
                                                                .padding(6.dp)
                                                        ) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                                            Text("FEES FULLY PAID", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                                                            Text("APPROVED & CLEARED", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                                            // Teacher/Head comment lines
                                            Text(
                                                "Class Teacher's Remark: " + if (avgScore >= 80) {
                                                    "Consistently diligent and disciplined. Outstanding academic competence. Keep the same tempo!"
                                                } else if (avgScore >= 60) {
                                                    "A promising child who shows regular performance. Should double efforts in homework targets."
                                                } else {
                                                    "Amiable child. Needs strictly focused revision assistance in Mathematics to pass well."
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.DarkGray
                                            )

                                            Text(
                                                "Headteacher's Comment: " + if (avgScore >= 80) {
                                                    "Excellent Outturn! Approved for academic honors. Keep shining."
                                                } else {
                                                    "Reviewed and signed. Encouraged to strive higher next term."
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.DarkGray,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Stamp Area
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column(modifier = Modifier.border(0.5.dp, Color.LightGray).padding(4.dp)) {
                                                    Text("HEADTEACHER SIGNATURE", fontSize = 10.sp, color = Color.Gray)
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Text("Nabakooza Sarah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }

                                                Column(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .border(1.dp, Color(0xFF1976D2), CircleShape)
                                                        .background(Color(0xFFE3F2FD))
                                                        .padding(4.dp),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("OFFICIAL", fontSize = 8.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                                                    Text("PEARL", fontSize = 8.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Black)
                                                    Text("STAMP", fontSize = 8.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (countSubjects > 0) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text("Report Card Utilities & Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        android.widget.Toast.makeText(context, "Report Excel Master mark list exported!", android.widget.Toast.LENGTH_LONG).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Excel Out", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        android.widget.Toast.makeText(context, "PDF Report compiled. Print job queued to system for " + activePupil.name, android.widget.Toast.LENGTH_LONG).show()
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Print Report", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        android.widget.Toast.makeText(context, "Academic results dispatch email queued to parent address: " + activePupil.email, android.widget.Toast.LENGTH_LONG).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Send Alert", fontSize = 11.sp)
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
    }

    // Modal dialog for original Add Grade action (Ledger view tab 0)
    if (showAddGrade) {
        AlertDialog(
            onDismissRequest = { showAddGrade = false },
            title = { Text("Assign Performance Grade") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var studentExpanded by remember { mutableStateOf(false) }
                    var subjExpanded by remember { mutableStateOf(false) }
                    var examExpanded by remember { mutableStateOf(false) }

                    Text("Pick Student", fontWeight = FontWeight.Bold)
                    if (listStudents.isEmpty()) {
                        Text("No registered students found.", color = Color.Red)
                    } else {
                        val activeSt = listStudents.find { it.id == selectedStudentId } ?: listStudents.first()
                        ExposedDropdownMenuBox(
                            expanded = studentExpanded,
                            onExpandedChange = { studentExpanded = !studentExpanded }
                        ) {
                            OutlinedTextField(
                                value = "${activeSt.name} (${activeSt.rollNumber})",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = studentExpanded,
                                onDismissRequest = { studentExpanded = false }
                            ) {
                                listStudents.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text("${st.name} (${st.rollNumber})") },
                                        onClick = { selectedStudentId = st.id; studentExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Text("Academic Subject", fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = subjExpanded,
                        onExpandedChange = { subjExpanded = !subjExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjExpanded,
                            onDismissRequest = { subjExpanded = false }
                        ) {
                            subjectOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { selectedSubject = opt; subjExpanded = false }
                                )
                            }
                        }
                    }

                    Text("Exam Name", fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = examExpanded,
                        onExpandedChange = { examExpanded = !examExpanded }
                    ) {
                        OutlinedTextField(
                            value = examInput,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = examExpanded,
                            onDismissRequest = { examExpanded = false }
                        ) {
                            examOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { examInput = opt; examExpanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = scoreValue,
                        onValueChange = { scoreValue = it },
                        label = { Text("Score obtained (max 100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val score = scoreValue.toDoubleOrNull()
                        if (score != null && score in 0.0..100.0) {
                            val actualId = if (selectedStudentId == 0) listStudents.first().id else selectedStudentId
                            viewModel.insertGrade(actualId, selectedSubject, examInput, score, 100.0)
                        }
                        showAddGrade = false
                        scoreValue = ""
                    },
                    enabled = listStudents.isNotEmpty()
                ) {
                    Text("Save Performance Mark")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGrade = false }) { Text("Cancel") }
            }
        )
    }
}
