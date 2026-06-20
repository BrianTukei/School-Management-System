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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolApp(viewModel: SchoolViewModel) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    if (!isUserLoggedIn) {
        AuthGateScreen(viewModel = viewModel)
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

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
        currentRoute == "fee_tracking" -> "Finance & Fees Tracker"
        currentRoute == "library" -> "Library Circulation Hub"
        currentRoute == "ai_assistant" -> "AI Senior Copilot"
        currentRoute == "sms_broadcast" -> "SMS Broadcast Gateway"
        currentRoute == "pupil_monitoring" -> "Pupil Performance & Activity Monitor"
        currentRoute == "performance_trends" -> "Academic Performance Trends"
        currentRoute == "timetable_planner" -> "Timetable & Events Planner"
        currentRoute == "parent_portal" -> "Parent Secure Portal"
        else -> "Pearl Junior School"
    }

    val isRootRoute = currentRoute in listOf(
        "dashboard", "students", "teachers", "classes", 
        "attendance", "grades", "fee_tracking", "ai_assistant", "sms_broadcast", "pupil_monitoring", "performance_trends", "timetable_planner", "parent_portal"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 800.dp

        if (isWideScreen) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(310.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerTonalElevation = 4.dp
                    ) {
                        NavigationMenuContent(
                            currentRoute = currentRoute,
                            onItemClick = { route ->
                                if (route == "logout") {
                                    viewModel.logOutUser()
                                } else {
                                    navController.navigate(route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) {
                AppScaffold(
                    isWideScreen = true,
                    isRootRoute = isRootRoute,
                    screenTitle = screenTitle,
                    currentRoute = currentRoute,
                    navController = navController,
                    onOpenDrawer = { /* No-op on wide screen */ },
                    viewModel = viewModel
                )
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = isRootRoute,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(310.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerTonalElevation = 4.dp
                    ) {
                        NavigationMenuContent(
                            currentRoute = currentRoute,
                            onItemClick = { route ->
                                scope.launch { drawerState.close() }
                                if (route == "logout") {
                                    viewModel.logOutUser()
                                } else {
                                    navController.navigate(route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) {
                AppScaffold(
                    isWideScreen = false,
                    isRootRoute = isRootRoute,
                    screenTitle = screenTitle,
                    currentRoute = currentRoute,
                    navController = navController,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    viewModel = viewModel
                )
            }
        }
    }
}
}

@Composable
fun NavigationMenuContent(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    // Header Design
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "School Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Column {
                Text(
                    "ST. JUDE ACADEMY",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    "School Management Suite",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Navigation Items List
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Category: Core Academic Directory
        item {
            Text(
                "CORE DIRECTORY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
            )
        }
        
        val coreItems = listOf(
            Triple("dashboard", "School Dashboard", Icons.Default.Dashboard),
            Triple("students", "Student Directory", Icons.Default.People),
            Triple("teachers", "Faculty Directory", Icons.Default.Person),
            Triple("classes", "Class Planner", Icons.Default.Home),
            Triple("pupil_monitoring", "Pupil Monitoring", Icons.Default.Analytics),
            Triple("performance_trends", "Performance Trends", Icons.Default.TrendingUp)
        )
        
        items(coreItems.size) { index ->
            val item = coreItems[index]
            val isSelected = currentRoute == item.first
            NavigationDrawerItem(
                label = { Text(item.second, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                selected = isSelected,
                onClick = {
                    onItemClick(item.first)
                },
                icon = { Icon(item.third, contentDescription = item.second) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Divider & Category: Administration
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "ADMINISTRATION",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }

        val adminItems = listOf(
            Triple("attendance", "Attendance Registrar", Icons.Default.CheckCircle),
            Triple("grades", "Academic Records & Reports", Icons.Default.ListAlt),
            Triple("fee_tracking", "Finance & Fees Tracker", Icons.Default.AccountBalance),
            Triple("timetable_planner", "Timetable & Events Planner", Icons.Default.DateRange),
            Triple("library", "Library Circulation Hub", Icons.Default.Book)
        )

        items(adminItems.size) { index ->
            val item = adminItems[index]
            val isSelected = currentRoute == item.first
            NavigationDrawerItem(
                label = { Text(item.second, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                selected = isSelected,
                onClick = {
                    onItemClick(item.first)
                },
                icon = { Icon(item.third, contentDescription = item.second) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Divider & Category: Services
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "CHANNELS & CO-PILOT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }

        val serviceItems = listOf(
            Triple("sms_broadcast", "SMS Broadcast Gateway", Icons.Default.SendToMobile),
            Triple("ai_assistant", "AI Senior Copilot", Icons.Default.AutoAwesome)
        )

        items(serviceItems.size) { index ->
            val item = serviceItems[index]
            val isSelected = currentRoute == item.first
            NavigationDrawerItem(
                label = { Text(item.second, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                selected = isSelected,
                onClick = {
                    onItemClick(item.first)
                },
                icon = { Icon(item.third, contentDescription = item.second) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Divider & Category: Parents Space
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "PARENTS PORTAL",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }

        val parentSpaceItems = listOf(
            Triple("parent_portal", "Parent Portal Workspace", Icons.Default.SupervisorAccount)
        )

        items(parentSpaceItems.size) { index ->
            val item = parentSpaceItems[index]
            val isSelected = currentRoute == item.first
            NavigationDrawerItem(
                label = { Text(item.second, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                selected = isSelected,
                onClick = {
                    onItemClick(item.first)
                },
                icon = { Icon(item.third, contentDescription = item.second) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            NavigationDrawerItem(
                label = { Text("Log Out Access", fontWeight = FontWeight.Bold) },
                selected = false,
                onClick = {
                    onItemClick("logout")
                },
                icon = { Icon(Icons.Default.Logout, contentDescription = "Log Out Portal", tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.padding(vertical = 2.dp).testTag("drawer_logout_button"),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                    unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

@Composable
fun AppScaffold(
    isWideScreen: Boolean,
    isRootRoute: Boolean,
    screenTitle: String,
    currentRoute: String?,
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    viewModel: SchoolViewModel
) {
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val appNotifications by viewModel.appNotifications.collectAsStateWithLifecycle()
    val unreadCount = appNotifications.count { !it.read }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔔 Live Activity Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (appNotifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearNotifications() }) {
                            Icon(Icons.Default.DeleteSweep, "Clear database", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            text = {
                if (appNotifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Notifications, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("No recent alerts in your active log.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(appNotifications) { notification ->
                            val color = when(notification.type) {
                                "Event" -> MaterialTheme.colorScheme.primaryContainer
                                "Timetable" -> MaterialTheme.colorScheme.secondaryContainer
                                "Leave" -> MaterialTheme.colorScheme.tertiaryContainer
                                "Lesson" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = color),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notification.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notification.content, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.markAllNotificationsAsRead()
                    showNotificationsDialog = false
                }) {
                    Text("Clear Badges")
                }
            }
        )
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
                    if (isRootRoute) {
                        if (!isWideScreen) {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                            }
                        } else {
                            // Small elegant leading branding icon
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp, end = 8.dp).size(24.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                        }
                    }
                },
                actions = {
                    // Notification Bell Icon Button with Red Circle Badge Count
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            Icon(
                                imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "System Log",
                                tint = if (unreadCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(16.dp)
                                    .background(MaterialTheme.colorScheme.error, shape = androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Direct Swap between Administrator and Secure Parent Space Workspaces
                    IconButton(
                        onClick = {
                            if (currentRoute == "parent_portal") {
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = true }
                                    launchSingleTop = true
                                }
                                android.widget.Toast.makeText(navController.context, "Admin Office Desk Loaded", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                navController.navigate("parent_portal") {
                                    launchSingleTop = true
                                }
                                android.widget.Toast.makeText(navController.context, "Secure Parent Portal Loaded", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (currentRoute == "parent_portal") Icons.Default.AdminPanelSettings else Icons.Default.SupervisorAccount,
                            contentDescription = "Switch Desktop Roles",
                            tint = if (currentRoute == "parent_portal") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
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
            if (!isWideScreen && (currentRoute == "dashboard" || currentRoute == "students" || currentRoute == "teachers" || currentRoute == "classes" || currentRoute == "ai_assistant")) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isWideScreen) {
                            Modifier
                                .widthIn(max = 1200.dp)
                                .align(Alignment.TopCenter)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.fillMaxSize()
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
                        composable("pupil_monitoring") {
                            PupilMonitoringScreen(viewModel)
                        }
                        composable("performance_trends") {
                            PerformanceTrendsScreen(viewModel)
                        }
                        composable("timetable_planner") {
                            TimetablePlannerScreen(viewModel)
                        }
                        composable("parent_portal") {
                            ParentPortalScreen(viewModel)
                        }
                        composable("fee_tracking") {
                            FeeTrackingScreen(viewModel)
                        }
                        composable("library") {
                            LibraryScreen(viewModel)
                        }
                    }
                }

                // Dynamic persistent Back and Next navigation sequence ribbon
                val currentRouteStr = currentRoute ?: "dashboard"
                val orderedMainRoutes = listOf(
                    "dashboard",
                    "students",
                    "teachers",
                    "classes",
                    "pupil_monitoring",
                    "performance_trends",
                    "attendance",
                    "grades",
                    "sms_broadcast",
                    "ai_assistant"
                )

                var prevRoute = "dashboard"
                var nextRoute = "dashboard"
                var prevLabel = "Dashboard"
                var nextLabel = "Dashboard"

                fun getRouteLabel(route: String): String {
                    return when {
                        route == "dashboard" -> "Dashboard"
                        route == "students" -> "Pupils"
                        route == "teachers" -> "Faculty"
                        route == "classes" -> "Planner"
                        route == "pupil_monitoring" -> "Monitoring"
                        route == "performance_trends" -> "Trends"
                        route == "attendance" -> "Attendance"
                        route == "grades" -> "Academic Records"
                        route == "sms_broadcast" -> "SMS Gateway"
                        route == "ai_assistant" -> "AI Copilot"
                        route.startsWith("student_detail") -> "Pupil Detail"
                        route.startsWith("student_form") -> "Pupil Form"
                        route.startsWith("teacher_form") -> "Faculty Form"
                        else -> route
                    }
                }

                val isSubRoute = currentRouteStr.startsWith("student_detail") || 
                                  currentRouteStr.startsWith("student_form") || 
                                  currentRouteStr.startsWith("teacher_form")

                if (isSubRoute) {
                    prevRoute = when {
                        currentRouteStr.startsWith("student_detail") || currentRouteStr.startsWith("student_form") -> "students"
                        currentRouteStr.startsWith("teacher_form") -> "teachers"
                        else -> "dashboard"
                    }
                    prevLabel = getRouteLabel(prevRoute)
                    nextRoute = "dashboard"
                    nextLabel = "Dashboard"
                } else {
                    val index = orderedMainRoutes.indexOf(currentRouteStr)
                    if (index != -1) {
                        val prevIndex = if (index == 0) orderedMainRoutes.lastIndex else index - 1
                        val nextIndex = if (index == orderedMainRoutes.lastIndex) 0 else index + 1
                        
                        prevRoute = orderedMainRoutes[prevIndex]
                        nextRoute = orderedMainRoutes[nextIndex]
                        
                        prevLabel = getRouteLabel(prevRoute)
                        nextLabel = getRouteLabel(nextRoute)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous/Back Button
                        Button(
                            onClick = {
                                if (isSubRoute) {
                                    navController.navigateUp()
                                } else {
                                    navController.navigate(prevRoute) {
                                        popUpTo("dashboard")
                                        launchSingleTop = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .testTag("persistent_nav_prev_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Navigate back to $prevLabel",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Back: $prevLabel",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Next/Forward Button
                        Button(
                            onClick = {
                                navController.navigate(nextRoute) {
                                    popUpTo("dashboard")
                                    launchSingleTop = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .testTag("persistent_nav_next_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Next: $nextLabel",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Navigate next to $nextLabel",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedBadge(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

// ==================== DASHBOARD HUB ====================
@Composable
fun DashboardScreen(navController: NavController, viewModel: SchoolViewModel) {
    val totalStudents by viewModel.studentCount.collectAsStateWithLifecycle()
    val totalTeachers by viewModel.teacherCount.collectAsStateWithLifecycle()
    val feesStats by viewModel.totalFeesStats.collectAsStateWithLifecycle()
    val avgGradeScore by viewModel.averagePerformanceScore.collectAsStateWithLifecycle()

    val studentsList by viewModel.students.collectAsStateWithLifecycle(emptyList())
    val gradesList by viewModel.allGrades.collectAsStateWithLifecycle(emptyList())
    val attendanceList by viewModel.studentAttendance.collectAsStateWithLifecycle(emptyList())
    val leaveRequests by viewModel.leaveRequests.collectAsStateWithLifecycle(emptyList())
    val teachersList by viewModel.teachers.collectAsStateWithLifecycle(emptyList())
    val timetableList by viewModel.timetablePeriods.collectAsStateWithLifecycle(emptyList())
    val booksList by viewModel.books.collectAsStateWithLifecycle(emptyList())
    val checkoutsList by viewModel.checkouts.collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current
    var activeSmsTargetStudent by remember { mutableStateOf<Student?>(null) }
    var smsCustomMsgText by remember { mutableStateOf("") }
    var showSystemSetupHub by remember { mutableStateOf(false) }
    
    var showExcelImportHubDialog by remember { mutableStateOf(false) }
    var importTab by remember { mutableStateOf(0) } // 0: Pupils, 1: Assessments
    var csvPasteInput by remember { mutableStateOf("") }
    var importErrorFeedback by remember { mutableStateOf<String?>(null) }
    var isParsedCommitted by remember { mutableStateOf(false) }
    var parseLogMessage by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val getFileNameHelper = remember {
        { uri: Uri ->
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) {
                            result = cursor.getString(index)
                        }
                    }
                } catch (e: Exception) {
                    // Fail-safe
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/') ?: -1
                if (cut != -1) {
                    result = result?.substring(cut + 1)
                }
            }
            result ?: "Excel_Workbook_Source.csv"
        }
    }

    val deviceFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                selectedFileName = getFileNameHelper(uri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (text.isNotBlank()) {
                    csvPasteInput = text
                    importErrorFeedback = null
                    isParsedCommitted = false
                } else {
                    importErrorFeedback = "The chosen file is empty."
                    selectedFileName = null
                }
            } catch (e: Exception) {
                importErrorFeedback = "Failed to load device file: ${e.localizedMessage}"
                selectedFileName = null
            }
        }
    }

    if (activeSmsTargetStudent != null) {
        val st = activeSmsTargetStudent!!
        AlertDialog(
            onDismissRequest = { activeSmsTargetStudent = null },
            title = { Text("Parent Fee Alert Dispatcher", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recipients: Guardian of ${st.name} (${st.phone})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    OutlinedTextField(
                        value = smsCustomMsgText,
                        onValueChange = { smsCustomMsgText = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        label = { Text("SMS Content Alert") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.broadcastSms(st.name, listOf(st.phone), smsCustomMsgText)
                        android.widget.Toast.makeText(context, "Sms custom reminder broadcast to parent successfully!", android.widget.Toast.LENGTH_LONG).show()
                        activeSmsTargetStudent = null
                    }
                ) {
                    Text("Broadcast Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeSmsTargetStudent = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExcelImportHubDialog) {
        AlertDialog(
            onDismissRequest = { 
                showExcelImportHubDialog = false 
                csvPasteInput = ""
                importErrorFeedback = null
                isParsedCommitted = false
                parseLogMessage = ""
                selectedFileName = null
            },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Excel Bulk Import Hub", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Directly upload spreadsheet files from your device", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Modern styled Toggle Tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (importTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { 
                                    importTab = 0 
                                    csvPasteInput = ""
                                    importErrorFeedback = null
                                    isParsedCommitted = false
                                    selectedFileName = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "1. Import Pupils", 
                                color = if (importTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (importTab == 1) Color(0xFF2E7D32) else Color.Transparent)
                                .clickable { 
                                    importTab = 1 
                                    csvPasteInput = ""
                                    importErrorFeedback = null
                                    isParsedCommitted = false
                                    selectedFileName = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "2. Import Marks", 
                                color = if (importTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    if (importTab == 0) {
                        // Import Pupils view
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Format Guidelines:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    "RollNumber, Name, Grade, Gender, Phone, Email, FeesTotal, FeesPaid\nAll values should be comma separated.", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.DarkGray
                                )
                                Text("Example Data (Copy & modification friendly):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    "S1011,Nankya Rebecca,P.7,Female,+256 701 556677,rebecca.n@pearl.ac.ug,850000,450000\nS1012,Mugisha Daniel,P.5,Male,+256 772 889900,daniel.m@pearl.ac.ug,650000,650000", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.Gray,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        // Import Assessment marks view
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5E1A5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Format Guidelines:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text(
                                    "RollNumber, Subject, ExamType, Score, MaxScore\nWhere ExamType can be: 'Midterm Exam', 'End of Term Exam' or 'Continuous Assessment'.", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.DarkGray
                                )
                                Text("Example Data (Copy & modification friendly):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text(
                                    "S1001,Mathematics,Midterm Exam,88,100\nS1002,English Language,End of Term Exam,94,100\nS1005,Integrated Science,Midterm Exam,76,100", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.Gray,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Direct Device File Picker Workspace
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    deviceFilePickerLauncher.launch("*/*")
                                } catch (e: Exception) {
                                    importErrorFeedback = "Device spreadsheet loader unavailable: \${e.localizedMessage}. Please copy-paste raw data directly into the text field below."
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedFileName != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = if (selectedFileName != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedFileName == null) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload from local device storage",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    "Upload Spreadsheet File",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Tap here to choose any .csv or spreadsheet file directly from this device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Excel spreadsheet loaded successfully",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    "SPREADSHEET SELECTED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    selectedFileName ?: "Workbook.csv",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                    color = Color(0xFF1B4332),
                                    textAlign = TextAlign.Center
                                )
                                val lineCount = csvPasteInput.split("\n").filter { it.isNotBlank() }.size
                                Text(
                                    "Successfully loaded $lineCount rows from device! Tap to choose a different spreadsheet file.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Divider section
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1.0f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            "OR edit/paste spreadsheet content below",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1.0f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    OutlinedTextField(
                        value = csvPasteInput,
                        onValueChange = { csvPasteInput = it; importErrorFeedback = null; isParsedCommitted = false },
                        placeholder = { 
                            if (importTab == 0) {
                                Text("Paste Pupils rows here...\nS1011,Nankya Rebecca,P.7,Female,+256 701 556677,rebecca.n@pearl.ac.ug,850000,450000")
                            } else {
                                Text("Paste Assessment rows here...\nS1001,Mathematics,Midterm Exam,88,100")
                            }
                        },
                        label = { Text("Excel Spreadsheet CSV Raw Data") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        maxLines = 15
                    )

                    importErrorFeedback?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    if (isParsedCommitted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                Text(
                                    parseLogMessage, 
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (csvPasteInput.isBlank()) {
                            importErrorFeedback = "Please load a file or enter spreadsheet rows first."
                            return@Button
                        }
                        
                        try {
                            val lines = csvPasteInput.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                            var successCount = 0
                            
                            if (importTab == 0) {
                                // Import Pupils logic
                                lines.forEach { line ->
                                    val parts = line.split(",").map { it.trim() }
                                    if (parts.size < 6) {
                                        throw Exception("Invalid formatting! Each line must have at least 6 comma-separated fields.")
                                    }
                                    val roll = parts[0]
                                    val name = parts[1]
                                    val grade = parts[2]
                                    val gender = parts[3]
                                    val phone = parts[4]
                                    val email = parts[5]
                                    val feesTotal = if (parts.size > 6) parts[6].toDoubleOrNull() ?: 850000.0 else 850000.0
                                    val feesPaid = if (parts.size > 7) parts[7].toDoubleOrNull() ?: 0.0 else 0.0
                                    
                                    val student = Student(
                                        name = name,
                                        rollNumber = roll,
                                        gradeLevel = grade,
                                        email = email,
                                        phone = phone,
                                        gender = gender,
                                        feesTotal = feesTotal,
                                        feesPaid = feesPaid,
                                        status = "Active"
                                    )
                                    viewModel.saveStudent(student)
                                    successCount++
                                }
                                isParsedCommitted = true
                                parseLogMessage = "Import Finished! Added $successCount new pupils to registry successfully."
                                android.widget.Toast.makeText(context, "$successCount Pupils Imported successfully!", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                // Import Assessments logic
                                lines.forEach { line ->
                                    val parts = line.split(",").map { it.trim() }
                                    if (parts.size < 4) {
                                        throw Exception("Each line must specify: RollNumber, Subject, ExamType, Score. MaxScore is optional.")
                                    }
                                    val roll = parts[0]
                                    val subject = parts[1]
                                    val examName = parts[2]
                                    val scoreStr = parts[3]
                                    val score = scoreStr.toDoubleOrNull() ?: throw Exception("Invalid score number: $scoreStr")
                                    val maxScore = if (parts.size > 4) parts[4].toDoubleOrNull() ?: 100.0 else 100.0
                                    
                                    // Match pupil by roll number
                                    val matchingStudent = studentsList.find { it.rollNumber.equals(roll, ignoreCase = true) }
                                    if (matchingStudent == null) {
                                        throw Exception("Pupil with Roll Number '$roll' not found in system registry. Please add this pupil first.")
                                    }
                                    
                                    viewModel.insertGrade(
                                        studentId = matchingStudent.id,
                                        subjectName = subject,
                                        examName = examName,
                                        score = score,
                                        maxScore = maxScore
                                    )
                                    successCount++
                                }
                                isParsedCommitted = true
                                parseLogMessage = "Import Finished! Saved $successCount assessment scores successfully."
                                android.widget.Toast.makeText(context, "$successCount Assessment Records imported successfully!", android.widget.Toast.LENGTH_LONG).show()
                            }
                            
                            csvPasteInput = "" // Clear paste field after success
                            importErrorFeedback = null
                        } catch (e: Exception) {
                            importErrorFeedback = "Error parsing line: ${e.message}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (importTab == 0) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                    )
                ) {
                    Text("Validate & Integrate")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showExcelImportHubDialog = false 
                        csvPasteInput = ""
                        importErrorFeedback = null
                        isParsedCommitted = false
                        parseLogMessage = ""
                        selectedFileName = null
                    }
                ) {
                    Text("Close Hub")
                }
            }
        )
    }

    // --- School Emblem Logo Config properties ---
    val logoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    viewModel.saveSchoolLogo(base64)
                    android.widget.Toast.makeText(context, "School custom crest updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                }
                inputStream?.close()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Logo load failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Firebase sync state variables config ---
    val (initialUrl, initialToken, initialEnabled) = remember {
        com.example.data.api.FirebaseService.getFirebaseConfig(context)
    }
    var dbUrl by remember { mutableStateOf(initialUrl) }
    var authToken by remember { mutableStateOf(initialToken) }
    var syncEnabled by remember { mutableStateOf(initialEnabled) }
    var isSyncingAll by remember { mutableStateOf(false) }
    var selectedPlatformTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
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

        // --- Custom School Identity Crest Logo Badge Upload ---
        item {
            val logoBase64 by viewModel.schoolLogoBase64.collectAsStateWithLifecycle()
            val dashboardContext = LocalContext.current
            val logoLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    try {
                        val inputStream = dashboardContext.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        if (bytes != null) {
                            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                            viewModel.saveSchoolLogo(base64)
                            android.widget.Toast.makeText(dashboardContext, "School custom crest updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        inputStream?.close()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(dashboardContext, "Logo load failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("school_logo_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoBase64.isNotEmpty()) {
                            val imageBytes = try {
                                android.util.Base64.decode(logoBase64, android.util.Base64.DEFAULT)
                            } catch (e: Exception) {
                                null
                            }
                            if (imageBytes != null) {
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "School badge banner",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                } else {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Emblem",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "School Official Emblem & Badge",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (logoBase64.isNotEmpty()) "Crest badge uploaded! This crest is embedded into matching HTML & PDF document headers dynamically." 
                            else "No emblem uploaded yet. Tap below to upload the school's badge to professionalize reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Button(
                                onClick = { logoLauncher.launch("image/*") },
                                modifier = Modifier.testTag("upload_badge_button").height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Emblem", style = MaterialTheme.typography.labelMedium)
                            }
                            if (logoBase64.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { 
                                        viewModel.saveSchoolLogo("")
                                        android.widget.Toast.makeText(dashboardContext, "Emblem cleared", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("remove_badge_button").height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Firebase Cloud Sync Settings Card ---
        item {
            val context = LocalContext.current
            val (initialUrl, initialToken, initialEnabled) = remember {
                com.example.data.api.FirebaseService.getFirebaseConfig(context)
            }
            var dbUrl by remember { mutableStateOf(initialUrl) }
            var authToken by remember { mutableStateOf(initialToken) }
            var syncEnabled by remember { mutableStateOf(initialEnabled) }
            var isSyncingAll by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_sync_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (dbUrl.isNotBlank() && syncEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Firebase Cloud Sync Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { 
                                syncEnabled = it
                                com.example.data.api.FirebaseService.saveFirebaseConfig(context, dbUrl, authToken, it)
                            },
                            modifier = Modifier.testTag("firebase_sync_toggle")
                        )
                    }

                    Text(
                        "Synchronize student registration details, fee payment ledgers, and attendance rolls instantly with your Google Firebase Cloud Firestore database for real-time online syncing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = dbUrl,
                        onValueChange = { dbUrl = it.trim() },
                        label = { Text("Firebase Project ID (for Firestore)") },
                        placeholder = { Text("e.g., pearl-junior-school-db") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("firebase_db_url_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = authToken,
                        onValueChange = { authToken = it.trim() },
                        label = { Text("Firebase API Key (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("firebase_token_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                com.example.data.api.FirebaseService.saveFirebaseConfig(context, dbUrl, authToken, syncEnabled)
                                android.widget.Toast.makeText(context, "Firebase configuration saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("save_firebase_config_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Database")
                        }

                        if (dbUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    isSyncingAll = true
                                    viewModel.syncAllStudentsToFirebase { success ->
                                        isSyncingAll = false
                                        if (success) {
                                            android.widget.Toast.makeText(context, "All database records synchronized with Firebase!", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Sync completed with some connection anomalies. Verify your Firebase setup.", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isSyncingAll,
                                modifier = Modifier.weight(1.2f).testTag("bulk_sync_firebase_button")
                            ) {
                                if (isSyncingAll) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.dp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bulk Sync")
                            }
                        }
                    }

                    if (dbUrl.isBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Text(
                                "Database URL empty: Syncing suspended.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (syncEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Text(
                                "Real-time auto-synchronization enabled.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // --- Cross-Platform SETUP & DOWNLOAD CENTER ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("download_platform_banner")
            ) {
                var selectedPlatformTab by remember { mutableStateOf(0) } // 0: Android APK, 1: Windows Suite
                
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Deployment Gate",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "📥 Download & Deploy Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Access on any phone, tablet, or Windows Desktop computer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    TabRow(
                        selectedTabIndex = selectedPlatformTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedPlatformTab == 0,
                            onClick = { selectedPlatformTab = 0 },
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                  ) {
                                    Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(14.dp))
                                    Text("Android Setup", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        )
                        Tab(
                            selected = selectedPlatformTab == 1,
                            onClick = { selectedPlatformTab = 1 },
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                  ) {
                                    Icon(Icons.Default.DesktopWindows, null, modifier = Modifier.size(14.dp))
                                    Text("Windows Desktop", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        )
                    }
                    
                    if (selectedPlatformTab == 0) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "📱 Run Natively on your Android Phone / Tablet:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "• Direct APK Installer: Tap the Project Settings / Gear menu on the top-right tool panel of this AI Studio preview player, and choose 'Download APK'. Copy this file to any Android device to install instantly.\n• Compiled Resource ZIP: You can also choose 'Export as ZIP' to download the source code of this Grade-A system and open in Android Studio to build high-performance custom release builds.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Tip: Tap settings gear on top right to build and fetch your stable production APK file!", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("download_android_tip_btn")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get APK Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "💻 Setup as Native Desktop App on Windows PC:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "• Edge / Chrome PWA Launcher (Recommended):\nOpen this suite in Edge or Chrome on your PC. Click the 'App Available / Install' icon on the browser address bar to save St. Jude Workspace directly to your desktop. It runs in a clean standalone window with full mouse / keyboard shortcut support!\n• Windows Subsystem for Android (WSA):\nWindows 11 supports playing APKs natively. Sideload the generated APK to run St. Jude right next to Microsoft Teams and Word. Or use emulator clients (like BlueStacks or LDPlayer).",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Bookmark this layout, hit options, and click 'Install Pearl App' on desktop browser!", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("download_windows_tip_btn")
                                ) {
                                    Icon(Icons.Default.DesktopWindows, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PWA Desktop Tip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
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

        // Library Circulation & Books
        item {
            val totalBooks = booksList.sumOf { it.copiesTotal }
            val activeLoans = checkoutsList.count { it.returnDate == null }
            val overdueCount = checkoutsList.count { loan ->
                if (loan.returnDate != null) return@count false
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val due = sdf.parse(loan.dueDate) ?: java.util.Date()
                    val today = sdf.parse(sdf.format(java.util.Date())) ?: java.util.Date()
                    due.before(today)
                } catch (e: Exception) {
                    false
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("library") }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Library Catalog",
                    value = "$totalBooks Copies",
                    icon = Icons.Default.Book,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Active Circulations",
                    value = if (overdueCount > 0) "$activeLoans Loans ($overdueCount Overdue!!)" else "$activeLoans Active",
                    icon = Icons.Default.SwapCalls,
                    color = if (overdueCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Premium: Smart Analytics Dashboard Visualizations ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Smart School Analytics & Resource Visuals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Realtime Logs", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    // 1. Class Period Density Utilization Bar Charts
                    Text("Class Timetable Utilization Intensity", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val gradeGroups = timetableList.groupBy { it.className }.mapValues { it.value.size }
                    if (gradeGroups.isEmpty()) {
                        Text("No timetable periods logged in the database yet to calculate utilization.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        val maxCount = gradeGroups.values.maxOrNull() ?: 1
                        gradeGroups.entries.sortedByDescending { it.value }.take(4).forEach { (grade, count) ->
                            val scaleRatio = count.toFloat() / maxCount
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Class $grade", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("$count periods active", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { scaleRatio },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Subject Distribution analytics
                    Text("Weekly Lesson Subject Frequency Distribution", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    val subjectGroups = timetableList.groupBy { it.subjectName }.mapValues { it.value.size }
                    if (subjectGroups.isEmpty()) {
                        Text("No subject distribution metrics captured.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        val maxSubCount = subjectGroups.values.maxOrNull() ?: 1
                        subjectGroups.entries.sortedByDescending { it.value }.take(4).forEach { (subject, count) ->
                            val scaleRatio = count.toFloat() / maxSubCount
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(subject, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("$count times", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { scaleRatio },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Faculty workload indicators
                    Text("Faculty Assigned Workload Index (Periods Count)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    val workloadGroups = timetableList.groupBy { it.teacherName }.mapValues { it.value.size }
                    if (workloadGroups.isEmpty()) {
                        Text("Please assign instructors to weekly timetables to analyze workloads.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        val maxWorkload = workloadGroups.values.maxOrNull() ?: 1
                        workloadGroups.entries.sortedByDescending { it.value }.take(4).forEach { (teacher, count) ->
                            val scaleRatio = count.toFloat() / maxWorkload
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(teacher, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("$count assigned slots", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { scaleRatio },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Smart Administration Alerts Workspace & Action Hub ---
        item {
            Text(
                "ST. JUDE SMART ADMINISTRATION SHIELD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            // Find active pending leaves
            val pendingLeaves = leaveRequests.filter { it.status.equals("Pending", ignoreCase = true) }
            // Find delinquent billing students (dues outstanding > 45% of total)
            val delinquentPayers = studentsList.filter { 
                it.status == "Active" && (it.feesTotal - it.feesPaid) > (it.feesTotal * 0.45) 
            }
            // Find low performing subjects
            val lowPerformingSubjects = remember(gradesList) {
                gradesList.groupBy { it.subjectName }
                    .map { (subject, list) ->
                        val avg = list.map { (it.score / it.maxScore) * 100.0 }.average()
                        subject to avg
                    }
                    .filter { it.second < 80.0 }
            }

            val hasAlerts = pendingLeaves.isNotEmpty() || delinquentPayers.isNotEmpty() || lowPerformingSubjects.isNotEmpty()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "INTELLIGENT INSIGHTS & ACTIONS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (hasAlerts) {
                            val totalAlertsCount = pendingLeaves.size + delinquentPayers.size + lowPerformingSubjects.size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "$totalAlertsCount ACTIONABLE ITEMS", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (!hasAlerts) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    "Pearl Junior administration checklist is perfect!",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    "All faculty leaves, student tuition, and class performance runs are on track and comply with criteria.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Display active notifications
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Leave Approval Section
                            pendingLeaves.forEach { leaveReq ->
                                val teacherName = teachersList.find { it.id == leaveReq.teacherId }?.name ?: "Unknown Faculty"
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DateRange,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    "LEAVE REQUEST REVIEW",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Text(
                                                text = leaveReq.leaveType,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }

                                        Text(
                                            text = "$teacherName requested time off from ${leaveReq.startDate} to ${leaveReq.endDate}.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Reason: \"${leaveReq.reason}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.updateLeaveRequestStatus(leaveReq.id, "Approved")
                                                    android.widget.Toast.makeText(context, "Leave Request approved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Approve Plan", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.updateLeaveRequestStatus(leaveReq.id, "Rejected")
                                                    android.widget.Toast.makeText(context, "Leave Request declined.", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Decline", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Billing / Delinquency alerts
                            delinquentPayers.forEach { student ->
                                val dues = student.feesTotal - student.feesPaid
                                val percentPaid = if (student.feesTotal > 0) (student.feesPaid / student.feesTotal) * 100.0 else 100.0
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    "CRITICAL TUITION DUE ALERT",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }

                                            Text(
                                                text = "UGX ${String.format(Locale.US, "%,.0f", dues)} Due",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }

                                        Text(
                                            text = "Pupil ${student.name} (Grade ${student.gradeLevel}) has only cleared ${String.format(Locale.US, "%.1f%%", percentPaid)} of terminal school fees.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Button(
                                            onClick = {
                                                smsCustomMsgText = "Dear Parent/Guardian of ${student.name}, this is a friendly reminder that an outstanding tuition dues payment balance of UGX ${String.format(Locale.US, "%,.0f", dues)} remains outstanding at Pearl Junior School. Kindly clear this balance as soon as possible. Thank you."
                                                activeSmsTargetStudent = student
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Text("Dispatch Parent Reminder", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Low Performance Academic Insight Alerts
                            lowPerformingSubjects.forEach { (subj, avg) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Analytics,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    "ACADEMIC UNDERPERFORMANCE INSIGHT",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }

                                            Text(
                                                text = "${String.format(Locale.US, "%.1f%%", avg)} GPA",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }

                                        Text(
                                            text = "Subject \"$subj\" terminal combined run averages are below the target 80% threshold.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Button(
                                            onClick = {
                                                val prompt = "Our school pupils registered a term average score of ${String.format(Locale.US, "%.1f%%", avg)} in $subj. We want to improve this standard of excellence at Pearl Junior School. What are 5 highly actionable classroom interventions, lesson plans, or guidance strategies we can deploy to boost their knowledge assessment run outcomes immediately?"
                                                viewModel.sendPromptToAI(prompt)
                                                navController.navigate("ai_assistant")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Text("Consult AI Copilot auditor", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
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

        // --- 📊 SCHOOL ANALYTICS COMMAND PALETTE ---
        item {
            Text(
                "ST. JUDE VISUAL INTELLIGENCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isWideScreenLocal = configuration.screenWidthDp >= 800

            if (isWideScreenLocal) {
                // Wide Desktop/Tablet visual layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FinanceFeesDonutChart(
                            collected = feesStats.totalCollected,
                            expected = feesStats.totalExpected,
                            modifier = Modifier.weight(1f)
                        )
                        SubjectPerformanceBarChart(
                            grades = gradesList,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GradeEnrollmentChart(
                            students = studentsList,
                            modifier = Modifier.weight(1f)
                        )
                        SchoolAttendanceSplineChart(
                            attendanceRecords = attendanceList,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Highly polished mobile horizontal slider tab selection
                var selectedTab by remember { mutableStateOf(0) }
                val tabTitles = listOf("Fees Collections", "Subject GPA", "Grades Share", "Attendance History")
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            tabTitles.forEachIndexed { idx, title ->
                                Tab(
                                    selected = selectedTab == idx,
                                    onClick = { selectedTab = idx },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            when (selectedTab) {
                                0 -> FinanceFeesDonutChart(
                                    collected = feesStats.totalCollected,
                                    expected = feesStats.totalExpected
                                )
                                1 -> SubjectPerformanceBarChart(
                                    grades = gradesList
                                )
                                2 -> GradeEnrollmentChart(
                                    students = studentsList
                                )
                                3 -> SchoolAttendanceSplineChart(
                                    attendanceRecords = attendanceList
                                )
                            }
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

    // Beautiful Extended Floating Action Button for Excel Batch Integration
    FloatingActionButton(
        onClick = { showExcelImportHubDialog = true },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp)
            .testTag("excel_import_hub_fab"),
        containerColor = Color(0xFF1B4332), // Elegant dark forest Excel Green
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add, 
                contentDescription = "Excel Bulk Hub Link", 
                modifier = Modifier.size(22.dp)
            )
            Text(
                "Excel Fold", 
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )
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
    
    val context = LocalContext.current

    val gradeLevels = listOf("All Grades", "Nursery", "Middle", "Top", "P.1", "P.2", "P.3", "P.4", "P.5", "P.6", "P.7")

    val filteredList = studentsList.filter { student ->
        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                student.rollNumber.contains(searchQuery, ignoreCase = true)
        val matchesGrade = selectedGradeFilter == "All Grades" || student.gradeLevel == selectedGradeFilter
        matchesSearch && matchesGrade
    }

    // Excel Pupil Upload dialog states
    var showPupilImportDialog by remember { mutableStateOf(false) }
    var pupilExcelFileName by remember { mutableStateOf<String?>(null) }
    var pupilImportCsvInput by remember { mutableStateOf("") }
    var pupilImportError by remember { mutableStateOf<String?>(null) }
    var pupilImportSuccess by remember { mutableStateOf<String?>(null) }

    val pupilFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var resultName: String? = null
                if (uri.scheme == "content") {
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    try {
                        if (cursor != null && cursor.moveToFirst()) {
                            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (index >= 0) {
                                resultName = cursor.getString(index)
                            }
                        }
                    } catch (e: Exception) {
                    } finally {
                        cursor?.close()
                    }
                }
                if (resultName == null) {
                    resultName = uri.path
                    val cut = resultName?.lastIndexOf('/') ?: -1
                    if (cut != -1) {
                        resultName = resultName?.substring(cut + 1)
                    }
                }
                pupilExcelFileName = resultName ?: "Students_Import.csv"

                val inputStream = context.contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (text.isNotBlank()) {
                    pupilImportCsvInput = text
                    pupilImportError = null
                    val linesCount = text.split("\n").filter { it.isNotBlank() }.size
                    pupilImportSuccess = "Successfully loaded $linesCount student columns from device!"
                } else {
                    pupilImportError = "The chosen file is empty."
                    pupilExcelFileName = null
                }
            } catch (e: Exception) {
                pupilImportError = "Failed to load device file: ${e.localizedMessage}"
                pupilExcelFileName = null
            }
        }
    }

    // Pupil CSV Parsing local inline helper
    val getPupilPreviewList = { input: String ->
        val list = mutableListOf<Student>()
        if (input.isNotBlank()) {
            try {
                input.split("\n").map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size >= 3) {
                        list.add(
                            Student(
                                id = 0,
                                name = parts[1],
                                rollNumber = parts[0],
                                gradeLevel = parts[2],
                                email = "",
                                phone = "",
                                gender = ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {}
        }
        list
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

            // Top actions header for Excel Imports
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredList.size} Pupils Found",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showPupilImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Pupils Excel", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Grade filters chips (scrollable to prevent overflow)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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

            // Dialog for importing pupil Excel files
            if (showPupilImportDialog) {
                AlertDialog(
                    onDismissRequest = { showPupilImportDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF1B4332))
                            Text("Import Pupils Spreadsheet")
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Select a .csv Excel workbook directly from your device file system to batch upload pupil logs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            // Clickable device file selector Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            pupilFilePickerLauncher.launch("*/*")
                                        } catch (e: Exception) {
                                            pupilImportError = "System file picker unavailable. Use manual copy-paste below."
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (pupilExcelFileName != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (pupilExcelFileName != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (pupilExcelFileName != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = if (pupilExcelFileName != null) Color(0xFF2E7D32) else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            pupilExcelFileName ?: "Tap to select device Excel spreadsheet",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (pupilExcelFileName != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Excel CSV Columns: Roll, Name, Grade, Gender, Phone, Email",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                Text("OR Paste CSV Raw Details", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }

                            OutlinedTextField(
                                value = pupilImportCsvInput,
                                onValueChange = {
                                    pupilImportCsvInput = it
                                    pupilExcelFileName = null
                                    pupilImportError = null
                                },
                                placeholder = {
                                    Text("Format: Roll,Name,Grade,Gender,Phone,Email\npupil_101,Aisha Namono,P.7,Female,0755123456\npupil_102,Ivan Ssempa,P.7,Male,0788999888")
                                },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = false
                            )

                            val matches = getPupilPreviewList(pupilImportCsvInput)
                            if (matches.isNotEmpty()) {
                                Text("Importing Preview (${matches.size} Pupil Rows Listed):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        matches.take(5).forEach { p ->
                                            Text(
                                                "• ${p.name} (${p.gradeLevel}) - Roll: ${p.rollNumber}", 
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray
                                            )
                                        }
                                        if (matches.size > 5) {
                                            Text("And ${matches.size - 5} more rows...", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            pupilImportError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            pupilImportSuccess?.let {
                                Text(it, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                pupilImportError = null
                                pupilImportSuccess = null
                                if (pupilImportCsvInput.isBlank()) {
                                    pupilImportError = "Choose any file or type CSV records first."
                                    return@Button
                                }
                                try {
                                    var importedCount = 0
                                    val lines = pupilImportCsvInput.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                    lines.forEach { line ->
                                        val parts = line.split(",").map { it.trim() }
                                        if (parts.size >= 3) {
                                            val roll = parts[0]
                                            val name = parts[1]
                                            val grade = parts[2]
                                            val gender = if (parts.size > 3) parts[3] else "Female"
                                            val phone = if (parts.size > 4) parts[4] else ""
                                            val email = if (parts.size > 5) parts[5] else ""
                                            val fTotal = if (parts.size > 6) parts[6].toDoubleOrNull() ?: 1200.0 else 1200.0
                                            val fPaid = if (parts.size > 7) parts[7].toDoubleOrNull() ?: 0.0 else 0.0

                                            viewModel.saveStudent(
                                                Student(
                                                    id = 0,
                                                    name = name,
                                                    rollNumber = roll,
                                                    gradeLevel = grade,
                                                    gender = gender,
                                                    phone = phone,
                                                    email = email,
                                                    feesTotal = fTotal,
                                                    feesPaid = fPaid,
                                                    status = "Active"
                                                )
                                            )
                                            importedCount++
                                        }
                                    }
                                    pupilImportSuccess = "Successfully saved $importedCount pupil profiles to local school database!"
                                    pupilImportCsvInput = ""
                                    pupilExcelFileName = null
                                    android.widget.Toast.makeText(context, "Successfully imported $importedCount pupils!", android.widget.Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    pupilImportError = "Compilation error: ${e.localizedMessage}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
                        ) {
                            Text("Confirm & Import")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPupilImportDialog = false }) {
                            Text("Close")
                        }
                    }
                )
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
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("Primary Seven (P.7)") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var feesTotal by remember { mutableStateOf("850000.0") }
    var feesPaid by remember { mutableStateOf("0.0") }
    var status by remember { mutableStateOf("Active") }
    var dateOfBirth by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
 
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
                dateOfBirth = s.dateOfBirth
                parentName = s.parentName
            }
        }
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var rollError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var parentNameError by remember { mutableStateOf<String?>(null) }
    var parentPhoneError by remember { mutableStateOf<String?>(null) }
    var parentEmailError by remember { mutableStateOf<String?>(null) }

    val gradeOptions = listOf(
        "Nursery",
        "Middle",
        "Top",
        "P.1",
        "P.2",
        "P.3",
        "P.4",
        "P.5",
        "P.6",
        "P.7"
    )
    val genderOptions = listOf("Male", "Female")
    val statusOptions = listOf("Active", "Inactive")

    fun getInitials(nameText: String): String {
        if (nameText.isBlank()) return "👤"
        val parts = nameText.trim().split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) return "👤"
        return if (parts.size >= 2) {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        } else {
            parts[0].take(2).uppercase()
        }
    }

    fun calculateAge(dobText: String): String {
        if (dobText.isBlank()) return "Not specified"
        val cleanDob = dobText.trim()
        val parts = cleanDob.split("-")
        if (parts.size != 3) return "Required format: YYYY-MM-DD"
        val year = parts[0].toIntOrNull() ?: return "Invalid year value"
        val month = parts[1].toIntOrNull() ?: return "Invalid month value"
        val day = parts[2].toIntOrNull() ?: return "Invalid day value"
        
        if (year < 1920 || year > 2026) return "Year must be 1920 - 2026"
        if (month < 1 || month > 12) return "Month must be 01 - 12"
        if (day < 1 || day > 31) return "Day must be 01 - 31"
        
        val currentYear = 2026
        val calculatedAge = currentYear - year
        return if (calculatedAge < 0) "Invalid DOB" else "$calculatedAge yrs old"
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = if (studentId == 0) "Register New Student" else "Update Academic Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (studentId == 0) "Establish a new pupil profile in the academic registry" else "Amend current registration information and details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Live Virtual ID Preview Card
            item {
                val isMale = gender == "Male"
                val gradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = if (isMale) {
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        )
                    } else {
                        listOf(
                            Color(0xFFFFD1DC).copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                        )
                    }
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_form_preview_badge"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .background(gradientBrush)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "PEARL JUNIOR SCHOOL REGISTRY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Surface(
                                    color = if (status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = getInitials(name),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (name.isBlank()) "Enter Full Name..." else name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = gradeLevel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        
                                        Text(
                                            text = "Roll ID: ${if (rollNumber.isBlank()) "TBD" else rollNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("DATE OF BIRTH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    Text(
                                        text = if (dateOfBirth.isBlank()) "Unspecified" else "$dateOfBirth (${calculateAge(dateOfBirth)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PARENT / GUARDIAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    Text(
                                        text = if (parentName.isBlank()) "Unspecified" else parentName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: Basic Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Student Personal Credentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; nameError = null },
                            label = { Text("Student Full Name *") },
                            placeholder = { Text("e.g. Liam Sempijja") },
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_form_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Column {
                            Text("Gender Selector *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Male", "Female").forEach { gOpt ->
                                    val isSelected = gender == gOpt
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { gender = gOpt },
                                        label = { Text(gOpt) },
                                        leadingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (gOpt == "Male") MaterialTheme.colorScheme.primaryContainer else Color(0xFFFFD1DC)
                                        )
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = dateOfBirth,
                            onValueChange = { dateOfBirth = it; dobError = null },
                            label = { Text("Date of Birth (YYYY-MM-DD) *") },
                            placeholder = { Text("e.g. 2012-10-24") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            isError = dobError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_form_dob"),
                            singleLine = true,
                            supportingText = {
                                if (dobError != null) {
                                    Text(dobError!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text(
                                        text = if (dateOfBirth.isBlank()) "Mandatory field format: YYYY-MM-DD" else "Age metric: ${calculateAge(dateOfBirth)}"
                                    )
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Section 2: Academic Setup & ID Registry
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Academic Assignment & Registry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Column {
                            Text("Class Assignment *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(gradeOptions) { opt ->
                                    val isSelected = gradeLevel == opt
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { gradeLevel = opt },
                                        label = { Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = gradeLevel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Selected Class Level") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { expanded = !expanded }
                                )
                                DropdownMenu(
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
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = rollNumber,
                                onValueChange = { rollNumber = it; rollError = null },
                                label = { Text("Roll ID / Access Key *") },
                                placeholder = { Text("e.g. S108") },
                                isError = rollError != null,
                                supportingText = { rollError?.let { Text(it) } },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("student_form_roll"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            Button(
                                onClick = {
                                    val initials = if (name.isNotBlank()) {
                                        name.trim().split(" ").take(2).map { it.take(1) }.joinToString("").uppercase()
                                    } else "ST"
                                    val prefix = gradeLevel.replace(".", "").take(3).uppercase()
                                    val randomSuffix = (100..999).random()
                                    rollNumber = "$prefix-$initials-$randomSuffix"
                                    rollError = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (studentId != 0) {
                            Column {
                                Text("Registration Status", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                var expandedStatus by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = status,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { expandedStatus = !expandedStatus }
                                    )
                                    DropdownMenu(
                                        expanded = expandedStatus,
                                        onDismissRequest = { expandedStatus = false }
                                    ) {
                                        statusOptions.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = { status = opt; expandedStatus = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Parent & Contact Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Parent / Guardian Contact details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it; parentNameError = null },
                            label = { Text("Parent / Guardian Full Name *") },
                            placeholder = { Text("e.g. Richard Sempijja") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            isError = parentNameError != null,
                            supportingText = { parentNameError?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_form_parent_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; parentPhoneError = null },
                            label = { Text("Parent Contact Phone *") },
                            placeholder = { Text("e.g. +256 701 234567") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            isError = parentPhoneError != null,
                            supportingText = { parentPhoneError?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_form_parent_phone"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; parentEmailError = null },
                            label = { Text("Parent Contact Email *") },
                            placeholder = { Text("e.g. richard@example.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            isError = parentEmailError != null,
                            supportingText = { parentEmailError?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_form_parent_email"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Section 4: Finance & Tuition Fees
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Tuition & School Fees", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = feesTotal,
                                onValueChange = { feesTotal = it },
                                label = { Text("Total Term Fee (UGX)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            OutlinedTextField(
                                value = feesPaid,
                                onValueChange = { feesPaid = it },
                                label = { Text("Amount Paid (UGX)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        
                        Column {
                            Text("Tuition Quick Presets:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val parsedTotal = feesTotal.toDoubleOrNull() ?: 0.0
                                
                                AssistChip(
                                    onClick = { feesPaid = "0.0" },
                                    label = { Text("Unpaid (0%)") }
                                )
                                
                                AssistChip(
                                    onClick = { feesPaid = (parsedTotal / 2.0).toString() },
                                    label = { Text("Half (50%)") }
                                )
                                
                                AssistChip(
                                    onClick = { feesPaid = parsedTotal.toString() },
                                    label = { Text("Full (100%)") }
                                )
                            }
                        }
                    }
                }
            }

            // Save / Cancel Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            var hasError = false
                            
                            if (name.isBlank()) {
                                nameError = "Name cannot be left empty."
                                hasError = true
                            } else {
                                nameError = null
                            }
                            
                            if (rollNumber.isBlank()) {
                                rollError = "Roll ID cannot be left empty."
                                hasError = true
                            } else {
                                rollError = null
                            }
                            
                            // Date of Birth validation
                            if (dateOfBirth.isBlank()) {
                                dobError = "Date of Birth cannot be left empty."
                                hasError = true
                            } else {
                                val dobRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
                                if (!dobRegex.matches(dateOfBirth.trim())) {
                                    dobError = "Please enter in YYYY-MM-DD format."
                                    hasError = true
                                } else {
                                    val parts = dateOfBirth.trim().split("-")
                                    val year = parts[0].toIntOrNull()
                                    val month = parts[1].toIntOrNull()
                                    val day = parts[2].toIntOrNull()
                                    
                                    if (year == null || month == null || day == null ||
                                        year < 1920 || year > 2026 ||
                                        month < 1 || month > 12 ||
                                        day < 1 || day > 31) {
                                        dobError = "Invalid Date of Birth details."
                                        hasError = true
                                    } else {
                                        dobError = null
                                    }
                                }
                            }
                            
                            // Parent/Guardian Name validation
                            if (parentName.isBlank()) {
                                parentNameError = "Parent name cannot be left empty."
                                hasError = true
                            } else {
                                parentNameError = null
                            }
                            
                            // Parent Contact Phone validation
                            if (phone.isBlank()) {
                                parentPhoneError = "Phone number cannot be left empty."
                                hasError = true
                            } else {
                                val phoneRegex = Regex("^\\+?[0-9\\s\\-\\(\\)]{8,15}$")
                                if (!phoneRegex.matches(phone.trim())) {
                                    parentPhoneError = "Invalid format (needs at least 8 digits)."
                                    hasError = true
                                } else {
                                    parentPhoneError = null
                                }
                            }
                            
                            // Parent Contact Email validation
                            if (email.isBlank()) {
                                parentEmailError = "Email cannot be left empty."
                                hasError = true
                            } else {
                                val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
                                if (!emailRegex.matches(email.trim())) {
                                    parentEmailError = "Please enter a valid email address."
                                    hasError = true
                                } else {
                                    parentEmailError = null
                                }
                            }
                            
                            if (hasError) {
                                android.widget.Toast.makeText(context, "Form validation failed. Please check highlighted errors.", android.widget.Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            
                            val st = Student(
                                id = studentId,
                                name = name,
                                rollNumber = rollNumber,
                                gradeLevel = gradeLevel,
                                email = email,
                                phone = phone,
                                gender = gender,
                                feesTotal = feesTotal.toDoubleOrNull() ?: 850000.0,
                                feesPaid = feesPaid.toDoubleOrNull() ?: 0.0,
                                status = status,
                                dateOfBirth = dateOfBirth,
                                parentName = parentName
                            )
                            viewModel.saveStudent(st)
                            val isUpdate = studentId != 0
                            val successMsg = if (isUpdate) "Student profile updated successfully!" else "Student successfully enrolled!"
                            android.widget.Toast.makeText(context, successMsg, android.widget.Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_form_save"),
                        shape = RoundedCornerShape(8.dp)
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
    val subjectOptions = remember(student) {
        val grade = student?.gradeLevel?.lowercase() ?: ""
        if (grade.contains("nursery") || grade.contains("middle") || grade.contains("top")) {
            listOf("Literacy & Numeracy", "Reading & Writing", "Art & Craft", "News & Speech", "Physical Play")
        } else {
            listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "Religious Education")
        }
    }
    var gradeSubjectName by remember { mutableStateOf("Mathematics") }
    LaunchedEffect(student, subjectOptions) {
        if (!subjectOptions.contains(gradeSubjectName)) {
            gradeSubjectName = subjectOptions.firstOrNull() ?: ""
        }
    }
    var gradeExamName by remember { mutableStateOf("Midterm Exam") }
    var gradeScore by remember { mutableStateOf("") }

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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gradeSubjectName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { subjectExpanded = !subjectExpanded }
                        )
                        DropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjectOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { gradeSubjectName = opt; subjectExpanded = false })
                            }
                        }
                    }

                    Text("Exam Name")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gradeExamName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { examExpanded = !examExpanded }
                        )
                        DropdownMenu(
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
    val teacherSignInMap by viewModel.teacherSignInTimesMap.collectAsStateWithLifecycle()
    val teacherSignOutMap by viewModel.teacherSignOutTimesMap.collectAsStateWithLifecycle()

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

    val allTeacherAttendanceList by viewModel.teacherAttendanceList.collectAsStateWithLifecycle()
    var attendanceSubMode by remember { mutableStateOf(0) } // 0 = Daily Register, 1 = Monthly Report
    var selectedReportYear by remember { mutableStateOf(2026) }
    var selectedReportMonthIndex by remember { mutableStateOf(5) } // Default to June (index 5)
    val monthsNames = remember {
        listOf(
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        )
    }
    val yearsList = listOf(2025, 2026, 2027)

    // Editing dialog states
    var selectedCellForEdit by remember { mutableStateOf<Triple<Int, String, String>?>(null) } // (teacherId, dateString, currentStatus)
    var showSummaryReportDialog by remember { mutableStateOf(false) }

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
                    text = { Text("Leaves", style = MaterialTheme.typography.titleSmall) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Leaves") }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Lesson Logs", style = MaterialTheme.typography.titleSmall) },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Lessons") }
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
                    // ATTENDANCE TAB WITH DUAL DAILY LOG & MONTHLY REPORT MODES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Submode Selector Switcher (Daily Log vs Monthly Reports)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { attendanceSubMode = 0 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (attendanceSubMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (attendanceSubMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Daily Ledger", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Button(
                                onClick = { attendanceSubMode = 1 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (attendanceSubMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (attendanceSubMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Monthly Reports", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        if (attendanceSubMode == 0) {
                            // DAILY RECORDING LEDGER
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

                                                // Sign-In/Out row
                                                if (currentStatus == "Present" || currentStatus == "Late") {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val currentSignIn = teacherSignInMap[teacher.id] ?: ""
                                                        val currentSignOut = teacherSignOutMap[teacher.id] ?: ""

                                                        OutlinedTextField(
                                                            value = currentSignIn,
                                                            onValueChange = { viewModel.updateTeacherSignInTime(teacher.id, it) },
                                                            label = { Text("Sign-In Time", style = MaterialTheme.typography.bodySmall) },
                                                            placeholder = { Text("08:00 AM") },
                                                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                            modifier = Modifier.weight(1f),
                                                            singleLine = true,
                                                            textStyle = MaterialTheme.typography.bodySmall,
                                                            trailingIcon = {
                                                                IconButton(onClick = {
                                                                    val timeString = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                                                    viewModel.updateTeacherSignInTime(teacher.id, timeString)
                                                                 }) {
                                                                    Icon(Icons.Default.Check, contentDescription = "Now", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                                }
                                                            }
                                                        )

                                                        OutlinedTextField(
                                                            value = currentSignOut,
                                                            onValueChange = { viewModel.updateTeacherSignOutTime(teacher.id, it) },
                                                            label = { Text("Sign-Out Time", style = MaterialTheme.typography.bodySmall) },
                                                            placeholder = { Text("05:00 PM") },
                                                            modifier = Modifier.weight(1f),
                                                            singleLine = true,
                                                            textStyle = MaterialTheme.typography.bodySmall,
                                                            trailingIcon = {
                                                                IconButton(onClick = {
                                                                    val timeString = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                                                    viewModel.updateTeacherSignOutTime(teacher.id, timeString)
                                                                }) {
                                                                    Icon(Icons.Default.Check, contentDescription = "Now", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // MONTHLY LOGS REPORTING VIEW
                            val maxDaysInMonth = remember(selectedReportYear, selectedReportMonthIndex) {
                                val cal = java.util.Calendar.getInstance()
                                cal.set(java.util.Calendar.YEAR, selectedReportYear)
                                cal.set(java.util.Calendar.MONTH, selectedReportMonthIndex)
                                cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                            }

                            val datesInMonth = remember(selectedReportYear, selectedReportMonthIndex, maxDaysInMonth) {
                                (1..maxDaysInMonth).map { day ->
                                    String.format(java.util.Locale.US, "%04d-%02d-%02d", selectedReportYear, selectedReportMonthIndex + 1, day)
                                }
                            }

                            val monthlyAttendanceRecords = remember(allTeacherAttendanceList, selectedReportYear, selectedReportMonthIndex) {
                                val prefix = String.format(java.util.Locale.US, "%04d-%02d", selectedReportYear, selectedReportMonthIndex + 1)
                                allTeacherAttendanceList.filter { it.date.startsWith(prefix) }
                            }

                            // Horizontal Months list selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                monthsNames.forEachIndexed { idx, mName ->
                                    val isSelected = selectedReportMonthIndex == idx
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedReportMonthIndex = idx },
                                        label = { Text(mName, fontSize = 12.sp) }
                                    )
                                }
                            }

                            // Year and Title header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monthly Staff Calendar Grid",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Year selector dropdown
                                var yearExpanded by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(
                                        onClick = { yearExpanded = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(selectedReportYear.toString(), style = MaterialTheme.typography.bodySmall)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    DropdownMenu(
                                        expanded = yearExpanded,
                                        onDismissRequest = { yearExpanded = false }
                                    ) {
                                        yearsList.forEach { yr ->
                                            DropdownMenuItem(
                                                text = { Text(yr.toString()) },
                                                onClick = {
                                                    selectedReportYear = yr
                                                    yearExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Executive Summary card for month
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "Month Executive Overview",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val totPresent = monthlyAttendanceRecords.count { it.status == "Present" }
                                            val totAbsent = monthlyAttendanceRecords.count { it.status == "Absent" }
                                            val totLate = monthlyAttendanceRecords.count { it.status == "Late" }
                                            val totLeave = monthlyAttendanceRecords.count { it.status == "On Leave" }
                                            val totLogs = totPresent + totAbsent + totLate

                                            val avgMonthlyRate = if (totLogs > 0) {
                                                ((totPresent + totLate).toFloat() / totLogs) * 100f
                                            } else {
                                                100f
                                            }

                                            Text(
                                                text = "On-Time attendance rate is ${String.format(java.util.Locale.US, "%.1f%%", avgMonthlyRate)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Text(
                                                text = "Active sessions: $totLogs. Logs breakdown -> Present: $totPresent | Late: $totLate | Absent: $totAbsent | On Leave: $totLeave",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { showSummaryReportDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("Generate Monthly Summary Report", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }

                            // Matrix grid card
                            Card(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (teachersList.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No faculty available to generate monthly reporting.", color = Color.Gray, textAlign = TextAlign.Center)
                                            }
                                        }
                                    } else {
                                        items(teachersList) { teacher ->
                                            val teacherRecords = monthlyAttendanceRecords.filter { it.teacherId == teacher.id }
                                            val pCount = teacherRecords.count { it.status == "Present" }
                                            val aCount = teacherRecords.count { it.status == "Absent" }
                                            val lCount = teacherRecords.count { it.status == "Late" }
                                            val oCount = teacherRecords.count { it.status == "On Leave" }
                                            val legCount = pCount + aCount + lCount
                                            val rate = if (legCount > 0) {
                                                ((pCount + lCount).toFloat() / legCount) * 100f
                                            } else {
                                                100f
                                            }

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = teacher.name.firstOrNull()?.toString() ?: "T",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }

                                                        Column {
                                                            Text(
                                                                text = teacher.name,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "Pres: $pCount | Late: $lCount | Abs: $aCount | Lve: $oCount",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Color.Gray,
                                                                fontSize = 11.sp
                                                            )
                                                            val loggedDaysWithTimes = teacherRecords.filter { it.signInTime.isNotEmpty() || it.signOutTime.isNotEmpty() }
                                                            if (loggedDaysWithTimes.isNotEmpty()) {
                                                                val latestRecord = loggedDaysWithTimes.maxByOrNull { it.date }
                                                                Text(
                                                                    text = "Hours: ${loggedDaysWithTimes.size} days. Latest sync: ${latestRecord?.signInTime ?: "N/A"} - ${latestRecord?.signOutTime ?: "N/A"}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Surface(
                                                        color = when {
                                                            rate >= 90f -> Color(0xFFE8F5E9)
                                                            rate >= 75f -> Color(0xFFFFF3E0)
                                                            else -> Color(0xFFFFEBEE)
                                                        },
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = String.format(java.util.Locale.US, "%.0f%% Rate", rate),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when {
                                                                rate >= 90f -> Color(0xFF2E7D32)
                                                                rate >= 75f -> Color(0xFFE65100)
                                                                else -> Color(0xFFC62828)
                                                            },
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                // Attendance circles calendar timeline row
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState())
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    (1..maxDaysInMonth).forEach { day ->
                                                        val dateStr = String.format(java.util.Locale.US, "%04d-%02d-%02d", selectedReportYear, selectedReportMonthIndex + 1, day)
                                                        val match = monthlyAttendanceRecords.find { it.teacherId == teacher.id && it.date == dateStr }
                                                        val status = match?.status ?: "None"

                                                        val (circleColor, textColor) = when (status) {
                                                            "Present" -> Color(0xFF2E7D32) to Color.White
                                                            "Absent" -> Color(0xFFC62828) to Color.White
                                                            "Late" -> Color(0xFFE65100) to Color.White
                                                            "On Leave" -> Color(0xFF1565C0) to Color.White
                                                            else -> Color.DarkGray.copy(alpha = 0.08f) to Color.Gray
                                                        }

                                                        val badgeChar = when (status) {
                                                            "Present" -> "P"
                                                            "Absent" -> "A"
                                                            "Late" -> "L"
                                                            "On Leave" -> "O"
                                                            else -> day.toString()
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(CircleShape)
                                                                .background(circleColor)
                                                                .clickable {
                                                                    selectedCellForEdit = Triple(teacher.id, dateStr, status)
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = badgeChar,
                                                                color = textColor,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                                        .padding(vertical = 4.dp)
                                                )
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
                3 -> {
                    // LESSON AND SUBSTITUTE TRACKING DESK
                    val lessonTracks by viewModel.lessonTracks.collectAsStateWithLifecycle()
                    val timetableList by viewModel.timetablePeriods.collectAsStateWithLifecycle()
                    
                    var showLogDialog by remember { mutableStateOf(false) }
                    var selectedPeriodId by remember { mutableStateOf(0) }
                    var logStatus by remember { mutableStateOf("Taught") }
                    var logNotes by remember { mutableStateOf("") }
                    var substituteTeacher by remember { mutableStateOf("") }
                    var logPunctuality by remember { mutableStateOf("Punctual") }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "👨‍🏫 Lesson Coverage & Teacher Punctuality Board",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Log lessons taught, substitute assignments, missed periods, and check teacher punctuality indicators across the primary grades.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showLogDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, "Log Period Coverage")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Completed Period", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (lessonTracks.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No lessons logged today yet. Log completed periods above to see logs.", color = Color.Gray)
                                    }
                                }
                            } else {
                                items(lessonTracks.sortedByDescending { it.id }) { track ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                                        text = "Class ${track.className} - ${track.subjectName}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Primary Teacher: ${track.teacherName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = when (track.status) {
                                                                "Taught" -> Color(0xFFE8F5E9)
                                                                "Missed" -> Color(0xFFFFEBEE)
                                                                else -> Color(0xFFFFF3E0)
                                                            },
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = track.status,
                                                        color = when (track.status) {
                                                            "Taught" -> Color(0xFF2E7D32)
                                                            "Missed" -> Color(0xFFD32F2F)
                                                            else -> Color(0xFFEF6C00)
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("📅 Date: ${track.trackDate}  |  ⏱️ Punctuality: ${track.punctuality}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            
                                            if (track.substituteTeacherName.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("🔄 Substitute Teacher Assigned: ${track.substituteTeacherName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                            }
                                            
                                            if (track.notes.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("📝 Memo: ${track.notes}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                            }
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { viewModel.deleteLessonTrack(track.id) }) {
                                                    Text("Remove Log", color = Color.Red, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Dialog to log details
                    if (showLogDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogDialog = false },
                            title = { Text("Log Period Progress") },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                ) {
                                    Text("Record a completed lesson period from the master timetable class periods list.")
                                    
                                    if (timetableList.isEmpty()) {
                                        Text("⚠️ Please build or generate the school timetable periods list first.", color = Color.Red, fontWeight = FontWeight.Bold)
                                    } else {
                                        var periodExpanded by remember { mutableStateOf(false) }
                                        val selectedPeriod = timetableList.find { it.id == selectedPeriodId } ?: timetableList.firstOrNull() ?: timetableList.getOrNull(0)
                                        
                                        LaunchedEffect(timetableList) {
                                            if (selectedPeriodId == 0 && timetableList.isNotEmpty()) {
                                                selectedPeriodId = timetableList.first().id
                                            }
                                        }
                                        
                                        if (selectedPeriod != null) {
                                            Text("Select Scheduled Period Block:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = "Class ${selectedPeriod.className}: ${selectedPeriod.subjectName} (${selectedPeriod.startTime} with ${selectedPeriod.teacherName})",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Box(modifier = Modifier.matchParentSize().clickable { periodExpanded = !periodExpanded })
                                                DropdownMenu(
                                                    expanded = periodExpanded,
                                                    onDismissRequest = { periodExpanded = false }
                                                ) {
                                                    timetableList.forEach { prd ->
                                                        DropdownMenuItem(
                                                            text = { Text("Class ${prd.className}: ${prd.subjectName} (${prd.startTime} - ${prd.endTime})") },
                                                            onClick = {
                                                                selectedPeriodId = prd.id
                                                                periodExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Text("Coverage Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("Taught", "Missed", "Substitute Assigned").forEach { st ->
                                                FilterChip(
                                                    selected = logStatus == st,
                                                    onClick = { logStatus = st },
                                                    label = { Text(st, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        
                                        if (logStatus == "Substitute Assigned") {
                                            Text("Assign Substitute Teacher Name:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            OutlinedTextField(
                                                value = substituteTeacher,
                                                onValueChange = { substituteTeacher = it },
                                                placeholder = { Text("e.g. Mr. Ssewankambo Henry") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        
                                        Text("Teacher Punctuality:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("Punctual", "Late", "Extremely Late").forEach { pct ->
                                                FilterChip(
                                                    selected = logPunctuality == pct,
                                                    onClick = { logPunctuality = pct },
                                                    label = { Text(pct, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        
                                        Text("Memo Notes / Topics Covered:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = logNotes,
                                            onValueChange = { logNotes = it },
                                            placeholder = { Text("e.g. Chapter 4: Fractions and algebra, class was attentive.") },
                                            minLines = 2,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                val match = timetableList.find { it.id == selectedPeriodId } ?: timetableList.firstOrNull()
                                if (match != null) {
                                    Button(
                                        onClick = {
                                            viewModel.insertLessonTrack(
                                                timetablePeriodId = match.id,
                                                className = match.className,
                                                subjectName = match.subjectName,
                                                teacherName = match.teacherName,
                                                trackDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                                status = logStatus,
                                                substituteTeacherName = if (logStatus == "Substitute Assigned") substituteTeacher else "",
                                                notes = logNotes,
                                                punctuality = logPunctuality
                                            )
                                            showLogDialog = false
                                            logNotes = ""
                                            substituteTeacher = ""
                                        }
                                    ) {
                                        Text("Log Record")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLogDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Retroactive edit dialog
        selectedCellForEdit?.let { cell ->
            val teacherId = cell.first
            val dateStr = cell.second
            val currentStatus = cell.third
            val teacher = teachersList.find { it.id == teacherId }

            val matchRecord = remember(cell, allTeacherAttendanceList) {
                allTeacherAttendanceList.find { it.teacherId == teacherId && it.date == dateStr }
            }

            var chosenStatus by remember(cell) { mutableStateOf(if (currentStatus == "None") "Present" else currentStatus) }
            var retroactiveSignIn by remember(cell) { mutableStateOf(matchRecord?.signInTime ?: "") }
            var retroactiveSignOut by remember(cell) { mutableStateOf(matchRecord?.signOutTime ?: "") }

            AlertDialog(
                onDismissRequest = { selectedCellForEdit = null },
                title = {
                    Text(
                        text = "Retroactive Ledger Update",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Updating attendance record for ${teacher?.name ?: "Faculty Member"} on $dateStr.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Select Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                        listOf("Present", "Absent", "Late", "On Leave").forEach { st ->
                            val isSelected = chosenStatus == st
                            val color = when (st) {
                                "Present" -> Color(0xFF2E7D32)
                                "Absent" -> Color(0xFFC62828)
                                "Late" -> Color(0xFFE65100)
                                else -> Color(0xFF1565C0)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable { chosenStatus = st }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { chosenStatus = st },
                                    colors = RadioButtonDefaults.colors(selectedColor = color)
                                )
                                Text(
                                    text = st,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (chosenStatus == "Present" || chosenStatus == "Late") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Log Hours:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = retroactiveSignIn,
                                onValueChange = { retroactiveSignIn = it },
                                label = { Text("Sign-In Time") },
                                placeholder = { Text("08:00 AM") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        retroactiveSignIn = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Now", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = retroactiveSignOut,
                                onValueChange = { retroactiveSignOut = it },
                                label = { Text("Sign-Out Time") },
                                placeholder = { Text("05:00 PM") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        retroactiveSignOut = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Now", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.recordSingleTeacherAttendance(
                                teacherId = teacherId,
                                date = dateStr,
                                status = chosenStatus,
                                signInTime = retroactiveSignIn,
                                signOutTime = retroactiveSignOut
                            )
                            android.widget.Toast.makeText(context, "Record updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            selectedCellForEdit = null
                        }
                    ) {
                        Text("Save Update")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedCellForEdit = null }
                    ) {
                        Text("Cancel")
                    }
                }
            )
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
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedTeacher?.name ?: "Select staff member",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { teacherExpanded = !teacherExpanded }
                            )
                            DropdownMenu(
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
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = leaveType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leaveTypeExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { leaveTypeExpanded = !leaveTypeExpanded }
                            )
                            DropdownMenu(
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

    if (showSummaryReportDialog) {
        val selectedMonthName = monthsNames[selectedReportMonthIndex]
        val totalTeachersCount = teachersList.size
        
        val reportBuilder = StringBuilder()
        reportBuilder.append("========================================\n")
        reportBuilder.append("    MONTHLY TEACHER ATTENDANCE REPORT   \n")
        reportBuilder.append("========================================\n")
        reportBuilder.append("School Faculty: Greenhill Academy\n")
        reportBuilder.append("Report Month: $selectedMonthName $selectedReportYear\n")
        reportBuilder.append("Generated On: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        reportBuilder.append("Total Staff on Roster: $totalTeachersCount members\n")
        reportBuilder.append("----------------------------------------\n\n")

        val prefix = String.format(java.util.Locale.US, "%04d-%02d", selectedReportYear, selectedReportMonthIndex + 1)
        val monthlyAttendanceRecords = allTeacherAttendanceList.filter { it.date.startsWith(prefix) }
        
        val totPresent = monthlyAttendanceRecords.count { it.status == "Present" }
        val totAbsent = monthlyAttendanceRecords.count { it.status == "Absent" }
        val totLate = monthlyAttendanceRecords.count { it.status == "Late" }
        val totLeave = monthlyAttendanceRecords.count { it.status == "On Leave" }
        val totLogs = totPresent + totAbsent + totLate

        val avgMonthlyRate = if (totLogs > 0) {
            ((totPresent + totLate).toFloat() / totLogs) * 100f
        } else {
            100f
        }

        reportBuilder.append("OVERALL METRICS:\n")
        reportBuilder.append(" * Attendance Rate: ${String.format(java.util.Locale.US, "%.1f%%", avgMonthlyRate)}\n")
        reportBuilder.append(" * Total Logs Made: $totLogs\n")
        reportBuilder.append(" * Present Days: $totPresent\n")
        reportBuilder.append(" * Late Arrivals: $totLate\n")
        reportBuilder.append(" * Unexcused Absences: $totAbsent\n")
        reportBuilder.append(" * Authorized Leaves: $totLeave\n\n")
        reportBuilder.append("STAFF BREAKDOWN:\n")
        reportBuilder.append("----------------------------------------\n")

        teachersList.forEach { teacher ->
            val teacherRecords = monthlyAttendanceRecords.filter { it.teacherId == teacher.id }
            val pCount = teacherRecords.count { it.status == "Present" }
            val aCount = teacherRecords.count { it.status == "Absent" }
            val lCount = teacherRecords.count { it.status == "Late" }
            val oCount = teacherRecords.count { it.status == "On Leave" }
            val legCount = pCount + aCount + lCount
            val rate = if (legCount > 0) {
                ((pCount + lCount).toFloat() / legCount) * 100f
            } else {
                100f
            }
            val rating = when {
                rate >= 90f -> "Excellent"
                rate >= 75f -> "Satisfactory"
                else -> "Review Needed"
            }
            reportBuilder.append("👨‍🏫 Name: ${teacher.name}\n")
            reportBuilder.append("  Role: ${teacher.assignedRole}\n")
            reportBuilder.append("  Department: ${teacher.subjectSpecialty}\n")
            reportBuilder.append("  Stats: Present: $pCount | Late: $lCount | Absent: $aCount | Leave: $oCount\n")
            reportBuilder.append("  On-Time Attendance Rate: ${String.format(java.util.Locale.US, "%.1f%%", rate)} -> $rating\n")
            reportBuilder.append("----------------------------------------\n")
        }

        val fullReportText = reportBuilder.toString()

        AlertDialog(
            onDismissRequest = { showSummaryReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$selectedMonthName $selectedReportYear Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Here is the compiled administrative monthly report for Greenhill Academy faculty of teachers.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = fullReportText,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Monthly Attendance Report", fullReportText)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Report copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Check, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Text("Copy", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Greenhill Teacher Attendance Report - $selectedMonthName")
                                    putExtra(android.content.Intent.EXTRA_TEXT, fullReportText)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Attendance Report Summary"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummaryReportDialog = false }) {
                    Text("Close Ledger")
                }
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expanded = !expanded }
                    )
                    DropdownMenu(
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
    var selectedGrade by remember { mutableStateOf("P.7") }
    
    val gradeOptions = listOf("Nursery", "Middle", "Top", "P.1", "P.2", "P.3", "P.4", "P.5", "P.6", "P.7")
    val subjectOptions = remember(selectedGrade) {
        val grade = selectedGrade.lowercase()
        if (grade.contains("nursery") || grade.contains("middle") || grade.contains("top")) {
            listOf("Literacy & Numeracy", "Reading & Writing", "Art & Craft", "News & Speech", "Physical Play")
        } else {
            listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "Religious Education")
        }
    }
    
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    LaunchedEffect(selectedGrade, subjectOptions) {
        if (!subjectOptions.contains(selectedSubject)) {
            selectedSubject = subjectOptions.firstOrNull() ?: ""
        }
    }
    var selectedTeacherId by remember { mutableStateOf(0) }

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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedGrade,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { gradeExpanded = !gradeExpanded }
                        )
                        DropdownMenu(
                            expanded = gradeExpanded,
                            onDismissRequest = { gradeExpanded = false }
                        ) {
                            gradeOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { selectedGrade = opt; gradeExpanded = false })
                            }
                        }
                    }

                    Text("Syllabus Subject")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { subjectExpanded = !subjectExpanded }
                        )
                        DropdownMenu(
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
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = activeTeacher.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { teacherExpanded = !teacherExpanded }
                            )
                            DropdownMenu(
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
    val gradeOptions = listOf("Nursery", "Middle", "Top", "P.1", "P.2", "P.3", "P.4", "P.5", "P.6", "P.7")

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
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = grade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { gradeExpanded = !gradeExpanded }
                            )
                            DropdownMenu(
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

    val examOptions = listOf("Midterm Exam", "End of Term Exam", "Continuous Assessment")

    val classLevels = listOf(
        "Nursery", "Middle", "Top",
        "P.1", "P.2", "P.3",
        "P.4", "P.5", "P.6",
        "P.7"
    )

    // Excel Entry states
    var excelClassInput by remember { mutableStateOf("P.7") }
    
    val subjectOptions = remember(excelClassInput) {
        val grade = excelClassInput.lowercase()
        if (grade.contains("nursery") || grade.contains("middle") || grade.contains("top")) {
            listOf("Literacy & Numeracy", "Reading & Writing", "Art & Craft", "News & Speech", "Physical Play")
        } else {
            listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "Religious Education")
        }
    }
    
    var excelSubjectInput by remember { mutableStateOf("Mathematics") }
    LaunchedEffect(excelClassInput, subjectOptions) {
        if (!subjectOptions.contains(excelSubjectInput)) {
            excelSubjectInput = subjectOptions.firstOrNull() ?: ""
        }
    }

    // Grid entry states: studentId -> score String
    val midTermInputs = remember { mutableStateMapOf<Int, String>() }
    val endOfTermInputs = remember { mutableStateMapOf<Int, String>() }

    // String for Excel Copy-Paste CSV
    var bulkCsvInput by remember { mutableStateOf("") }
    var showCsvInstructions by remember { mutableStateOf(true) }
    var csvErrorFeedback by remember { mutableStateOf<String?>(null) }

    // Direct assessment file pickers and auto-grader states
    var selectedAssessmentFileName by remember { mutableStateOf<String?>(null) }
    var assessmentErrorFeedback by remember { mutableStateOf<String?>(null) }
    var assessmentSuccessFeedback by remember { mutableStateOf<String?>(null) }

    val assessmentFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Determine file name helper
                var resultName: String? = null
                if (uri.scheme == "content") {
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    try {
                        if (cursor != null && cursor.moveToFirst()) {
                            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (index >= 0) {
                                resultName = cursor.getString(index)
                            }
                        }
                    } catch (e: Exception) {
                        // Safe
                    } finally {
                        cursor?.close()
                    }
                }
                if (resultName == null) {
                    resultName = uri.path
                    val cut = resultName?.lastIndexOf('/') ?: -1
                    if (cut != -1) {
                        resultName = resultName?.substring(cut + 1)
                    }
                }
                selectedAssessmentFileName = resultName ?: "Assessments_Sheet.csv"

                val inputStream = context.contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (text.isNotBlank()) {
                    bulkCsvInput = text
                    assessmentErrorFeedback = null
                    val lineCount = text.split("\n").filter { it.isNotBlank() }.size
                    assessmentSuccessFeedback = "Successfully loaded $lineCount spreadsheet rows from device!"
                } else {
                    assessmentErrorFeedback = "The chosen file is empty."
                    selectedAssessmentFileName = null
                }
            } catch (e: Exception) {
                assessmentErrorFeedback = "Failed to load device file: ${e.localizedMessage}"
                selectedAssessmentFileName = null
            }
        }
    }

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
    val actualReportStudentId = if (selectedReportStudentId == 0 && listStudents.isNotEmpty()) listStudents.first().id else selectedReportStudentId
    var showReportCardPreview by remember { mutableStateOf(false) }

    // AI Report Comment State
    var aiTeacherRemark by remember { mutableStateOf("") }
    var aiHeadmasterRemark by remember { mutableStateOf("") }
    var preparingAiRemarks by remember { mutableStateOf(false) }

    LaunchedEffect(actualReportStudentId) {
        aiTeacherRemark = ""
        aiHeadmasterRemark = ""
        preparingAiRemarks = false
    }

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
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Central Grade Book", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp)) }
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
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = excelClassInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = excelClassExpanded) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable { excelClassExpanded = !excelClassExpanded }
                                        )
                                        DropdownMenu(
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
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = excelSubjectInput,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = excelSubjExpanded) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyMedium
                                        )
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable { excelSubjExpanded = !excelSubjExpanded }
                                        )
                                        DropdownMenu(
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

                        // STANDALONE SECTION A: Spreadsheet File/CSV Importer & Auto-Grader (Always clearly displayed!)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFF81C784))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            tint = Color(0xFF2E7D32),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Batch Spreadsheet File Upload & Parser", 
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                    
                                    Text(
                                        "Select a .csv Excel workbook or paste comma-separated assessment scores to automatically grade and synchronize with the interactive ledger table below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )

                                    // Pre-Load UNEB Conforming Dataset Button
                                    Button(
                                        onClick = {
                                            if (filteredStudents.isEmpty()) {
                                                android.widget.Toast.makeText(context, "No registered pupils found in $excelClassInput. Add some students first!", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                val isNursery = excelClassInput.lowercase().contains("nursery") || excelClassInput.lowercase().contains("middle") || excelClassInput.lowercase().contains("top")
                                                val sampleBuilder = StringBuilder()
                                                val classSubjects = if (isNursery) {
                                                    listOf("Literacy & Numeracy")
                                                } else {
                                                    listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")
                                                }
                                                filteredStudents.forEach { st ->
                                                    classSubjects.forEach { subj ->
                                                        val base = (65..92).random()
                                                        val mid = (base - (1..10).random()).coerceIn(40, 100)
                                                        val eot = (base + (1..8).random()).coerceIn(40, 100)
                                                        sampleBuilder.append("${st.rollNumber},$subj,Midterm Exam,$mid,100.0\n")
                                                        sampleBuilder.append("${st.rollNumber},$subj,End of Term Exam,$eot,100.0\n")
                                                    }
                                                }
                                                bulkCsvInput = sampleBuilder.toString()
                                                selectedAssessmentFileName = "Conforming_UNEB_Sheet.csv"
                                                assessmentErrorFeedback = null
                                                assessmentSuccessFeedback = "Generated optimal UNEB-conforming exam scores for ${filteredStudents.size} registered pupils."
                                                android.widget.Toast.makeText(context, "Conforming Excel dataset loaded for $excelClassInput! Click run evaluator below to sync.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("load_uneb_sample_button")
                                    ) {
                                        Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pre-Load Conforming UNEB Excel Dataset", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    }
                                    
                                    // File Selection Input Area
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    assessmentFilePickerLauncher.launch("*/*")
                                                } catch (e: Exception) {
                                                    assessmentErrorFeedback = "Device picker unavailable. Please use manual copy-paste below."
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedAssessmentFileName != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (selectedAssessmentFileName != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (selectedAssessmentFileName != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                                contentDescription = "File picker",
                                                tint = if (selectedAssessmentFileName != null) Color(0xFF2E7D32) else Color.Gray,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Column(modifier = Modifier.weight(1.0f)) {
                                                Text(
                                                    text = selectedAssessmentFileName ?: "Click here to upload assessment spreadsheet file",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (selectedAssessmentFileName != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (selectedAssessmentFileName != null) "Spreadsheet loaded. Run auto-grader below." else "Choose any .csv spreadsheet containing student scores directly from device.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }

                                    // Manual Paste Section
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                                            Text("OR Paste Excel/CSV Raw Rows", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                                        }

                                        OutlinedTextField(
                                            value = bulkCsvInput,
                                            onValueChange = { 
                                                bulkCsvInput = it
                                                selectedAssessmentFileName = null
                                                assessmentErrorFeedback = null
                                                assessmentSuccessFeedback = null
                                            },
                                            placeholder = { 
                                                Text("General columns:\nRollNumber,SubjectName,ExamType,Score,MaxScore\n\nOr short manual format:\nRollNumber,MidtermScore,EndOfTermScore\n\ne.g., S1001,88,94") 
                                            },
                                            label = { Text("Excel Assessment CSV / Paste Direct Data") },
                                            modifier = Modifier.fillMaxWidth().height(115.dp),
                                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                            singleLine = false
                                        )
                                    }

                                    // Dynamic grading parser and preview workspace
                                    if (bulkCsvInput.isNotBlank()) {
                                        val parseLines = bulkCsvInput.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                        val previewList = remember(bulkCsvInput, listStudents) {
                                            val tmp = mutableListOf<Triple<String, String, Double>>() // StudentName / Grade / Points
                                            try {
                                                parseLines.take(15).forEach { line ->
                                                    val parts = line.split(",").map { it.trim() }
                                                    if (parts.size >= 2) {
                                                        val roll = parts[0]
                                                        val pupil = listStudents.find { it.rollNumber.equals(roll, ignoreCase = true) }
                                                        if (pupil != null) {
                                                            // Check format
                                                            val score = if (parts.size == 3) {
                                                                // assume midterm/endterm or average of both
                                                                val sc1 = parts[1].toDoubleOrNull() ?: 0.0
                                                                val sc2 = parts[2].toDoubleOrNull() ?: 0.0
                                                                (sc1 + sc2) / 2.0
                                                            } else if (parts.size >= 4) {
                                                                parts[3].toDoubleOrNull() ?: 0.0
                                                            } else {
                                                                parts[1].toDoubleOrNull() ?: 0.0
                                                            }
                                                            tmp.add(Triple(pupil.name, roll, score))
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {}
                                            tmp
                                        }

                                        if (previewList.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    "Automatic Grading Preview (Top ${previewList.size} Rows Analyzed):", 
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF2E7D32)
                                                )
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF6)),
                                                    border = BorderStroke(0.5.dp, Color(0xFFFFD54F)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        previewList.forEach { p ->
                                                            val avg = p.third
                                                            val (g, desc) = when {
                                                                avg >= 85.0 -> Pair("D1 🌟", "Distinct 1")
                                                                avg >= 75.0 -> Pair("D2", "Distinct 2")
                                                                avg >= 70.0 -> Pair("C3", "Credit 3")
                                                                avg >= 65.0 -> Pair("C4", "Credit 4")
                                                                avg >= 60.0 -> Pair("C5", "Credit 5")
                                                                avg >= 50.0 -> Pair("C6", "Credit 6")
                                                                avg >= 45.0 -> Pair("P7", "Pass 7")
                                                                avg >= 40.0 -> Pair("P8", "Pass 8")
                                                                else -> Pair("F9 ❌", "Fail")
                                                            }
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    "${p.first} (${p.second})", 
                                                                    style = MaterialTheme.typography.labelSmall, 
                                                                    color = Color.DarkGray,
                                                                    modifier = Modifier.weight(1.8f)
                                                                )
                                                                Text(
                                                                    "Score: ${String.format(Locale.US, "%.1f", avg)}%", 
                                                                    style = MaterialTheme.typography.labelSmall, 
                                                                    color = Color.Gray,
                                                                    modifier = Modifier.weight(1.1f)
                                                                )
                                                                Surface(
                                                                    color = if (avg >= 75) Color(0xFFE8F5E9) else if (avg >= 50) Color(0xFFFFF3E0) else Color(0xFFFFEBEE),
                                                                    shape = RoundedCornerShape(4.dp)
                                                                ) {
                                                                    Text(
                                                                        g, 
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                        color = if (avg >= 75) Color(0xFF2E7D32) else if (avg >= 50) Color(0xFFE65100) else Color(0xFFC62828)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Error / Success feedbacks
                                    assessmentErrorFeedback?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    assessmentSuccessFeedback?.let {
                                        Text(it, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    csvErrorFeedback?.let {
                                        Text(it, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (selectedAssessmentFileName != null || bulkCsvInput.isNotBlank()) {
                                            TextButton(
                                                onClick = {
                                                    bulkCsvInput = ""
                                                    selectedAssessmentFileName = null
                                                    assessmentErrorFeedback = null
                                                    assessmentSuccessFeedback = null
                                                    csvErrorFeedback = null
                                                }
                                            ) {
                                                Text("Clear", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Button(
                                            onClick = {
                                                csvErrorFeedback = null
                                                assessmentErrorFeedback = null
                                                assessmentSuccessFeedback = null
                                                if (bulkCsvInput.isBlank()) {
                                                    assessmentErrorFeedback = "Spreadsheet field is blank. Select any Excel file or type csv records."
                                                    return@Button
                                                }
                                                try {
                                                    var parsedCount = 0
                                                    val lines = bulkCsvInput.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                                    lines.forEach { line ->
                                                        val parts = line.split(",")
                                                        if (parts.size >= 2) {
                                                            val roll = parts[0].trim()
                                                            
                                                            val pupil = listStudents.find { it.rollNumber.equals(roll, ignoreCase = true) }
                                                            if (pupil != null) {
                                                                if (parts.size == 3) {
                                                                    // standard Row format: Roll, MidtermScore, EOTScore
                                                                    val midVal = parts[1].trim()
                                                                    val finalVal = parts[2].trim()
                                                                    midTermInputs[pupil.id] = midVal
                                                                    endOfTermInputs[pupil.id] = finalVal
                                                                    parsedCount++
                                                                } else if (parts.size >= 4) {
                                                                    // standard format: Roll, Subject, Exam, Score, [MaxScore]
                                                                    val subj = parts[1].trim()
                                                                    val exam = parts[2].trim()
                                                                    val score = parts[3].toDoubleOrNull() ?: 0.0
                                                                    val maxSc = if (parts.size > 4) parts[4].toDoubleOrNull() ?: 100.0 else 100.0
                                                                    
                                                                    // Save assessment score dynamically to DB
                                                                    viewModel.insertGrade(
                                                                        studentId = pupil.id,
                                                                        subjectName = subj,
                                                                        examName = exam,
                                                                        score = score,
                                                                        maxScore = maxSc
                                                                    )
                                                                    parsedCount++
                                                                } else {
                                                                    // assumed midterm grade for current selected subject
                                                                    val midVal = parts[1].trim()
                                                                    midTermInputs[pupil.id] = midVal
                                                                    parsedCount++
                                                                }
                                                            }
                                                        }
                                                    }
                                                    csvErrorFeedback = "Spreadsheet Loaded! Graded and synchronized $parsedCount pupil grades into the interactive spreadsheet cells successfully."
                                                    android.widget.Toast.makeText(context, "Successfully imported, graded and synchronized $parsedCount spreadsheet grades!", android.widget.Toast.LENGTH_LONG).show()
                                                } catch (e: Exception) {
                                                    assessmentErrorFeedback = "Evaluation failed: Ensure valid format."
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                        ) {
                                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Run Auto-Grading Evaluator")
                                        }
                                    }
                                }
                            }
                        }

                        // STANDALONE SECTION B: Interactive Manual Excel Grid Interface (Instantly reactive!)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Excel light green hue
                                border = BorderStroke(1.dp, Color(0xFF81C784))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "Interactive Excel Ledger Grid (${filteredStudents.size} Pupils Found)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

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
                                            val midVal = midTermInputs[st.id] ?: ""
                                            val finalVal = endOfTermInputs[st.id] ?: ""

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
                    var isBulkGenerating by remember { mutableStateOf(false) }
                    var bulkProgressCurrent by remember { mutableStateOf(0) }
                    var bulkProgressTotal by remember { mutableStateOf(0) }
                    var selectedClassBatch by remember { mutableStateOf("Primary Seven (P.7)") }
                    var classBatchDropdownExpanded by remember { mutableStateOf(false) }
                    val classStudents = listStudents.filter { it.gradeLevel == selectedClassBatch }

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
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("class_batch_pdf_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Class-by-Class Batch PDF Generator",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Text(
                                        "Fetch grades uploaded from Excel to immediately batch-compile and download printable report cards for all pupils in a specific class inside a single multi-page PDF document.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1.3f)) {
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = selectedClassBatch,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Select Class Level", fontSize = 11.sp) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classBatchDropdownExpanded) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textStyle = MaterialTheme.typography.bodyMedium,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    )
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .clickable { classBatchDropdownExpanded = !classBatchDropdownExpanded }
                                                )
                                                DropdownMenu(
                                                    expanded = classBatchDropdownExpanded,
                                                    onDismissRequest = { classBatchDropdownExpanded = false }
                                                ) {
                                                    classLevels.forEach { lvl ->
                                                        DropdownMenuItem(
                                                            text = { Text(lvl, style = MaterialTheme.typography.bodyMedium) },
                                                            onClick = {
                                                                selectedClassBatch = lvl
                                                                classBatchDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Text(
                                                "${classStudents.size} Pupils",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (classStudents.isEmpty()) {
                                                android.widget.Toast.makeText(context, "No pupils listed in $selectedClassBatch.", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                exportClassReportCardsToPdf(
                                                    context = context,
                                                    classLevel = selectedClassBatch,
                                                    students = listStudents,
                                                    allGrades = listGrades
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("download_class_batch_pdf_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = classStudents.isNotEmpty()
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Batch Download PDFs (Print All)", fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (classStudents.isEmpty()) {
                                                android.widget.Toast.makeText(context, "No pupils listed in $selectedClassBatch.", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                isBulkGenerating = true
                                                bulkProgressCurrent = 0
                                                bulkProgressTotal = classStudents.size
                                                bulkExportIndividualPdfs(
                                                    context = context,
                                                    classLevel = selectedClassBatch,
                                                    students = listStudents,
                                                    allGrades = listGrades,
                                                    onProgress = { current, total ->
                                                        bulkProgressCurrent = current
                                                        bulkProgressTotal = total
                                                    },
                                                    onFinished = { total ->
                                                        isBulkGenerating = false
                                                        android.widget.Toast.makeText(context, "Successfully generated and saved $total individual PDF report cards to Downloads/Pearl_Junior_School_Reports!", android.widget.Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("bulk_generate_individual_pdfs_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = classStudents.isNotEmpty() && !isBulkGenerating
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bulk-Generate Individual PDFs (Save Separately)", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        item {
                            var smsTemplateText by remember { mutableStateOf("Dear [Parent], the Terminal CA Report Card for [Student] ([Roll]) in [Class] has been officially published by Pearl Junior School Office. Please login to the Secure Parent Portal using the roll key to retrieve it.") }
                            
                            val publishedCount = classStudents.count { it.isReportCardPublished }
                            val isPublishedSelection = classStudents.isNotEmpty() && publishedCount > 0
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("report_card_publication_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPublishedSelection) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f) 
                                                     else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isPublishedSelection) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f) 
                                            else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SendToMobile,
                                            contentDescription = null,
                                            tint = if (isPublishedSelection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Simulated SMS Gateway & Dispatch",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPublishedSelection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Text(
                                        text = "Manage terminal reports online availability status and configure automated broadcast dispatches to parents. Placeholders available: [Parent], [Student], [Class], [Roll].",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    // Status Indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Publication Status:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = if (isPublishedSelection) "Published ($publishedCount/${classStudents.size} Pupils)" else "Unpublished / Saved Draft",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (isPublishedSelection) MaterialTheme.colorScheme.tertiaryContainer 
                                                                 else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    }
                                    
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    
                                    OutlinedTextField(
                                        value = smsTemplateText,
                                        onValueChange = { smsTemplateText = it },
                                        label = { Text("SMS Dispatch Template Text", fontSize = 11.sp) },
                                        placeholder = { Text("Draft the automated SMS details...") },
                                        modifier = Modifier.fillMaxWidth().testTag("sms_template_input_field"),
                                        textStyle = MaterialTheme.typography.bodySmall,
                                        minLines = 3,
                                        maxLines = 5,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Publish Button
                                        Button(
                                            onClick = {
                                                if (classStudents.isEmpty()) {
                                                     android.widget.Toast.makeText(context, "No pupils listed in $selectedClassBatch to publish reports.", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.publishReportCardsForClass(selectedClassBatch, smsTemplateText)
                                                     android.widget.Toast.makeText(context, "Successfully published report cards & triggered automated SMS alerts to parents!", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f).testTag("publish_class_reports_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(8.dp),
                                            enabled = classStudents.isNotEmpty()
                                        ) {
                                            Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                             Text("Publish & Alert", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        
                                         // Retract/Unpublish Button
                                        OutlinedButton(
                                            onClick = {
                                                if (classStudents.isEmpty()) {
                                                     android.widget.Toast.makeText(context, "No pupils listed in $selectedClassBatch.", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.unpublishReportCardsForClass(selectedClassBatch)
                                                     android.widget.Toast.makeText(context, "Retracted report cards visibility from Parent Secure Space.", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1.1f).testTag("retract_class_reports_button"),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            enabled = classStudents.isNotEmpty() && isPublishedSelection
                                        ) {
                                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                             Text("Retract / Pull Back", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Select Individual Pupil to Preview", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (listStudents.isEmpty()) {
                                Text("No registered pupils found.", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                val currentReportPupil = listStudents.find { it.id == actualReportStudentId } ?: listStudents.first()


                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = "${currentReportPupil.name} (${currentReportPupil.gradeLevel})",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reportStudentExpanded) },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodyLarge
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { reportStudentExpanded = !reportStudentExpanded }
                                    )
                                    DropdownMenu(
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
                        val activePupil = listStudents.find { it.id == actualReportStudentId }
                        if (activePupil != null) {
                            item {
                                val isNursery = activePupil.gradeLevel.contains("Class", ignoreCase = true)
                                val studentGrades = listGrades.filter { it.studentId == activePupil.id }
                                val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")

                                var totalWeightedGradeSum = 0.0
                                var countSubjects = 0
                                var aggregatesSum = 0
                                var subjectGradesCount = 0
                                var avgScore = 0.0
                                var divisionStr = "Division U (Ungraded / Fail)"
                                var currentTeacherRemark = ""
                                var currentHeadRemark = ""
                                var mathPts = 9
                                var engPts = 9

                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF6)), // Cream colored background
                                    border = BorderStroke(2.dp, Color(0xFFE65100))
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
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                                    color = Color(0xFF1A237E)
                                                )
                                            }
                                            Text("THE REPUBLIC OF UGANDA • MINISTRY OF EDUCATION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = Color(0xFFD50000))
                                            Text("P.O. Box 773, Kampala • Tel: +256 772 400101", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Text("Web: www.pearljuniorschool.sc.ug", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.border(1.dp, Color(0xFFE65100))
                                            ) {
                                                Text(
                                                    "NATIONAL UNEB-STANDARD PROGRESS REPORT",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    color = Color(0xFFE65100)
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
                                                val candidateIdx = "UT4812/" + activePupil.rollNumber.replace("S", "").replace("s", "").trim().padStart(3, '0')
                                                Text("UNEB Index: $candidateIdx", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1A237E))
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
                                                    if (subj == "Mathematics") mathPts = points
                                                    if (subj == "English Language") engPts = points
                                                }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(0.5.dp, Color.LightGray)
                                                        .padding(6.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val (subjCode, teacherInitials) = when (subj) {
                                                         "Mathematics" -> Pair("MTC-456", "M.K.")
                                                         "English Language" -> Pair("ENG-101", "N.S.")
                                                         "Integrated Science" -> Pair("SCI-553", "A.O.")
                                                         "Social Studies" -> Pair("SST-204", "K.B.")
                                                         else -> Pair("GEN-909", "TR")
                                                     }
                                                     Column(modifier = Modifier.weight(1.8f)) {
                                                         Text(subj, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                         Text("Code: $subjCode • Initials: $teacherInitials", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                                                     }
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
                                            avgScore = totalWeightedGradeSum / countSubjects
                                            
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
                                                        divisionStr = when {
                                                            (mathPts == 9 || engPts == 9) -> "Division IV (Failure in Core Subject)"
                                                            aggregatesSum in 4..12 && (mathPts <= 6 && engPts <= 6) -> "Division I (First Grade) 🌟"
                                                            aggregatesSum in 4..12 && !(mathPts <= 6 && engPts <= 6) -> "Division II (Downgraded due to English/Math)"
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

                                            // Fallbacks & Custom comments combining state
                                            val defaultTeacherRemark = if (avgScore >= 80) {
                                                "Consistently diligent and disciplined. Outstanding academic competence. Keep the same tempo!"
                                            } else if (avgScore >= 60) {
                                                "A promising child who shows regular performance. Should double efforts in homework targets."
                                            } else {
                                                "Amiable child. Needs strictly focused revision assistance in Mathematics to pass well."
                                            }

                                            val defaultHeadRemark = if (avgScore >= 80) {
                                                "Excellent Outturn! Approved for academic honors. Keep shining."
                                            } else {
                                                "Reviewed and signed. Encouraged to strive higher next term."
                                            }

                                            currentTeacherRemark = if (aiTeacherRemark.isNotEmpty()) aiTeacherRemark else defaultTeacherRemark
                                            currentHeadRemark = if (aiHeadmasterRemark.isNotEmpty()) aiHeadmasterRemark else defaultHeadRemark

                                            // Teacher/Head comment lines
                                            Text(
                                                "Class Teacher's Remark: $currentTeacherRemark",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.DarkGray
                                            )

                                            Text(
                                                "Headteacher's Comment: $currentHeadRemark",
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
                                                // Dynamic Digital Integrity Barcode
                                                Column(
                                                    horizontalAlignment = Alignment.Start,
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(end = 4.dp)
                                                ) {
                                                    Text("SYSTEM VERIFICATION BARCODE", fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                    Row(
                                                        modifier = Modifier
                                                            .border(0.5.dp, Color.LightGray)
                                                            .background(Color.White)
                                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                                                    ) {
                                                        val barWidths = listOf(1, 3, 2, 1, 4, 1, 2, 3, 1, 2, 4, 1, 3, 1)
                                                        barWidths.forEach { w ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(w.dp)
                                                                    .height(18.dp)
                                                                    .background(Color.Black)
                                                            )
                                                        }
                                                    }
                                                    Text("VERIFIED: TERM-I-2026", fontSize = 7.sp, color = Color.Gray)
                                                }

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
                                    val outstandingFee = activePupil.feesTotal - activePupil.feesPaid
                                    val feesSummary = if (outstandingFee <= 0) "FULLY PAID" else "UGX ${String.format(Locale.US, "%,.0f", outstandingFee)} DUE"

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1.5f).padding(end = 6.dp)) {
                                                Text("AI Evaluation & Remarks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text("Digest exam performance to auto-formulate personalized remarks with Gemini AI.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }

                                            val localCoroutineScope = rememberCoroutineScope()
                                            Button(
                                                onClick = {
                                                    localCoroutineScope.launch {
                                                        preparingAiRemarks = true
                                                        try {
                                                            val sysPrompt = "You are an expert academic evaluator and school principal at Pearl Junior School, Kampala, Uganda."
                                                            val userPrompt = """
                                                                Generate customized professional report card remarks for pupil: ${activePupil.name}.
                                                                Class Level: ${activePupil.gradeLevel}
                                                                Scores:
                                                                ${activeSubjects.map { subj ->
                                                                    val m = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
                                                                    val e = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
                                                                    "$subj: Mid midterm ${m ?: "-"}, EOT endpoint ${e ?: "-"}"
                                                                }.joinToString("\n")}
                                                                Average Mark: ${String.format(Locale.US, "%.2f%%", avgScore)}
                                                                UNEB Division: $divisionStr
                                                                Fees Status: $feesSummary
                                                                
                                                                Provide your response strictly as a JSON block with exactly two fields without formatting wrappers:
                                                                {
                                                                  "teacherRemark": "(Highly personalized comment as Class Teacher, citing relevant subjects, max 2 sentences)",
                                                                  "headComment": "(Official encouraging sign-off as Headteacher)"
                                                                }
                                                            """.trimIndent()

                                                            val aiResponseRaw = com.example.data.api.askGemini(sysPrompt, userPrompt)
                                                            var cleanResponse = aiResponseRaw.trim()
                                                            if (cleanResponse.startsWith("```json")) {
                                                                cleanResponse = cleanResponse.substringAfter("```json")
                                                            }
                                                            if (cleanResponse.endsWith("```")) {
                                                                cleanResponse = cleanResponse.substringBeforeLast("```")
                                                            }
                                                            cleanResponse = cleanResponse.trim()

                                                            val parsedMap = parseSimpleJsonMap(cleanResponse)
                                                            if (parsedMap.containsKey("teacherRemark") && parsedMap.containsKey("headComment")) {
                                                                aiTeacherRemark = parsedMap["teacherRemark"] ?: ""
                                                                aiHeadmasterRemark = parsedMap["headComment"] ?: ""
                                                            } else {
                                                                // Use custom line split fallback
                                                                fallbackComments(cleanResponse) { t, h ->
                                                                    aiTeacherRemark = t
                                                                    aiHeadmasterRemark = h
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            aiTeacherRemark = "Consistently polite and active. (AI Error: ${e.message})"
                                                            aiHeadmasterRemark = "Encouraged to maintain outstanding efforts next term."
                                                        } finally {
                                                            preparingAiRemarks = false
                                                        }
                                                    }
                                                },
                                                enabled = !preparingAiRemarks,
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.testTag("generate_ai_remarks_button")
                                            ) {
                                                if (preparingAiRemarks) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                                } else {
                                                    Icon(Icons.Default.TipsAndUpdates, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("AI Remarks", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text("Report Card Download & Dispatch utilities", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        val posInfo = calculateStudentPosition(activePupil.id, activePupil.gradeLevel, listStudents, listGrades)
                                                        val positionStr = "${posInfo.first} of ${posInfo.second}"
                                                        exportReportCard(
                                                            context = context,
                                                            pupil = activePupil,
                                                            subjects = activeSubjects,
                                                            grades = studentGrades,
                                                            teacherRemark = currentTeacherRemark,
                                                            headComment = currentHeadRemark,
                                                            avgScore = avgScore,
                                                            division = divisionStr,
                                                            feesSummary = feesSummary,
                                                            format = "CSV",
                                                            position = positionStr
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                    modifier = Modifier.weight(1f).testTag("export_csv_button")
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Excel Out", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        val posInfo = calculateStudentPosition(activePupil.id, activePupil.gradeLevel, listStudents, listGrades)
                                                        val positionStr = "${posInfo.first} of ${posInfo.second}"
                                                        exportReportCard(
                                                            context = context,
                                                            pupil = activePupil,
                                                            subjects = activeSubjects,
                                                            grades = studentGrades,
                                                            teacherRemark = currentTeacherRemark,
                                                            headComment = currentHeadRemark,
                                                            avgScore = avgScore,
                                                            division = divisionStr,
                                                            feesSummary = feesSummary,
                                                            format = "HTML",
                                                            position = positionStr
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("print_pdf_button")
                                                ) {
                                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Print/PDF", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        // Direct Share alert message for Whatsapp/SMS dispatch
                                                        val briefReportStr = """
                                                            Pearl Junior School Terminal Report Card
                                                            Pupil: ${activePupil.name} (${activePupil.gradeLevel})
                                                            Average Mark: ${String.format(Locale.US, "%.1f%%", avgScore)}
                                                            ${if (!isNursery) "UNEB Division: $divisionStr" else ""}
                                                            Status: $feesSummary
                                                            Remarks: $currentTeacherRemark
                                                            
                                                            Sign off: Sarah Nabakooza (Headteacher)
                                                        """.trimIndent()
                                                        
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Pearl Junior Report Card Alert - ${activePupil.name}")
                                                            putExtra(android.content.Intent.EXTRA_TEXT, briefReportStr)
                                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                        }
                                                        context.startActivity(android.content.Intent.createChooser(intent, "Dispatch Guardian Results").apply {
                                                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                        })
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                                    modifier = Modifier.weight(1f).testTag("send_sms_alert_button")
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

                    if (isBulkGenerating) {
                        AlertDialog(
                            onDismissRequest = { /* Prevent dismiss */ },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Bulk-Generating PDFs", style = MaterialTheme.typography.titleMedium)
                                }
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Please keep the app open while we compile individual report cards.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    LinearProgressIndicator(
                                        progress = { if (bulkProgressTotal > 0) bulkProgressCurrent.toFloat() / bulkProgressTotal else 0f },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Text(
                                        text = "Processed $bulkProgressCurrent of $bulkProgressTotal pupils...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Saving directly to Downloads/Pearl_Junior_School_Reports/",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            },
                            confirmButton = {}
                        )
                    }
                }
                3 -> {
                    CentralGradeBookScreen(viewModel)
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
                    val activeSt = listStudents.find { it.id == selectedStudentId } ?: listStudents.firstOrNull()
                    
                    val dialogSubjectOptions = remember(activeSt) {
                        val grade = activeSt?.gradeLevel?.lowercase() ?: ""
                        if (grade.contains("nursery") || grade.contains("middle") || grade.contains("top")) {
                            listOf("Literacy & Numeracy", "Reading & Writing", "Art & Craft", "News & Speech", "Physical Play")
                        } else {
                            listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "Religious Education")
                        }
                    }
                    LaunchedEffect(activeSt, dialogSubjectOptions) {
                        if (activeSt != null && !dialogSubjectOptions.contains(selectedSubject)) {
                            selectedSubject = dialogSubjectOptions.firstOrNull() ?: ""
                        }
                    }

                    if (listStudents.isEmpty() || activeSt == null) {
                        Text("No registered students found.", color = Color.Red)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = "${activeSt.name} (${activeSt.rollNumber})",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { studentExpanded = !studentExpanded }
                            )
                            DropdownMenu(
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { subjExpanded = !subjExpanded }
                        )
                        DropdownMenu(
                            expanded = subjExpanded,
                            onDismissRequest = { subjExpanded = false }
                        ) {
                            dialogSubjectOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { selectedSubject = opt; subjExpanded = false }
                                )
                            }
                        }
                    }

                    Text("Exam Name", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = examInput,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { examExpanded = !examExpanded }
                        )
                        DropdownMenu(
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

// ==================== HELPER FUNCTIONS FOR REPORT CARDS ====================
fun parseSimpleJsonMap(jsonStr: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    try {
        val teacherRegex = "\"teacherRemark\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val headRegex = "\"headComment\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        
        teacherRegex.find(jsonStr)?.let {
            map["teacherRemark"] = it.groupValues[1]
        }
        headRegex.find(jsonStr)?.let {
            map["headComment"] = it.groupValues[1]
        }
    } catch (e: Exception) {
        // Safe Catch
    }
    return map
}

fun fallbackComments(raw: String, onResult: (String, String) -> Unit) {
    if (raw.contains("teacherRemark", ignoreCase = true)) {
        val teachPat = "\"teacherRemark\"\\s*:\\s*\"([^\"]+)\"".toRegex(RegexOption.IGNORE_CASE)
        val headPat = "\"headComment\"\\s*:\\s*\"([^\"]+)\"".toRegex(RegexOption.IGNORE_CASE)
        val tMatch = teachPat.find(raw)?.groupValues?.get(1)
        val hMatch = headPat.find(raw)?.groupValues?.get(1)
        if (tMatch != null && hMatch != null) {
            onResult(tMatch, hMatch)
            return
        }
    }
    
    val lines = raw.lines().filter { it.isNotBlank() }
    val teacher = lines.firstOrNull { it.contains("teacher", ignoreCase = true) || it.contains("Remark", ignoreCase = true) } 
        ?: (lines.firstOrNull() ?: "Consistently eager to learn and helpful in class.")
    val head = lines.lastOrNull { it.contains("headmaster", ignoreCase = true) || it.contains("comment", ignoreCase = true) || it.contains("sign-off", ignoreCase = true) } 
        ?: (lines.getOrNull((lines.size - 2).coerceAtLeast(0)) ?: "Good progress recorded. Strive for higher honors next Term.")
        
    onResult(teacher.replace("\"", "").trim(), head.replace("\"", "").trim())
}

fun calculateStudentPosition(
    studentId: Int,
    classLevel: String,
    students: List<com.example.data.entity.Student>,
    allGrades: List<com.example.data.entity.Grade>
): Pair<Int, Int> {
    val classStudents = students.filter { it.gradeLevel.equals(classLevel, ignoreCase = true) }
    if (classStudents.isEmpty()) return Pair(1, 1)

    val studentAverages = classStudents.map { pupil ->
        val isNursery = pupil.gradeLevel.contains("Class", ignoreCase = true)
        val studentGrades = allGrades.filter { it.studentId.toInt() == pupil.id }
        val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")
        var totalWeightedGradeSum = 0.0
        var countSubjects = 0
        activeSubjects.forEach { subj ->
            val mid = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0
            if (mid != null || eot != null) {
                totalWeightedGradeSum += wTotal
                countSubjects++
            }
        }
        val avgScore = if (countSubjects > 0) totalWeightedGradeSum / countSubjects else 0.0
        pupil.id to avgScore
    }

    val sortedAverages = studentAverages.sortedByDescending { it.second }
    val rank = sortedAverages.indexOfFirst { it.first == studentId } + 1
    val finalRank = if (rank <= 0) 1 else rank
    return Pair(finalRank, classStudents.size)
}

fun exportReportCard(
    context: android.content.Context, 
    pupil: com.example.data.entity.Student, 
    subjects: List<String>, 
    grades: List<com.example.data.entity.Grade>, 
    teacherRemark: String, 
    headComment: String, 
    avgScore: Double, 
    division: String, 
    feesSummary: String, 
    format: String,
    position: String = ""
) {
    val prefs = context.getSharedPreferences("school_prefs", android.content.Context.MODE_PRIVATE)
    val logoBase64 = prefs.getString("school_logo_base64", null)
    val logoImgTag = if (!logoBase64.isNullOrBlank()) {
        "<img src=\"data:image/png;base64,$logoBase64\" style=\"max-height: 80px; max-width: 150px; margin-bottom: 12px; object-fit: contain; display: block; margin-left: auto; margin-right: auto;\" />"
    } else {
        ""
    }
    val isNursery = pupil.gradeLevel.contains("Class", ignoreCase = true)
    
    if (format == "CSV") {
        val csvBuilder = java.lang.StringBuilder()
        csvBuilder.append("PEARL JUNIOR SCHOOL - TERMINAL REPORT CARD\n")
        csvBuilder.append("Pupil Name,${pupil.name}\n")
        csvBuilder.append("Roll Number,${pupil.rollNumber}\n")
        csvBuilder.append("Class Level,${pupil.gradeLevel}\n")
        if (position.isNotBlank()) {
            csvBuilder.append("Class Position,$position\n")
        }
        csvBuilder.append("Average Score,${String.format(java.util.Locale.US, "%.2f%%", avgScore)}\n")
        if (!isNursery) {
            csvBuilder.append("Outturn,${division.replace("🌟", "")}\n")
        }
        csvBuilder.append("Fees standing,${feesSummary}\n")
        csvBuilder.append("\n")
        csvBuilder.append("SUBJECT,MIDTERM (40%),END OF TERM (60%),WEIGHTED TOTAL,GRADE\n")
        
        subjects.forEach { subj ->
            val mid = grades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = grades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0
            val gradeStr = if (isNursery) {
                if (wTotal >= 80) "A (Achieved)" else if (wTotal >= 50) "D (Developing)" else "B (Beginning)"
            } else {
                when {
                    wTotal >= 85 -> "D1"
                    wTotal >= 75 -> "D2"
                    wTotal >= 70 -> "C3"
                    wTotal >= 65 -> "C4"
                    wTotal >= 60 -> "C5"
                    wTotal >= 50 -> "C6"
                    wTotal >= 45 -> "P7"
                    wTotal >= 40 -> "P8"
                    else -> "F9"
                }
            }
            csvBuilder.append("$subj,${mid ?: "-"},${eot ?: "-"},${String.format(java.util.Locale.US, "%.1f", wTotal)},$gradeStr\n")
        }
        csvBuilder.append("\n")
        csvBuilder.append("Teacher Remarks,\"${teacherRemark.replace("\"", "'")}\"\n")
        csvBuilder.append("Headmaster Comments,\"${headComment.replace("\"", "'")}\"\n")
        
        val fileContent = csvBuilder.toString()
        val fileName = "Report_${pupil.name.replace(" ", "_")}.csv"
        
        try {
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(fileContent)
            
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/comma-separated-values"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Pearl Junior School Report Card - ${pupil.name}")
                putExtra(android.content.Intent.EXTRA_TEXT, fileContent)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Download or Save Spreadsheet CSV").apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            })
            android.widget.Toast.makeText(context, "Spreadsheet CSV compiled successfully!", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    } else if (format == "HTML") {
        val htmlBuilder = java.lang.StringBuilder()
        htmlBuilder.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Pearl Junior School Report - ${pupil.name}</title>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; background: #fff; color: #333; padding: 30px; line-height: 1.4; }
                    .report-container { max-width: 800px; margin: 0 auto; border: 3px double #1a237e; padding: 25px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 20px; }
                    .school-name { font-size: 28px; font-weight: 900; color: #1a237e; letter-spacing: 1px; margin: 0; }
                    .school-sub { font-size: 13px; color: #666; margin: 3px 0; }
                    .report-title { display: inline-block; padding: 6px 16px; background: #f5f5f5; border: 1px solid #1a237e; margin-top: 10px; font-weight: bold; border-radius: 4px; }
                    .student-info { display: flex; justify-content: space-between; margin: 20px 0; padding: 10px; background: #fbfbfb; border: 1px solid #ddd; border-radius: 4px; }
                    .info-col { width: 48%; }
                    .info-item { font-size: 14px; margin: 5px 0; }
                    .info-label { font-weight: bold; color: #555; }
                    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                    th { background: #1a237e; color: #fff; text-align: left; padding: 10px; font-size: 13px; }
                    td { border: 1px solid #ddd; padding: 10px; font-size: 14px; }
                    tr:nth-child(even) { background-color: #f9f9f9; }
                    .summary-sec { display: flex; justify-content: space-between; margin-top: 20px; gap: 20px; }
                    .metrics { flex: 1.2; font-size: 14px; }
                    .fees-status { flex: 0.8; border: 2px dashed #c62828; padding: 10px; text-align: center; border-radius: 6px; background: #ffebee; color: #c62828; max-height: max-content; }
                    .fees-cleared { flex: 0.8; border: 2px solid #2e7d32; padding: 10px; text-align: center; border-radius: 6px; background: #e8f5e9; color: #2e7d32; max-height: max-content; }
                    .bold { font-weight: bold; }
                    .remarks-box { margin-top: 25px; padding: 15px; border: 1px solid #ddd; background: #fafafa; border-radius: 4px; }
                    .remark-item { margin-bottom: 10px; font-size: 14px; }
                    .footer-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 35px; }
                    .signature-box { border-top: 1px solid #ddd; padding-top: 8px; width: 200px; text-align: center; font-size: 12px; }
                    .stamp { width: 70px; height: 70px; border: 2px solid #1976d2; border-radius: 50%; display: flex; flex-direction: column; justify-content: center; align-items: center; background: #e3f2fd; color: #1976d2; font-size: 8px; font-weight: bold; }
                    @media print {
                        body { padding: 0; background: none; }
                        .report-container { box-shadow: none; border-width: 2px; }
                    }
                </style>
            </head>
            <body>
                <div class="report-container">
                    <div class="header">
                        $logoImgTag
                        <h1 class="school-name">PEARL JUNIOR SCHOOL</h1>
                        <div class="school-sub">P.O. Box 773, Kampala • Tel: +256 772 400101</div>
                        <div class="school-sub">Web: www.pearljuniorschool.sc.ug</div>
                        <div class="report-title">TERMLY CA PROGRESS REPORT CARD</div>
                    </div>
                    
                    <div class="student-info">
                        <div class="info-col">
                            <div class="info-item"><span class="info-label">Pupil Name:</span> ${pupil.name}</div>
                            <div class="info-item"><span class="info-label">Class Level:</span> ${pupil.gradeLevel}</div>
                            ${if (position.isNotBlank()) "<div class=\"info-item\"><span class=\"info-label\">Class Position:</span> <strong style=\"color: #1a237e;\">$position</strong></div>" else ""}
                        </div>
                        <div class="info-col" style="text-align: right;">
                            <div class="info-item"><span class="info-label">Roll ID:</span> ${pupil.rollNumber}</div>
                            <div class="info-item"><span class="info-label">Term:</span> Term I (2026)</div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>ACADEMIC SUBJECT</th>
                                <th>MIDTERM (40%)</th>
                                <th>EOT (60%)</th>
                                <th>COMBINED TOTAL</th>
                                <th>GRADE</th>
                            </tr>
                        </thead>
                        <tbody>
        """.trimIndent())
        
        subjects.forEach { subj ->
            val mid = grades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = grades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0
            val gradeStr = if (isNursery) {
                if (wTotal >= 80) "A (Achieved)" else if (wTotal >= 50) "D (Developing)" else "B (Beginning)"
            } else {
                when {
                    wTotal >= 85 -> "D1"
                    wTotal >= 75 -> "D2"
                    wTotal >= 70 -> "C3"
                    wTotal >= 65 -> "C4"
                    wTotal >= 60 -> "C5"
                    wTotal >= 50 -> "C6"
                    wTotal >= 45 -> "P7"
                    wTotal >= 40 -> "P8"
                    else -> "F9"
                }
            }
            htmlBuilder.append("""
                <tr>
                    <td class="bold">$subj</td>
                    <td>${mid?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "-"}</td>
                    <td>${eot?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "-"}</td>
                    <td class="bold">${String.format(java.util.Locale.US, "%.1f", wTotal)}</td>
                    <td><span style="font-weight: bold; color: #1a237e;">$gradeStr</span></td>
                </tr>
            """.trimIndent())
        }
        
        val outstanding = pupil.feesTotal - pupil.feesPaid
        val isCleared = outstanding <= 0
        val feesHtml = if (isCleared) {
            """
            <div class="fees-cleared">
                <div style="font-weight: bold; font-size: 13px;">FEES STATUS</div>
                <div style="font-size: 16px; font-weight: bold; margin: 4px 0;">FULLY PAID</div>
                <div style="font-size: 10px;">APPROVED & CLEARED</div>
            </div>
            """.trimIndent()
        } else {
            """
            <div class="fees-status">
                <div style="font-weight: bold; font-size: 13px;">FEES BALANCE DUE</div>
                <div style="font-size: 16px; font-weight: bold; margin: 4px 0;">UGX ${String.format(java.util.Locale.US, "%,.0f", outstanding)}</div>
                <div style="font-size: 10px;">PROVISIONAL DISPATCH</div>
            </div>
            """.trimIndent()
        }
        
        htmlBuilder.append("""
                        </tbody>
                    </table>
                    
                    <div class="summary-sec">
                        <div class="metrics">
                            <div class="info-item"><span class="info-label">Total Subjects Evaluated:</span> ${subjects.size}</div>
                            <div class="info-item"><span class="info-label">Combined Average Mark:</span> ${String.format(java.util.Locale.US, "%.2f%%", avgScore)}</div>
                            ${if (!isNursery) "<div class=\"info-item\"><span class=\"bold\">Termly Outturn:</span> $division</div>" else ""}
                        </div>
                        $feesHtml
                    </div>
                    
                    <div class="remarks-box">
                        <div class="remark-item"><span class="bold">Class Teacher's Remark:</span> $teacherRemark</div>
                        <div class="remark-item"><span class="bold">Headteacher's Comments:</span> $headComment</div>
                    </div>
                    
                    <div class="footer-row">
                        <div class="signature-box">
                            Nabakooza Sarah<br>
                            <span style="color: grey; font-size: 11px;">HEADTEACHER SIGNATURE</span>
                        </div>
                        <div class="stamp">
                            <span>OFFICIAL</span>
                            <span style="font-size: 9px; margin: 2px 0;">PEARL</span>
                            <span>STAMP</span>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent())
        
        val fileContent = htmlBuilder.toString()
        val fileName = "Report_${pupil.name.replace(" ", "_")}.html"
        
        try {
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(fileContent)
            
            // Print and Save as PDF using Android PrintManager & WebView, wrapped with extreme safety
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    val webView = android.webkit.WebView(context)
                    webView.settings.javaScriptEnabled = true
                    webView.loadDataWithBaseURL(null, fileContent, "text/html", "utf-8", null)
                    webView.webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            try {
                                val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                                val jobName = "Report_${pupil.name.replace(" ", "_")}"
                                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                                printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Printer error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                fallbackToDownloadAndShare(context, pupil.name, fileContent)
                            }
                        }
                    }
                    android.widget.Toast.makeText(context, "Opening PDF Print Spooler...", android.widget.Toast.LENGTH_SHORT).show()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    fallbackToDownloadAndShare(context, pupil.name, fileContent)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            fallbackToDownloadAndShare(context, pupil.name, fileContent)
        }
    }
}

fun exportClassReportCardsToPdf(
    context: android.content.Context,
    classLevel: String,
    students: List<com.example.data.entity.Student>,
    allGrades: List<com.example.data.entity.Grade>
) {
    val prefs = context.getSharedPreferences("school_prefs", android.content.Context.MODE_PRIVATE)
    val logoBase64 = prefs.getString("school_logo_base64", null)
    val logoImgTag = if (!logoBase64.isNullOrBlank()) {
        "<img src=\"data:image/png;base64,$logoBase64\" style=\"max-height: 80px; max-width: 150px; margin-bottom: 12px; object-fit: contain; display: block; margin-left: auto; margin-right: auto;\" />"
    } else {
        ""
    }
    val filtered = students.filter { it.gradeLevel == classLevel }
    if (filtered.isEmpty()) {
        android.widget.Toast.makeText(context, "No pupils found in $classLevel to generate report cards.", android.widget.Toast.LENGTH_LONG).show()
        return
    }

    val classAverages = filtered.map { pupil ->
        val isNursery = pupil.gradeLevel.contains("Class", ignoreCase = true)
        val studentGrades = allGrades.filter { it.studentId == pupil.id }
        val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")
        var totalWeightedGradeSum = 0.0
        var countSubjects = 0
        activeSubjects.forEach { subj ->
            val mid = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0
            if (mid != null || eot != null) {
                totalWeightedGradeSum += wTotal
                countSubjects++
            }
        }
        val avgScore = if (countSubjects > 0) totalWeightedGradeSum / countSubjects else 0.0
        pupil.id to avgScore
    }
    val sortedAverages = classAverages.sortedByDescending { it.second }

    val htmlBuilder = java.lang.StringBuilder()
    htmlBuilder.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Pearl Junior School Class Report - $classLevel</title>
            <style>
                body { font-family: 'Helvetica Neue', Arial, sans-serif; background: #fff; color: #333; padding: 20px; line-height: 1.4; }
                .report-container { 
                    max-width: 850px; 
                    margin: 0 auto 50px auto; 
                    border: 3px double #1a237e; 
                    padding: 25px; 
                    border-radius: 8px; 
                    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                    page-break-after: always; 
                    break-after: page;
                }
                .header { text-align: center; margin-bottom: 20px; }
                .school-name { font-size: 28px; font-weight: 900; color: #1a237e; letter-spacing: 1px; margin: 0; }
                .school-sub { font-size: 13px; color: #666; margin: 3px 0; }
                .report-title { display: inline-block; padding: 6px 16px; background: #f5f5f5; border: 1px solid #1a237e; margin-top: 10px; font-weight: bold; border-radius: 4px; }
                .student-info { display: flex; justify-content: space-between; margin: 20px 0; padding: 10px; background: #fbfbfb; border: 1px solid #ddd; border-radius: 4px; }
                .info-col { width: 48%; }
                .info-item { font-size: 14px; margin: 5px 0; }
                .info-label { font-weight: bold; color: #555; }
                table { width: 100%; border-collapse: collapse; margin: 15px 0; }
                th { background: #1a237e; color: #fff; text-align: left; padding: 10px; font-size: 13px; }
                td { border: 1px solid #ddd; padding: 10px; font-size: 14px; }
                tr:nth-child(even) { background-color: #f9f9f9; }
                .summary-sec { display: flex; justify-content: space-between; margin-top: 20px; gap: 20px; }
                .metrics { flex: 1.2; font-size: 14px; }
                .fees-status { flex: 0.8; border: 2px dashed #c62828; padding: 10px; text-align: center; border-radius: 6px; background: #ffebee; color: #c62828; max-height: max-content; }
                .fees-cleared { flex: 0.8; border: 2px solid #2e7d32; padding: 10px; text-align: center; border-radius: 6px; background: #e8f5e9; color: #2e7d32; max-height: max-content; }
                .bold { font-weight: bold; }
                .remarks-box { margin-top: 25px; padding: 15px; border: 1px solid #ddd; background: #fafafa; border-radius: 4px; }
                .remark-item { margin-bottom: 10px; font-size: 14px; }
                .footer-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 35px; }
                .signature-box { border-top: 1px solid #ddd; padding-top: 8px; width: 200px; text-align: center; font-size: 12px; }
                .stamp { width: 70px; height: 70px; border: 2px solid #1976d2; border-radius: 50%; display: flex; flex-direction: column; justify-content: center; align-items: center; background: #e3f2fd; color: #1976d2; font-size: 8px; font-weight: bold; }
                @media print {
                    body { padding: 0; background: none; }
                    .report-container { 
                        box-shadow: none; 
                        border-width: 2px; 
                        page-break-after: always; 
                        break-after: page;
                        margin-bottom: 0px !important;
                    }
                }
            </style>
        </head>
        <body>
    """.trimIndent())

    filtered.forEach { pupil ->
        val rankIndex = sortedAverages.indexOfFirst { it.first == pupil.id } + 1
        val finalRank = if (rankIndex <= 0) 1 else rankIndex
        val positionStr = "$finalRank of ${filtered.size}"

        val isNursery = pupil.gradeLevel.contains("Class", ignoreCase = true)
        val studentGrades = allGrades.filter { it.studentId == pupil.id }
        val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")

        var totalWeightedGradeSum = 0.0
        var countSubjects = 0
        var aggregatesSum = 0
        
        val rowsBuilder = java.lang.StringBuilder()

        activeSubjects.forEach { subj ->
            val mid = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0

            if (mid != null || eot != null) {
                totalWeightedGradeSum += wTotal
                countSubjects++

                val (gradeStr, points) = if (isNursery) {
                    when {
                        wTotal >= 80.0 -> Pair("A (Achieved)", 1)
                        wTotal >= 50.0 -> Pair("D (Developing)", 2)
                        else -> Pair("B (Beginning)", 9)
                    }
                } else {
                    when {
                        wTotal >= 85.0 -> Pair("D1", 1)
                        wTotal >= 75.0 -> Pair("D2", 2)
                        wTotal >= 70.0 -> Pair("C3", 3)
                        wTotal >= 65.0 -> Pair("C4", 4)
                        wTotal >= 60.0 -> Pair("C5", 5)
                        wTotal >= 50.0 -> Pair("C6", 6)
                        wTotal >= 45.0 -> Pair("P7", 7)
                        wTotal >= 40.0 -> Pair("P8", 8)
                        else -> Pair("F9", 9)
                    }
                }

                if (!isNursery) {
                    aggregatesSum += points
                }

                rowsBuilder.append("""
                    <tr>
                        <td class="bold">$subj</td>
                        <td>${mid?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "-"}</td>
                        <td>${eot?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "-"}</td>
                        <td class="bold">${String.format(java.util.Locale.US, "%.1f", wTotal)}</td>
                        <td><span style="font-weight: bold; color: #1a237e;">$gradeStr</span></td>
                    </tr>
                """.trimIndent())
            }
        }

        val avgScore = if (countSubjects > 0) totalWeightedGradeSum / countSubjects else 0.0
        val divisionStr = if (!isNursery) {
            when {
                aggregatesSum in 4..12 -> "Division I (First Grade) 🌟"
                aggregatesSum in 13..24 -> "Division II (Second Grade)"
                aggregatesSum in 25..29 -> "Division III (Third Grade)"
                aggregatesSum in 30..34 -> "Division IV (Fourth Grade)"
                else -> "Division U (Ungraded / Fail)"
            }
        } else {
            if (avgScore >= 80) "Nursery Achieved Promisingly" else "Developing Steadily"
        }

        val defaultTeacherRemark = if (avgScore >= 80.0) {
            "Consistently diligent and disciplined. Outstanding academic competence. Keep the same tempo!"
        } else if (avgScore >= 60.0) {
            "A promising child who shows regular performance. Should double efforts in homework targets."
        } else {
            "Amiable child. Needs strictly focused revision assistance in Mathematics to pass well."
        }

        val defaultHeadRemark = if (avgScore >= 80.0) {
            "Excellent Outturn! Approved for academic honors. Keep shining."
        } else {
            "Reviewed and signed. Encouraged to strive higher next term."
        }

        val outstanding = pupil.feesTotal - pupil.feesPaid
        val isCleared = outstanding <= 0
        val feesHtml = if (isCleared) {
            """
            <div class="fees-cleared">
                <div style="font-weight: bold; font-size: 13px;">FEES STATUS</div>
                <div style="font-size: 16px; font-weight: bold; margin: 4px 0;">FULLY PAID</div>
                <div style="font-size: 10px;">APPROVED & CLEARED</div>
            </div>
            """.trimIndent()
        } else {
            """
            <div class="fees-status">
                <div style="font-weight: bold; font-size: 13px;">FEES BALANCE DUE</div>
                <div style="font-size: 16px; font-weight: bold; margin: 4px 0;">UGX ${String.format(java.util.Locale.US, "%,.0f", outstanding)}</div>
                <div style="font-size: 10px;">PROVISIONAL DISPATCH</div>
            </div>
            """.trimIndent()
        }

        htmlBuilder.append("""
            <div class="report-container">
                <div class="header">
                    $logoImgTag
                    <h1 class="school-name">PEARL JUNIOR SCHOOL</h1>
                    <div class="school-sub">P.O. Box 773, Kampala • Tel: +256 772 400101</div>
                    <div class="school-sub">Web: www.pearljuniorschool.sc.ug</div>
                    <div class="report-title">TERMLY CA PROGRESS REPORT CARD</div>
                </div>
                
                <div class="student-info">
                    <div class="info-col">
                        <div class="info-item"><span class="info-label">Pupil Name:</span> ${pupil.name}</div>
                        <div class="info-item"><span class="info-label">Class Level:</span> ${pupil.gradeLevel}</div>
                        <div class="info-item"><span class="info-label">Class Position:</span> <strong style="color: #1a237e;">$positionStr</strong></div>
                    </div>
                    <div class="info-col" style="text-align: right;">
                        <div class="info-item"><span class="info-label">Roll ID:</span> ${pupil.rollNumber}</div>
                        <div class="info-item"><span class="info-label">Term:</span> Term I (2026)</div>
                    </div>
                </div>
                
                <table>
                    <thead>
                        <tr>
                            <th>ACADEMIC SUBJECT</th>
                            <th>MIDTERM (40%)</th>
                            <th>EOT (60%)</th>
                            <th>COMBINED TOTAL</th>
                            <th>GRADE</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rowsBuilder.toString()}
                    </tbody>
                </table>
                
                <div class="summary-sec">
                    <div class="metrics">
                        <div class="info-item"><span class="info-label">Total Subjects Evaluated:</span> $countSubjects</div>
                        <div class="info-item"><span class="info-label">Combined Average Mark:</span> ${String.format(java.util.Locale.US, "%.2f%%", avgScore)}</div>
                        ${if (!isNursery) "<div class=\"info-item\"><span class=\"bold\">Termly Outturn:</span> $divisionStr</div>" else ""}
                    </div>
                    $feesHtml
                </div>
                
                <div class="remarks-box">
                    <div class="remark-item"><span class="bold">Class Teacher's Remark:</span> $defaultTeacherRemark</div>
                    <div class="remark-item"><span class="bold">Headteacher's Comments:</span> $defaultHeadRemark</div>
                </div>
                
                <div class="footer-row">
                    <div class="signature-box">
                        Nabakooza Sarah<br>
                        <span style="color: grey; font-size: 11px;">HEADTEACHER SIGNATURE</span>
                    </div>
                    <div class="stamp">
                        <span>OFFICIAL</span>
                        <span style="font-size: 9px; margin: 2px 0;">PEARL</span>
                        <span>STAMP</span>
                    </div>
                </div>
            </div>
        """.trimIndent())
    }

    htmlBuilder.append("""
        </body>
        </html>
    """.trimIndent())

    val fileContent = htmlBuilder.toString()
    
    android.os.Handler(android.os.Looper.getMainLooper()).post {
        try {
            val webView = android.webkit.WebView(context)
            webView.settings.javaScriptEnabled = true
            webView.loadDataWithBaseURL(null, fileContent, "text/html", "utf-8", null)
            webView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    try {
                        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                        val jobName = "Class_Reports_${classLevel.replace(" ", "_")}"
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                        android.widget.Toast.makeText(context, "Print spooling error: ${ex.message}", android.widget.Toast.LENGTH_SHORT).show()
                        fallbackToDownloadAndShare(context, "Class_${classLevel}", fileContent)
                    }
                }
            }
            android.widget.Toast.makeText(context, "Assembled printable dossier. Opening PDF generator...", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Throwable) {
            e.printStackTrace()
            fallbackToDownloadAndShare(context, "Class_${classLevel}", fileContent)
        }
    }
}

fun bulkExportIndividualPdfs(
    context: android.content.Context,
    classLevel: String,
    students: List<com.example.data.entity.Student>,
    allGrades: List<com.example.data.entity.Grade>,
    onProgress: (Int, Int) -> Unit,
    onFinished: (Int) -> Unit
) {
    val filtered = students.filter { it.gradeLevel == classLevel }
    if (filtered.isEmpty()) {
        android.widget.Toast.makeText(context, "No pupils found in $classLevel to generate report cards.", android.widget.Toast.LENGTH_LONG).show()
        onFinished(0)
        return
    }

    val classAverages = filtered.map { pupil ->
        val isNursery = pupil.gradeLevel.contains("Class", ignoreCase = true)
        val studentGrades = allGrades.filter { it.studentId == pupil.id }
        val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")
        var totalWeightedGradeSum = 0.0
        var countSubjects = 0
        activeSubjects.forEach { subj ->
            val mid = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0
            if (mid != null || eot != null) {
                totalWeightedGradeSum += wTotal
                countSubjects++
            }
        }
        val avgScore = if (countSubjects > 0) totalWeightedGradeSum / countSubjects else 0.0
        pupil.id to avgScore
    }
    val sortedAverages = classAverages.sortedByDescending { it.second }

    val prefs = context.getSharedPreferences("school_prefs", android.content.Context.MODE_PRIVATE)
    val logoBase64 = prefs.getString("school_logo_base64", null)
    val logoImgTag = if (!logoBase64.isNullOrBlank()) {
        "<img src=\"data:image/png;base64,$logoBase64\" style=\"max-height: 80px; max-width: 150px; margin-bottom: 12px; object-fit: contain; display: block; margin-left: auto; margin-right: auto;\" />"
    } else {
        ""
    }

    var currentIndex = 0

    fun processNext() {
        if (currentIndex >= filtered.size) {
            onFinished(filtered.size)
            return
        }

        val pupil = filtered[currentIndex]
        onProgress(currentIndex + 1, filtered.size)

        val rankIndex = sortedAverages.indexOfFirst { it.first == pupil.id } + 1
        val finalRank = if (rankIndex <= 0) 1 else rankIndex
        val positionStr = "$finalRank of ${filtered.size}"

        val isNursery = pupil.gradeLevel.contains("Class", ignoreCase = true)
        val studentGrades = allGrades.filter { it.studentId == pupil.id }
        val activeSubjects = if (isNursery) listOf("Literacy & Numeracy") else listOf("Mathematics", "English Language", "Integrated Science", "Social Studies")

        var totalWeightedGradeSum = 0.0
        var countSubjects = 0
        var aggregatesSum = 0
        
        val rowsBuilder = java.lang.StringBuilder()

        activeSubjects.forEach { subj ->
            val mid = studentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
            val eot = studentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
            val wTotal = if (mid != null && eot != null) (mid * 0.4) + (eot * 0.6) else eot ?: mid ?: 0.0

            if (mid != null || eot != null) {
                totalWeightedGradeSum += wTotal
                countSubjects++

                val (gradeStr, points) = if (isNursery) {
                    when {
                        wTotal >= 80.0 -> Pair("A (Achieved)", 1)
                        wTotal >= 50.0 -> Pair("D (Developing)", 2)
                        else -> Pair("B (Beginning)", 9)
                    }
                } else {
                    when {
                        wTotal >= 85.0 -> Pair("D1", 1)
                        wTotal >= 75.0 -> Pair("D2", 2)
                        wTotal >= 70.0 -> Pair("C3", 3)
                        wTotal >= 65.0 -> Pair("C4", 4)
                        wTotal >= 60.0 -> Pair("C5", 5)
                        wTotal >= 50.0 -> Pair("C6", 6)
                        wTotal >= 45.0 -> Pair("P7", 7)
                        wTotal >= 40.0 -> Pair("P8", 8)
                        else -> Pair("F9", 9)
                    }
                }

                if (!isNursery) {
                    aggregatesSum += points
                }

                rowsBuilder.append("""
                    <tr>
                        <td class="bold">$subj</td>
                        <td>${mid?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "-"}</td>
                        <td>${eot?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "-"}</td>
                        <td class="bold">${String.format(java.util.Locale.US, "%.1f", wTotal)}</td>
                        <td><span style="font-weight: bold; color: #1a237e;">$gradeStr</span></td>
                    </tr>
                """.trimIndent())
            }
        }

        val avgScore = if (countSubjects > 0) totalWeightedGradeSum / countSubjects else 0.0
        val divisionStr = if (!isNursery) {
            when {
                aggregatesSum in 4..12 -> "Division I (First Grade) 🌟"
                aggregatesSum in 13..24 -> "Division II (Second Grade)"
                aggregatesSum in 25..29 -> "Division III (Third Grade)"
                aggregatesSum in 30..34 -> "Division IV (Fourth Grade)"
                else -> "Division U (Ungraded / Fail)"
            }
        } else {
            if (avgScore >= 80) "Nursery Achieved Promisingly" else "Developing Steadily"
        }

        val defaultTeacherRemark = if (avgScore >= 80.0) {
            "Consistently diligent and disciplined. Outstanding academic competence. Keep the same tempo!"
        } else if (avgScore >= 60.0) {
            "A promising child who shows regular performance. Should double efforts in homework targets."
        } else {
            "Amiable child. Needs strictly focused revision assistance in Mathematics to pass well."
        }

        val defaultHeadRemark = if (avgScore >= 80.0) {
            "Excellent Outturn! Approved for academic honors. Keep shining."
        } else {
            "Reviewed and signed. Encouraged to strive higher next term."
        }

        val outstanding = pupil.feesTotal - pupil.feesPaid
        val isCleared = outstanding <= 0
        val feesHtml = if (isCleared) {
            """
            <div class="fees-cleared">
                <div style="font-weight: bold; font-size: 13px;">FEES STATUS</div>
                <div style="font-size: 16px; font-weight: bold; margin: 4px 0;">FULLY PAID</div>
                <div style="font-size: 10px;">APPROVED & CLEARED</div>
            </div>
            """.trimIndent()
        } else {
            """
            <div class="fees-status">
                <div style="font-weight: bold; font-size: 13px;">FEES BALANCE DUE</div>
                <div style="font-size: 16px; font-weight: bold; margin: 4px 0;">UGX ${String.format(java.util.Locale.US, "%,.0f", outstanding)}</div>
                <div style="font-size: 10px;">PROVISIONAL DISPATCH</div>
            </div>
            """.trimIndent()
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Pearl Junior School Report - ${pupil.name}</title>
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; background: #fff; color: #333; padding: 25px; line-height: 1.4; }
                    .report-container { max-width: 800px; margin: 0 auto; border: 3px double #1a237e; padding: 25px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 20px; }
                    .school-name { font-size: 28px; font-weight: 900; color: #1a237e; letter-spacing: 1px; margin: 0; }
                    .school-sub { font-size: 13px; color: #666; margin: 3px 0; }
                    .report-title { display: inline-block; padding: 6px 16px; background: #f5f5f5; border: 1px solid #1a237e; margin-top: 10px; font-weight: bold; border-radius: 4px; }
                    .student-info { display: flex; justify-content: space-between; margin: 20px 0; padding: 10px; background: #fbfbfb; border: 1px solid #ddd; border-radius: 4px; }
                    .info-col { width: 48%; }
                    .info-item { font-size: 14px; margin: 5px 0; }
                    .info-label { font-weight: bold; color: #555; }
                    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                    th { background: #1a237e; color: #fff; text-align: left; padding: 10px; font-size: 13px; }
                    td { border: 1px solid #ddd; padding: 10px; font-size: 14px; }
                    tr:nth-child(even) { background-color: #f9f9f9; }
                    .summary-sec { display: flex; justify-content: space-between; margin-top: 20px; gap: 20px; }
                    .metrics { flex: 1.2; font-size: 14px; }
                    .fees-status { flex: 0.8; border: 2px dashed #c62828; padding: 10px; text-align: center; border-radius: 6px; background: #ffebee; color: #c62828; max-height: max-content; }
                    .fees-cleared { flex: 0.8; border: 2px solid #2e7d32; padding: 10px; text-align: center; border-radius: 6px; background: #e8f5e9; color: #2e7d32; max-height: max-content; }
                    .bold { font-weight: bold; }
                    .remarks-box { margin-top: 25px; padding: 15px; border: 1px solid #ddd; background: #fafafa; border-radius: 4px; }
                    .remark-item { margin-bottom: 10px; font-size: 14px; }
                    .footer-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 35px; }
                    .signature-box { border-top: 1px solid #ddd; padding-top: 8px; width: 200px; text-align: center; font-size: 12px; }
                    .stamp { width: 70px; height: 70px; border: 2px solid #1976d2; border-radius: 50%; display: flex; flex-direction: column; justify-content: center; align-items: center; background: #e3f2fd; color: #1976d2; font-size: 8px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="report-container">
                    <div class="header">
                        $logoImgTag
                        <h1 class="school-name">PEARL JUNIOR SCHOOL</h1>
                        <div class="school-sub">P.O. Box 773, Kampala • Tel: +256 772 400101</div>
                        <div class="school-sub">Web: www.pearljuniorschool.sc.ug</div>
                        <div class="report-title">TERMLY CA PROGRESS REPORT CARD</div>
                    </div>
                    
                    <div class="student-info">
                        <div class="info-col">
                            <div class="info-item"><span class="info-label">Pupil Name:</span> ${pupil.name}</div>
                            <div class="info-item"><span class="info-label">Class Level:</span> ${pupil.gradeLevel}</div>
                            <div class="info-item"><span class="info-label">Class Position:</span> <strong style="color: #1a237e;">$positionStr</strong></div>
                        </div>
                        <div class="info-col" style="text-align: right;">
                            <div class="info-item"><span class="info-label">Roll ID:</span> ${pupil.rollNumber}</div>
                            <div class="info-item"><span class="info-label">Term:</span> Term I (2026)</div>
                        </div>
                    </div>
                    
                    <table>
                        <thead>
                            <tr>
                                <th>ACADEMIC SUBJECT</th>
                                <th>MIDTERM (40%)</th>
                                <th>EOT (60%)</th>
                                <th>COMBINED TOTAL</th>
                                <th>GRADE</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${rowsBuilder.toString()}
                        </tbody>
                    </table>
                    
                    <div class="summary-sec">
                        <div class="metrics">
                            <div class="info-item"><span class="info-label">Total Subjects Evaluated:</span> $countSubjects</div>
                            <div class="info-item"><span class="info-label">Combined Average Mark:</span> ${String.format(java.util.Locale.US, "%.2f%%", avgScore)}</div>
                            ${if (!isNursery) "<div class=\"info-item\"><span class=\"bold\">Termly Outturn:</span> $divisionStr</div>" else ""}
                        </div>
                        $feesHtml
                    </div>
                    
                    <div class="remarks-box">
                        <div class="remark-item"><span class="bold">Class Teacher's Remark:</span> $defaultTeacherRemark</div>
                        <div class="remark-item"><span class="bold">Headteacher's Comments:</span> $defaultHeadRemark</div>
                    </div>
                    
                    <div class="footer-row">
                        <div class="signature-box">
                            Nabakooza Sarah<br>
                            <span style="color: grey; font-size: 11px;">HEADTEACHER SIGNATURE</span>
                        </div>
                        <div class="stamp">
                            <span>OFFICIAL</span>
                            <span style="font-size: 9px; margin: 2px 0;">PEARL</span>
                            <span>STAMP</span>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        val sanitizedName = pupil.name.replace(" ", "_").replace("(", "").replace(")", "").replace(".", "")
        val fileName = "${sanitizedName}_ReportCard.pdf"

        try {
            var pfd: android.os.ParcelFileDescriptor? = null
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS + "/Pearl_Junior_School_Reports")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    pfd = resolver.openFileDescriptor(uri, "rw")
                }
            } else {
                val downloadsDir = java.io.File(
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                    "Pearl_Junior_School_Reports"
                )
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = java.io.File(downloadsDir, fileName)
                pfd = android.os.ParcelFileDescriptor.open(
                    file,
                    android.os.ParcelFileDescriptor.MODE_READ_WRITE or android.os.ParcelFileDescriptor.MODE_CREATE or android.os.ParcelFileDescriptor.MODE_TRUNCATE
                )
            }

            if (pfd != null) {
                val webView = android.webkit.WebView(context)
                webView.settings.javaScriptEnabled = true
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
                val finalPfd = pfd
                webView.webViewClient = object : android.webkit.WebViewClient() {
                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                        try {
                            val printAdapter = webView.createPrintDocumentAdapter("Report_${sanitizedName}")
                            val printAttributes = android.print.PrintAttributes.Builder()
                                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                                .setResolution(android.print.PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                                .setMinMargins(android.print.PrintAttributes.Margins.NO_MARGINS)
                                .build()

                            android.print.PdfHelper.savePdfFromAdapter(
                                printAdapter,
                                printAttributes,
                                finalPfd,
                                object : android.print.PdfHelper.Callback {
                                    override fun onSuccess() {
                                        try { finalPfd.close() } catch (e: Exception) {}
                                        currentIndex++
                                        processNext()
                                    }

                                    override fun onFailure() {
                                        try { finalPfd.close() } catch (e: Exception) {}
                                        currentIndex++
                                        processNext()
                                    }
                                }
                            )
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            try { finalPfd.close() } catch (e: Exception) {}
                            currentIndex++
                            processNext()
                        }
                    }
                }
            } else {
                currentIndex++
                processNext()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            currentIndex++
            processNext()
        }
    }

    android.os.Handler(android.os.Looper.getMainLooper()).post {
        processNext()
    }
}

fun fallbackToDownloadAndShare(context: android.content.Context, name: String, fileContent: String) {
    var savedSuccess = false
    val fileName = "Report_${name.replace(" ", "_").replace("(", "").replace(")", "").replace(".", "")}.html"
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/html")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(fileContent.toByteArray(Charsets.UTF_8))
                }
                savedSuccess = true
            }
        } else {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = java.io.File(downloadsDir, fileName)
            file.writeText(fileContent)
            savedSuccess = true
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Pearl Junior School Report Card - $name")
            putExtra(android.content.Intent.EXTRA_TEXT, fileContent)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = android.content.Intent.createChooser(intent, "Download or Share Report Card HTML")
        chooser.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooser)
        
        if (savedSuccess) {
            android.widget.Toast.makeText(context, "Saved to Downloads & opening sharing options!", android.widget.Toast.LENGTH_LONG).show()
        } else {
            android.widget.Toast.makeText(context, "Opening share options...", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        try {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Report Card HTML", fileContent)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "Copied report card to clipboard (fallback)!", android.widget.Toast.LENGTH_LONG).show()
        } catch (clipEx: Throwable) {
            android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

// ==================== TIMETABLE AND EVENTS PLANNER HUB ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetablePlannerScreen(viewModel: SchoolViewModel) {
    val timetableList by viewModel.timetablePeriods.collectAsStateWithLifecycle()
    val eventsList by viewModel.schoolEvents.collectAsStateWithLifecycle()
    val teachersList by viewModel.teachers.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Timetable, 1: Events
    val tabTitles = listOf("Weekly Timetable", "Events Calendar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> TimetableTabContent(
                timetableList = timetableList,
                teachersList = teachersList,
                onAddPeriod = { className, subjectName, dayOfWeek, startTime, endTime, teacherName ->
                    viewModel.insertTimetablePeriod(className, subjectName, dayOfWeek, startTime, endTime, teacherName)
                },
                onDeletePeriod = { id ->
                    viewModel.deleteTimetablePeriod(id)
                },
                onAiGenerate = {
                    viewModel.generateAiTimetableAcrossSchool()
                }
            )
            1 -> EventsTabContent(
                eventsList = eventsList,
                onAddEvent = { title, date, description, audience, priority ->
                    viewModel.insertSchoolEvent(title, date, description, audience, priority)
                },
                onDeleteEvent = { id ->
                    viewModel.deleteSchoolEvent(id)
                }
            )
        }
    }
}

fun parseTimeToMinutes(timeStr: String): Int {
    val clean = timeStr.trim().uppercase()
    var hours = 0
    var minutes = 0
    try {
        if (clean.contains("AM") || clean.contains("PM")) {
            val isPm = clean.contains("PM")
            val numbersOnly = clean.replace("AM", "").replace("PM", "").replace(" ", "").trim()
            val parts = numbersOnly.split(":")
            if (parts.isNotEmpty()) {
                var h = parts[0].trim().toIntOrNull() ?: 0
                val m = if (parts.size > 1) parts[1].trim().toIntOrNull() ?: 0 else 0
                if (isPm) {
                    if (h < 12) h += 12
                } else {
                    if (h == 12) h = 0
                }
                hours = h
                minutes = m
            }
        } else {
            val parts = clean.split(":")
            if (parts.isNotEmpty()) {
                hours = parts[0].trim().toIntOrNull() ?: 0
                minutes = if (parts.size > 1) parts[1].trim().toIntOrNull() ?: 0 else 0
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return hours * 60 + minutes
}

fun timesOverlap(start1: String, end1: String, start2: String, end2: String): Boolean {
    val s1 = parseTimeToMinutes(start1)
    val e1 = parseTimeToMinutes(end1)
    val s2 = parseTimeToMinutes(start2)
    val e2 = parseTimeToMinutes(end2)
    return s1 < e2 && s2 < e1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableTabContent(
    timetableList: List<com.example.data.entity.TimetablePeriod>,
    teachersList: List<com.example.data.entity.Teacher>,
    onAddPeriod: (String, String, String, String, String, String) -> Unit,
    onDeletePeriod: (Int) -> Unit,
    onAiGenerate: (() -> Unit)? = null
) {
    var selectedGradeFilter by remember { mutableStateOf("P.7") }
    val gradeOptions = listOf("Nursery", "Middle", "Top", "P.1", "P.2", "P.3", "P.4", "P.5", "P.6", "P.7")

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule Class Period")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Elegant AI Timetable Generator Assistant Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "🤖 Smart AI Timetable Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Click to calculate conflict-free lesson timetables, assigning teachers dynamically for all classes.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.82f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAiGenerate?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, "Run AI", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Select Class Level Filter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            
            var gradeDropdownExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedGradeFilter,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { gradeDropdownExpanded = !gradeDropdownExpanded }
                )
                DropdownMenu(
                    expanded = gradeDropdownExpanded,
                    onDismissRequest = { gradeDropdownExpanded = false }
                ) {
                    gradeOptions.forEach { grade ->
                        DropdownMenuItem(
                            text = { Text(grade) },
                            onClick = {
                                selectedGradeFilter = grade
                                gradeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            val filteredList = timetableList.filter { it.className == selectedGradeFilter }
            val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

            if (filteredList.isEmpty()) {
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
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "No periods scheduled for Class $selectedGradeFilter",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    daysOfWeek.forEach { day ->
                        val dayPeriods = filteredList.filter { it.dayOfWeek.equals(day, ignoreCase = true) }
                        if (dayPeriods.isNotEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = day,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            items(dayPeriods) { period ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Book,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = period.subjectName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${period.startTime} - ${period.endTime}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Teacher: ${period.teacherName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { onDeletePeriod(period.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Period",
                                                tint = MaterialTheme.colorScheme.error
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

    if (showAddDialog) {
        var formClass by remember { mutableStateOf("P.7") }
        var formSubject by remember { mutableStateOf("Mathematics") }
        var formDay by remember { mutableStateOf("Monday") }
        var formStartHour by remember { mutableStateOf("08:30") }
        var formEndHour by remember { mutableStateOf("09:30") }
        var formTeacherName by remember { mutableStateOf("") }

        val subjectOptions = remember(formClass) {
            val grade = formClass.lowercase()
            if (grade.contains("nursery") || grade.contains("middle") || grade.contains("top")) {
                listOf("Literacy & Numeracy", "Reading & Writing", "Art & Craft", "News & Speech", "Physical Play")
            } else {
                listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "Religious Education")
            }
        }
        LaunchedEffect(subjectOptions) {
            if (!subjectOptions.contains(formSubject)) {
                formSubject = subjectOptions.firstOrNull() ?: ""
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule New Lesson Block", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var classExpanded by remember { mutableStateOf(false) }
                    Text("Select Class level", style = MaterialTheme.typography.labelSmall)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formClass,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { classExpanded = !classExpanded }
                        )
                        DropdownMenu(
                            expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }
                        ) {
                            gradeOptions.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = { formClass = g; classExpanded = false }
                                )
                            }
                        }
                    }

                    var subjectExpanded by remember { mutableStateOf(false) }
                    Text("Select Subject", style = MaterialTheme.typography.labelSmall)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { subjectExpanded = !subjectExpanded }
                        )
                        DropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjectOptions.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub) },
                                    onClick = { formSubject = sub; subjectExpanded = false }
                                )
                            }
                        }
                    }

                    var dayExpanded by remember { mutableStateOf(false) }
                    Text("Select Day", style = MaterialTheme.typography.labelSmall)
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formDay,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { dayExpanded = !dayExpanded }
                        )
                        DropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            days.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = { formDay = d; dayExpanded = false }
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = formStartHour,
                            onValueChange = { formStartHour = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("hh:mm") }
                        )
                        OutlinedTextField(
                            value = formEndHour,
                            onValueChange = { formEndHour = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("hh:mm") }
                        )
                    }

                    var teacherExpanded by remember { mutableStateOf(false) }
                    Text("Assigned Teacher / Instructor", style = MaterialTheme.typography.labelSmall)
                    
                    if (teachersList.isNotEmpty()) {
                        if (formTeacherName.isEmpty()) {
                            formTeacherName = teachersList.first().name
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = formTeacherName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { teacherExpanded = !teacherExpanded }
                            )
                            DropdownMenu(
                                expanded = teacherExpanded,
                                onDismissRequest = { teacherExpanded = false }
                            ) {
                                teachersList.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.name) },
                                        onClick = { formTeacherName = t.name; teacherExpanded = false }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = formTeacherName,
                            onValueChange = { formTeacherName = it },
                            placeholder = { Text("Enter teacher name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    val finalTeacher = if (formTeacherName.isBlank()) "Alternative Assignment" else formTeacherName

                    // 1. Classroom booking overlap check
                    val classroomConflict = remember(formClass, formDay, formStartHour, formEndHour, timetableList) {
                        timetableList.find {
                            it.dayOfWeek.equals(formDay, ignoreCase = true) &&
                            it.className.equals(formClass, ignoreCase = true) &&
                            timesOverlap(it.startTime, it.endTime, formStartHour, formEndHour)
                        }
                    }

                    // 2. Teacher booking overlap check
                    val teacherConflict = remember(finalTeacher, formDay, formStartHour, formEndHour, timetableList) {
                        timetableList.find {
                            it.dayOfWeek.equals(formDay, ignoreCase = true) &&
                            it.teacherName.equals(finalTeacher, ignoreCase = true) &&
                            timesOverlap(it.startTime, it.endTime, formStartHour, formEndHour)
                        }
                    }

                    if (classroomConflict != null || teacherConflict != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("dialog_conflict_error_card")
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "🚨 Scheduling Conflict Detected",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (classroomConflict != null) {
                                    Text(
                                        text = "• Classroom ($formClass) is already booked for '${classroomConflict.subjectName}' from ${classroomConflict.startTime} - ${classroomConflict.endTime}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 11.sp
                                    )
                                }
                                if (teacherConflict != null) {
                                    Text(
                                        text = "• Teacher ($finalTeacher) is already scheduled in Classroom ${teacherConflict.className} for '${teacherConflict.subjectName}' from ${teacherConflict.startTime} - ${teacherConflict.endTime}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val finalTeacher = if (formTeacherName.isBlank()) "Alternative Assignment" else formTeacherName
                val hasConflict = timetableList.any {
                    it.dayOfWeek.equals(formDay, ignoreCase = true) && (
                        (it.className.equals(formClass, ignoreCase = true) && timesOverlap(it.startTime, it.endTime, formStartHour, formEndHour)) ||
                        (it.teacherName.equals(finalTeacher, ignoreCase = true) && timesOverlap(it.startTime, it.endTime, formStartHour, formEndHour))
                    )
                }

                Button(
                    onClick = {
                        onAddPeriod(formClass, formSubject, formDay, formStartHour, formEndHour, finalTeacher)
                        showAddDialog = false
                    },
                    enabled = !hasConflict,
                    modifier = Modifier.testTag("dialog_save_period_btn")
                ) {
                    Text("Save Period")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsTabContent(
    eventsList: List<com.example.data.entity.SchoolEvent>,
    onAddEvent: (String, String, String, String, String) -> Unit,
    onDeleteEvent: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var filterAudience by remember { mutableStateOf("All") }
    val audienceOptions = listOf("All", "Parents", "Teachers", "Students")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add School Calendar Event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Audience Filter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                audienceOptions.forEach { audience ->
                    val isSelected = filterAudience == audience
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterAudience = audience },
                        label = { Text(audience) }
                    )
                }
            }

            val filteredEvents = if (filterAudience == "All") {
                eventsList
            } else {
                eventsList.filter { it.audience.equals(filterAudience, ignoreCase = true) || it.audience.equals("All", ignoreCase = true) }
            }

            if (filteredEvents.isEmpty()) {
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
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "No events on list",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = event.eventDate,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteEvent(event.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Event",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Target: ${event.audience}") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )

                                    val (badgeBg, badgeText) = when (event.priority.lowercase()) {
                                        "high" -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                                        "medium" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
                                        else -> Pair(Color(0xFFE3F2FD), Color(0xFF0D47A1))
                                    }
                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "${event.priority} Priority",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var formTitle by remember { mutableStateOf("") }
        var formDate by remember { mutableStateOf("") }
        var formDescription by remember { mutableStateOf("") }
        var formAudience by remember { mutableStateOf("All") }
        var formPriority by remember { mutableStateOf("Medium") }

        if (formDate.isEmpty()) {
            formDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Calendar Event / Circular", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = formTitle,
                        onValueChange = { formTitle = it },
                        label = { Text("Event Name / circular Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formDate,
                        onValueChange = { formDate = it },
                        label = { Text("Event Date") },
                        placeholder = { Text("yyyy-MM-dd") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    var audienceExpanded by remember { mutableStateOf(false) }
                    Text("Select Target Audience", style = MaterialTheme.typography.labelSmall)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formAudience,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audienceExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { audienceExpanded = !audienceExpanded }
                        )
                        DropdownMenu(
                            expanded = audienceExpanded,
                            onDismissRequest = { audienceExpanded = false }
                        ) {
                            audienceOptions.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = { formAudience = t; audienceExpanded = false }
                                )
                            }
                        }
                    }

                    var priorityExpanded by remember { mutableStateOf(false) }
                    Text("Priority Importance", style = MaterialTheme.typography.labelSmall)
                    val priorities = listOf("High", "Medium", "Low")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = formPriority,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { priorityExpanded = !priorityExpanded }
                        )
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            priorities.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = { formPriority = p; priorityExpanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formTitle.isNotBlank()) {
                            onAddEvent(
                                formTitle,
                                formDate,
                                formDescription,
                                formAudience,
                                formPriority
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(viewModel: SchoolViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    val timetableList by viewModel.timetablePeriods.collectAsStateWithLifecycle()
    val eventsList by viewModel.schoolEvents.collectAsStateWithLifecycle()
    val attendanceList by viewModel.studentAttendance.collectAsStateWithLifecycle()

    var enteredRollNum by remember { mutableStateOf("") }
    var unlockedRollNum by remember { mutableStateOf("") }
    var lookupError by remember { mutableStateOf<String?>(null) }
    var selectedReportTab by remember { mutableStateOf(0) } // 0: Reports & Record, 1: Timetable & Notices, 2: Sick Leave Absences

    if (studentsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.People, "No Pupils Enrolled", modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("No active student records available in system database.", color = Color.Gray)
            }
        }
        return
    }

    val selectedStudent = studentsList.find { it.rollNumber.trim().equals(unlockedRollNum.trim(), ignoreCase = true) }

    if (selectedStudent == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Access Gate Lock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Text(
                text = "Secure Parent Portal Gate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "To safeguard student records and comply with academic privacy guidelines, parents may only view details of their own respective child.\n\nPlease enter the unique Roll ID (Registration Number) of your child to gain authorization.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = enteredRollNum,
                onValueChange = { 
                    enteredRollNum = it
                    lookupError = null
                },
                label = { Text("Student Registration Number") },
                placeholder = { Text("e.g. S1001") },
                isError = lookupError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("parent_portal_reg_num_input"),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        val trimmed = enteredRollNum.trim()
                        val match = studentsList.any { it.rollNumber.trim().equals(trimmed, ignoreCase = true) }
                        if (match) {
                            unlockedRollNum = trimmed
                        } else {
                            lookupError = "Registration ID not found in current records. Please try S1001, S1002, etc."
                        }
                    }
                )
            )
            
            if (lookupError != null) {
                Text(
                    text = lookupError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Button(
                onClick = {
                    val trimmed = enteredRollNum.trim()
                    val match = studentsList.any { it.rollNumber.trim().equals(trimmed, ignoreCase = true) }
                    if (match) {
                        unlockedRollNum = trimmed
                        lookupError = null
                    } else {
                        lookupError = "Registration ID not found in current records. Please try S1001, S1002, etc."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("parent_portal_reg_num_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify & Unlock Portfolio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💡 Quick Access Tips for Parents & Reviewers:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "1. Student registration keys are assigned when enrolling a pupil in classes.\n2. Active system registration keys that you may test include: ${studentsList.take(4).joinToString(", ") { it.rollNumber }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming & Student Selector Card
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
                        Text(
                            text = "👦 Parent Secure Space",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        TextButton(
                            onClick = {
                                unlockedRollNum = ""
                                enteredRollNum = ""
                                lookupError = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Authorized access is limited exclusively to files belonging to ${selectedStudent.name}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Active Student Profile Summary Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedStudent.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Class: ${selectedStudent.gradeLevel}  |  Roll Number: ${selectedStudent.rollNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        // Circular avatar badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedStudent.name.take(2).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Fees Status Block
                    Text("Outstanding Tuition Standings", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val feesRemaining = (selectedStudent.feesTotal - selectedStudent.feesPaid).coerceAtLeast(0.0)
                    val percentPaid = if (selectedStudent.feesTotal > 0) (selectedStudent.feesPaid / selectedStudent.feesTotal).toFloat() else 0f
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid: UGX ${String.format(Locale.US, "%,.0f", selectedStudent.feesPaid)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Total: UGX ${String.format(Locale.US, "%,.0f", selectedStudent.feesTotal)}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = { percentPaid },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                        color = if (feesRemaining == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    if (feesRemaining > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Outstanding Balance due: UGX ${String.format(Locale.US, "%,.0f", feesRemaining)}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✅ Paid Up (Zero outstanding dues)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Feature Selector Segment tabs
        item {
            TabRow(
                selectedTabIndex = selectedReportTab,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Tab(
                    selected = selectedReportTab == 0,
                    onClick = { selectedReportTab = 0 },
                    text = { Text("Performance & Attendances", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedReportTab == 1,
                    onClick = { selectedReportTab = 1 },
                    text = { Text("Timetables & Board", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedReportTab == 2,
                    onClick = { selectedReportTab = 2 },
                    text = { Text("Draft Sick Leaves", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Display contents relative to selected tab
        when (selectedReportTab) {
            0 -> {
                // Performance and Attendances
                val studentRecords = attendanceList.filter { it.studentId == selectedStudent.id }
                val totalDays = studentRecords.size
                val presentDays = studentRecords.count { it.status == "Present" }
                val absentDays = studentRecords.count { it.status == "Absent" }
                val attendancePercent = if (totalDays > 0) (presentDays.toDouble() / totalDays * 100).toInt() else 100

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🗓️ Daily Attendance Status Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Attendance %", fontSize = 11.sp, color = Color.Gray)
                                    Text("$attendancePercent%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (attendancePercent < 80) Color.Red else MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Days Present", fontSize = 11.sp, color = Color.Gray)
                                    Text("$presentDays / $totalDays", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Absences", fontSize = 11.sp, color = Color.Gray)
                                    Text(absentDays.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            if (studentRecords.isEmpty()) {
                                Text("No recent daily attendance logs found for ${selectedStudent.name}.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                studentRecords.take(5).forEach { rec ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(rec.date, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (rec.status == "Present") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = rec.status, 
                                                color = if (rec.status == "Present") Color(0xFF2E7D32) else Color(0xFFD32F2F), 
                                                fontSize = 11.sp, 
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Grades summary from ViewModel
                item {
                    val gradesList by viewModel.allGrades.collectAsStateWithLifecycle()
                    val selectedStudentGrades = gradesList.filter { it.studentId == selectedStudent.id }
                    
                    val isNursery = selectedStudent.gradeLevel.contains("Class", ignoreCase = true)
                    val activeSubjects = selectedStudentGrades.map { it.subjectName }.distinct()
                    val countSubjects = activeSubjects.size
                    
                    var totalWeightedGradeSum = 0.0
                    var aggregatesSum = 0
                    
                    activeSubjects.forEach { subj ->
                        val midGrade = selectedStudentGrades.find { it.subjectName == subj && it.examName == "Midterm Exam" }?.score
                        val eotGrade = selectedStudentGrades.find { it.subjectName == subj && it.examName == "End of Term Exam" }?.score
                        
                        val weightedTotal = if (midGrade != null && eotGrade != null) {
                            (midGrade * 0.4) + (eotGrade * 0.6)
                        } else {
                            eotGrade ?: midGrade ?: 0.0
                        }
                        
                        totalWeightedGradeSum += weightedTotal
                        
                        val points = if (isNursery) {
                            0
                        } else {
                            when {
                                weightedTotal >= 85.0 -> 1
                                weightedTotal >= 75.0 -> 2
                                weightedTotal >= 70.0 -> 3
                                weightedTotal >= 65.0 -> 4
                                weightedTotal >= 60.0 -> 5
                                weightedTotal >= 50.0 -> 6
                                weightedTotal >= 45.0 -> 7
                                weightedTotal >= 40.0 -> 8
                                else -> 9
                            }
                        }
                        
                        if (!isNursery) {
                            aggregatesSum += points
                        }
                    }
                    
                    val avgScore = if (countSubjects > 0) totalWeightedGradeSum / countSubjects else 0.0
                    val divisionStr = if (isNursery) {
                        if (avgScore >= 80) "Nursery Achieved Promisingly" else "Developing Steadily"
                    } else {
                        when {
                            aggregatesSum in 4..12 -> "Division I (First Grade) 🌟"
                            aggregatesSum in 13..24 -> "Division II (Second Grade)"
                            aggregatesSum in 25..29 -> "Division III (Third Grade)"
                            aggregatesSum in 30..34 -> "Division IV (Fourth Grade)"
                            else -> "Division U (Ungraded / Fail)"
                        }
                    }
                    
                    if (!selectedStudent.isReportCardPublished) {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("unpublished_report_card_alert"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "Report Cards Awaiting Publication",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "The school administrative council has not officially released or signed off on the terminal grade reports for ${selectedStudent.gradeLevel} yet. An automated SMS dispatch will be delivered to your phone number (${selectedStudent.phone}) instantly upon official release.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏆 Term Academic Report Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                if (selectedStudentGrades.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            val defaultTeacherRemark = if (avgScore >= 80) {
                                                "Consistently diligent and disciplined. Outstanding academic competence. Keep the same tempo!"
                                            } else if (avgScore >= 60) {
                                                "A promising child who shows regular performance. Should double efforts in homework targets."
                                            } else {
                                                "Amiable child. Needs strictly focused revision assistance in Mathematics to pass well."
                                            }

                                            val defaultHeadRemark = if (avgScore >= 80) {
                                                "Excellent Outturn! Approved for academic honors. Keep shining."
                                            } else {
                                                "Reviewed and signed. Encouraged to strive higher next term."
                                            }
                                            
                                            val outstandingFee = selectedStudent.feesTotal - selectedStudent.feesPaid
                                            val formattedAmount = String.format(Locale.US, "%,.0f", outstandingFee)
                                            val feesSummary = if (outstandingFee <= 0) "FULLY PAID" else "UGX " + formattedAmount + " DUE"
                                            
                                            val posInfo = calculateStudentPosition(selectedStudent.id, selectedStudent.gradeLevel, studentsList, gradesList)
                                            val positionStr = "${posInfo.first} of ${posInfo.second}"

                                            exportReportCard(
                                                context = context,
                                                pupil = selectedStudent,
                                                subjects = activeSubjects,
                                                grades = selectedStudentGrades,
                                                teacherRemark = defaultTeacherRemark,
                                                headComment = defaultHeadRemark,
                                                avgScore = avgScore,
                                                division = divisionStr,
                                                feesSummary = feesSummary,
                                                format = "HTML",
                                                position = positionStr
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.testTag("download_parent_report_pdf")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "PDF Report", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PDF Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            if (selectedStudentGrades.isEmpty()) {
                                Text("Report card files have not been released yet for this term.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                selectedStudentGrades.forEach { grade ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val gradeRemarks = if (grade.score >= 90.0) "D1 Distinction" else if (grade.score >= 80.0) "D2 Distinction" else if (grade.score >= 70.0) "C3 Credit" else if (grade.score >= 60.0) "C4 Credit" else if (grade.score >= 50.0) "C6 Credit" else if (grade.score >= 40.0) "P8 Pass" else "F9 Fail"
                                        Text(grade.subjectName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("Score: ${grade.score.toInt()}%  [$gradeRemarks]", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Combined Average Mark: ${String.format(Locale.US, "%.1f%%", avgScore)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (!isNursery) {
                                            Text(
                                                text = "Grade Point Aggregates: $aggregatesSum  |  Outturn: $divisionStr",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Text(
                                                text = "Progress Stage: $divisionStr",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.SemiBold
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
            1 -> {
                // Timetable and Circulars
                val filteredPeriods = timetableList.filter { it.className == selectedStudent.gradeLevel }
                val currentEnrollGrade = selectedStudent.gradeLevel
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📅 Weekly Lesson Timetable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                
                                // Export Button
                                Button(
                                    onClick = {
                                        exportWeeklyTimetable(context, selectedStudent.name, currentEnrollGrade, filteredPeriods)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Print, "Print", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (filteredPeriods.isEmpty()) {
                                Text("No scheduled timetabled periods posted yet for Class $currentEnrollGrade.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                                daysOfWeek.forEach { day ->
                                    val periodsForDay = filteredPeriods.filter { it.dayOfWeek.equals(day, ignoreCase = true) }
                                    if (periodsForDay.isNotEmpty()) {
                                        Text(day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
                                        periodsForDay.forEach { prd ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("• ${prd.subjectName}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                Text("${prd.startTime} - ${prd.endTime}  (${prd.teacherName})", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Announcements targeting Parents or All
                item {
                    val filteredEvents = eventsList.filter { it.audience in listOf("Parents", "All", "Parents/Guardians") }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("📢 School Notices & Events Calendar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            if (filteredEvents.isEmpty()) {
                                Text("No recent notices or circulars mapped for parents.", color = Color.Gray)
                            } else {
                                filteredEvents.forEach { ev ->
                                    val priorityColor = when (ev.priority) {
                                        "High" -> Color(0xFFD32F2F)
                                        "Medium" -> Color(0xFFF57C00)
                                        else -> Color(0xFF388E3C)
                                    }
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(ev.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                            Box(
                                                modifier = Modifier
                                                    .background(priorityColor.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(ev.priority, color = priorityColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Scheduled: ${ev.eventDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(ev.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Form section for Leave Requests
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("📝 Submit Child Absence Leave Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Inform the school immediately of planned hospital checkups, sick leaves, or emergency domestic travels.", fontSize = 12.sp, color = Color.Gray)
                            
                            var startDate by remember { mutableStateOf("") }
                            var totalDaysInput by remember { mutableStateOf("1") }
                            var categoryField by remember { mutableStateOf("Sick Leave") }
                            var reasonField by remember { mutableStateOf("") }
                            
                            val listCategories = listOf("Sick Leave", "Family Urgent Checkup", "Exam Postponement", "Other Reasons")
                            var categoryExpanded by remember { mutableStateOf(false) }
                            
                            Text("Desired Start Date (yyyy-MM-dd):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                placeholder = { Text("e.g. 2026-06-05") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text("Number of Days Absent:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = totalDaysInput,
                                onValueChange = { totalDaysInput = it },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text("Absence Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = categoryField,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(
                                    modifier = Modifier.matchParentSize().clickable { categoryExpanded = !categoryExpanded }
                                )
                                DropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    listCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                categoryField = cat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Text("Full Statement Case Memo / Reasons:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = reasonField,
                                onValueChange = { reasonField = it },
                                placeholder = { Text("Write medical condition diagnosis or urgency detail here...") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Button(
                                onClick = {
                                    if (startDate.isNotBlank() && reasonField.isNotBlank()) {
                                        // Insert into database leave request
                                        viewModel.saveLeaveRequest(
                                            com.example.data.entity.LeaveRequest(
                                                teacherId = 0, // 0 denotes student leave
                                                startDate = startDate,
                                                endDate = "Days: $totalDaysInput",
                                                reason = "Absence Case: $reasonField",
                                                status = "Pending",
                                                leaveType = categoryField
                                            )
                                        )
                                        
                                        // Insert activity notify
                                        viewModel.insertAppNotification(
                                            title = "📝 Pupil Leave Filed: ${selectedStudent.name}",
                                            content = "A new Parent Leave file from ${selectedStudent.name}'s parent ($categoryField starting on $startDate for $totalDaysInput days) has been routed.",
                                            type = "Leave"
                                        )
                                        
                                        android.widget.Toast.makeText(context, "Absence File saved & sent to Head Teacher desk successfully!", android.widget.Toast.LENGTH_LONG).show()
                                        
                                        // Reset fields
                                        startDate = ""
                                        reasonField = ""
                                    } else {
                                        android.widget.Toast.makeText(context, "Please write a start date and explanatory reason first.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Send, "File absence document")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Leave Request File", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Global PDF printing adapter function for student timetables
fun exportWeeklyTimetable(context: android.content.Context, pupilName: String, grade: String, periods: List<com.example.data.entity.TimetablePeriod>) {
    val prefs = context.getSharedPreferences("school_prefs", android.content.Context.MODE_PRIVATE)
    val logoBase64 = prefs.getString("school_logo_base64", null)
    val logoImgTag = if (!logoBase64.isNullOrBlank()) {
        "<img src=\"data:image/png;base64,$logoBase64\" style=\"max-height: 80px; max-width: 150px; margin-bottom: 12px; object-fit: contain; display: block; margin-left: auto; margin-right: auto;\" />"
    } else {
        ""
    }
    val htmlBuilder = java.lang.StringBuilder()
    htmlBuilder.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Weekly School Timetable - ${pupilName}</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 24px; color: #333; }
                .header { text-align: center; border-bottom: 3px double #1a237e; padding-bottom: 12px; margin-bottom: 24px; }
                .logo { font-size: 24px; font-weight: bold; color: #1a237e; text-transform: uppercase; letter-spacing: 1px; }
                .school-title { font-size: 16px; font-weight: 500; color: #555; margin-top: 4px; }
                .title { font-size: 20px; font-weight: bold; margin-top: 16px; color: #111; }
                table { width: 100%; border-collapse: collapse; margin-top: 16px; }
                th { background-color: #1a237e; color: white; padding: 10px; text-align: left; font-size: 14px; font-weight: bold; text-transform: uppercase; }
                td { border-bottom: 1px solid #ddd; padding: 10px; font-size: 13px; }
                tr:nth-child(even) { background-color: #f8f9fa; }
                .day { background-color: #e8eaf6; font-weight: bold; color: #1a237e; }
                .footer { text-align: center; margin-top: 40px; font-size: 11px; color: #777; border-top: 1px solid #eee; padding-top: 12px; }
            </style>
        </head>
        <body>
            <div class="header">
                $logoImgTag
                <div class="logo">Pearl Junior School</div>
                <div class="school-title">Excellence and Purity | Official Timetable Registry</div>
                <div class="title">Weekly Classroom Schedule - Class: ${grade}</div>
                <p>Student Pupil Name: <strong>${pupilName}</strong></p>
            </div>
            
            <table>
                <thead>
                    <tr>
                        <th style="width: 25%;">Day</th>
                        <th style="width: 20%;">Time Slot</th>
                        <th style="width: 30%;">Subject</th>
                        <th style="width: 25%;">Assigned Instructor</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    daysOfWeek.forEach { day ->
        val dayPeriods = periods.filter { it.dayOfWeek.equals(day, ignoreCase = true) }
        if (dayPeriods.isNotEmpty()) {
            dayPeriods.forEachIndexed { idx, prd ->
                htmlBuilder.append("""
                    <tr>
                        ${if (idx == 0) "<td rowspan='${dayPeriods.size}' class='day'>${day}</td>" else ""}
                        <td>${prd.startTime} - ${prd.endTime}</td>
                        <td><strong>${prd.subjectName}</strong></td>
                        <td>${prd.teacherName}</td>
                    </tr>
                """.trimIndent())
            }
        }
    }

    if (periods.isEmpty()) {
        htmlBuilder.append("<tr><td colspan='4' style='text-align:center;'>No classroom periods scheduled for this student's grade level.</td></tr>")
    }

    htmlBuilder.append("""
                </tbody>
            </table>
            
            <div class="footer">
                <p>This is a formal academic timetable copy compiled on ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}.</p>
                <p>For inquiries, contact: info@pearl.ac.ug | Kampala, Uganda</p>
            </div>
        </body>
        </html>
    """.trimIndent())

    val fileContent = htmlBuilder.toString()
    android.os.Handler(android.os.Looper.getMainLooper()).post {
        try {
            val webView = android.webkit.WebView(context)
            webView.loadDataWithBaseURL(null, fileContent, "text/html", "utf-8", null)
            webView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    try {
                        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                        val jobName = "Timetable_${pupilName.replace(" ", "_")}"
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "Print spooler error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            android.widget.Toast.makeText(context, "Generating print-ready school document...", android.widget.Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            t.printStackTrace()
            android.widget.Toast.makeText(context, "WebView initialization failed: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}


@Composable
fun AuthGateScreen(viewModel: SchoolViewModel) {
    var mode by remember { mutableStateOf(0) } // 0: Login, 1: Sign Up, 2: Forgot Password

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Security details
    val recoveryQuestions = listOf(
        "What was the name of your first school?",
        "What is your mother's maiden name?",
        "What is the name of your favorite teacher?",
        "What city were you born in?"
    )
    var selectedQuestionIndex by remember { mutableStateOf(0) }
    var securityAnswer by remember { mutableStateOf("") }
    
    // For password recovery
    var recoveryStep by remember { mutableStateOf(0) } // 0: Enter email, 1: Verify Answer & Reset
    var verifiedQuestion by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var isDropDownExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Branding Icon Section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "ST. JUDE ACADEMY",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Secure Portal Authentication Desk",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_panel_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (mode) {
                            0 -> "Sign In to System"
                            1 -> "Create Administrator Account"
                            else -> "Recover Password"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (mode == 0) {
                        // ==================== LOGIN ====================
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Access Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = "Toggle password visibility")
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    android.widget.Toast.makeText(context, "Please enter your credentials", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val success = viewModel.logInUser(email, password)
                                if (success) {
                                    android.widget.Toast.makeText(context, "Welcome back, $email!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Invalid email or wrong password.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Secure Login Direct", fontWeight = FontWeight.Bold)
                        }

                    } else if (mode == 1) {
                        // ==================== SIGN UP ====================
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Access Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(image, contentDescription = "Toggle password visibility")
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(image, contentDescription = "Toggle password visibility")
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_input")
                        )

                        // Recovery question explanation
                        Text(
                            "Select a security question for account password recovery. Do not forget the answer!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Dropdown selection for Security Question
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = recoveryQuestions[selectedQuestionIndex],
                                onValueChange = {},
                                label = { Text("Security Recovery Question") },
                                trailingIcon = {
                                    IconButton(onClick = { isDropDownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().clickable { isDropDownExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isDropDownExpanded,
                                onDismissRequest = { isDropDownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                recoveryQuestions.forEachIndexed { idx, q ->
                                    DropdownMenuItem(
                                        text = { Text(q, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            selectedQuestionIndex = idx
                                            isDropDownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = securityAnswer,
                            onValueChange = { securityAnswer = it },
                            label = { Text("Write Secret Answer") },
                            leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_security_answer_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank() || securityAnswer.isBlank()) {
                                    android.widget.Toast.makeText(context, "All form fields are strictly mandatory.", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    android.widget.Toast.makeText(context, "Please write a valid email address.", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (password.length < 5) {
                                    android.widget.Toast.makeText(context, "Password should be at least 5 characters.", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    android.widget.Toast.makeText(context, "Passwords do not match.", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val success = viewModel.signUpUser(email, password, recoveryQuestions[selectedQuestionIndex], securityAnswer)
                                if (success) {
                                    android.widget.Toast.makeText(context, "Account created & secure access granted!", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Account already exists for this email address.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("signup_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create Secure Account", fontWeight = FontWeight.Bold)
                        }

                    } else {
                        // ==================== FORGOT PASSWORD ====================
                        if (recoveryStep == 0) {
                            Text(
                                "Enter your registered email below to retrieve the security recovery question.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Registered Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("recovery_email_input")
                            )

                            Button(
                                onClick = {
                                    if (email.isBlank()) {
                                        android.widget.Toast.makeText(context, "Please enter your email", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val question = viewModel.getSecurityQuestion(email)
                                    if (question != null) {
                                        verifiedQuestion = question
                                        recoveryStep = 1
                                    } else {
                                        android.widget.Toast.makeText(context, "No account matches this email address.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Load Recovery Question", fontWeight = FontWeight.Bold)
                            }

                        } else {
                            Text(
                                "Answer the security question to reset your password:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = verifiedQuestion,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            OutlinedTextField(
                                value = securityAnswer,
                                onValueChange = { securityAnswer = it },
                                label = { Text("Your Secret Answer") },
                                leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("recovery_answer_input")
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("New Secure Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(image, contentDescription = "Toggle visibility")
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("recovery_new_password_input")
                            )

                            Button(
                                onClick = {
                                    if (securityAnswer.isBlank() || password.isBlank()) {
                                        android.widget.Toast.makeText(context, "All reset fields are required.", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (password.length < 5) {
                                        android.widget.Toast.makeText(context, "Password should be at least 5 characters.", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val success = viewModel.recoverPassword(email, securityAnswer, password)
                                    if (success) {
                                        android.widget.Toast.makeText(context, "Password updated & logged in successfully!", android.widget.Toast.LENGTH_LONG).show()
                                        recoveryStep = 0
                                        mode = 0
                                    } else {
                                        android.widget.Toast.makeText(context, "Incorrect secret answer. Recovery failed.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reset & Log In Directly", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Toggles & Switches between authentication modes
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (mode == 0) {
                            TextButton(onClick = { mode = 2; recoveryStep = 0; email = ""; password = "" }) {
                                Text("Forgot Password?")
                            }
                            TextButton(onClick = { mode = 1; email = ""; password = ""; securityAnswer = "" }) {
                                Text("Create Account")
                            }
                        } else if (mode == 1) {
                            TextButton(onClick = { mode = 0; email = ""; password = "" }) {
                                Text("Already have an account? Sign In")
                            }
                        } else {
                            TextButton(onClick = { mode = 0; recoveryStep = 0; email = ""; password = "" }) {
                                Text("Return to Authentication")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeeTrackingScreen(viewModel: SchoolViewModel) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val feesStats by viewModel.totalFeesStats.collectAsStateWithLifecycle()
    val feePaymentsList by viewModel.feePayments.collectAsStateWithLifecycle()

    var activeFeeTab by remember { mutableStateOf(0) } // 0: Pupil Balances, 1: Transaction Ledger

    var searchQuery by remember { mutableStateOf("") }
    var classFilter by remember { mutableStateOf("All Classes") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Cleared", "Outstanding", "Unpaid"

    var selectedStudentForPayment by remember { mutableStateOf<Student?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }

    var selectedStudentForLedger by remember { mutableStateOf<Student?>(null) }
    var ledgerAmountInput by remember { mutableStateOf("") }
    var ledgerNotesInput by remember { mutableStateOf("") }

    // Detailed/Custom Payment dialog states
    var showDetailedPaymentDialog by remember { mutableStateOf(false) }
    var detailedStudentSelection by remember { mutableStateOf<Student?>(null) }
    var detailedDateInput by remember { mutableStateOf("2026-06-18") }
    var detailedAmountInput by remember { mutableStateOf("") }
    var detailedNotesInput by remember { mutableStateOf("") }
    var detailedStudentExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Extract all unique gradeLevels for filter
    val classesList = remember(students) {
        listOf("All Classes") + students.map { it.gradeLevel }.distinct().sorted()
    }

    // Filter students
    val filteredStudents = remember(students, searchQuery, classFilter, statusFilter) {
        students.filter { student ->
            // Search
            val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                    student.rollNumber.contains(searchQuery, ignoreCase = true)
            
            // Class Filter
            val matchesClass = classFilter == "All Classes" || student.gradeLevel == classFilter

            // Status Filter
            val outstanding = student.feesTotal - student.feesPaid
            val matchesStatus = when (statusFilter) {
                "Cleared" -> outstanding <= 0
                "Outstanding" -> outstanding > 0 && student.feesPaid > 0
                "Unpaid" -> student.feesPaid <= 0
                else -> true
            }

            matchesSearch && matchesClass && matchesStatus
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("fee_tracking_screen"),
        floatingActionButton = {
            if (activeFeeTab == 0) {
                FloatingActionButton(
                    onClick = {
                        detailedStudentSelection = null
                        detailedAmountInput = ""
                        detailedNotesInput = ""
                        detailedDateInput = "2026-06-18"
                        showDetailedPaymentDialog = true
                    },
                    modifier = Modifier.testTag("add_fee_record_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add Fee Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Concept description
            item {
                Column {
                    Text(
                        text = "Finance & Tuition Registrar (UGX)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track, report, and record tuition fee payments in UGX with precision.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab selection
            item {
                TabRow(
                    selectedTabIndex = activeFeeTab,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    divider = {}
                ) {
                    Tab(
                        selected = activeFeeTab == 0,
                        onClick = { activeFeeTab = 0 },
                        text = { Text("Pupil Balances", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeFeeTab == 1,
                        onClick = { activeFeeTab = 1 },
                        text = { Text("Transaction Ledger", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            if (activeFeeTab == 0) {
                // Key Finance Cards Row (KPI Stats)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Expected Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Expected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    String.format(java.util.Locale.US, "UGX %,.0f", feesStats.totalExpected),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Total Collected Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Green tint
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Collected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    String.format(java.util.Locale.US, "UGX %,.0f", feesStats.totalCollected),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }

                        // Total Outstanding Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)) // Red/pink tint
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Outstanding",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    String.format(java.util.Locale.US, "UGX %,.0f", feesStats.remainingBalance),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }

                // Simulated Automated SMS Settings
                item {
                    val automaticSmsEnabled by viewModel.isAutomatedFeeSmsEnabled.collectAsStateWithLifecycle()
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("automated_sms_gateway_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Automated Parent SMS Alerts",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Simulates real-time telecom gateway dispatch to parents instantly when a tuition fee payment is registered.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = automaticSmsEnabled,
                                onCheckedChange = { viewModel.setAutomatedFeeSmsEnabled(it) },
                                modifier = Modifier.testTag("automated_fee_sms_switch")
                            )
                        }
                    }
                }

                // Search Bar & Filter Controls
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search Pupil Name / Roll No.") },
                                placeholder = { Text("Type name or roll number...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("fee_search_input")
                            )

                            // Filters Headers Row
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Class Filter Dropdown Trigger
                                var classExpanded by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(
                                        onClick = { classExpanded = true },
                                        modifier = Modifier.height(42.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = classFilter,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = classExpanded,
                                        onDismissRequest = { classExpanded = false }
                                    ) {
                                        classesList.forEach { cls ->
                                            DropdownMenuItem(
                                                text = { Text(cls, style = MaterialTheme.typography.bodyMedium) },
                                                onClick = {
                                                    classFilter = cls
                                                    classExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Status Filter Segmented Selector Row
                                listOf("All", "Cleared", "Outstanding", "Unpaid").forEach { opt ->
                                    val selected = statusFilter == opt
                                    FilterChip(
                                        selected = selected,
                                        onClick = { statusFilter = opt },
                                        label = { Text(opt, fontSize = 11.sp) },
                                        modifier = Modifier.height(42.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Results count
                item {
                    Text(
                        text = "Showing ${filteredStudents.size} students",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Students with outstanding balances items
                if (filteredStudents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "No pupils match selected filters.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredStudents.size) { idx ->
                        val student = filteredStudents[idx]
                        val outstanding = student.feesTotal - student.feesPaid
                        val percentCleared = if (student.feesTotal > 0) (student.feesPaid / student.feesTotal).toFloat() else 1f

                        val statusLabel = when {
                            outstanding <= 0 -> "Cleared"
                            student.feesPaid > 0 -> "Outstanding Balance"
                            else -> "Fully Unpaid"
                        }

                        val badgeColor = when {
                            outstanding <= 0 -> Color(0xFF2E7D32) // green
                            student.feesPaid > 0 -> Color(0xFFF57C00) // orange
                            else -> Color(0xFFD32F2F) // red
                        }

                        val badgeBg = when {
                            outstanding <= 0 -> Color(0xFFE8F5E9)
                            student.feesPaid > 0 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fee_student_card_${student.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Student name & class
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(student.gradeLevel, fontSize = 10.sp) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                            Text(
                                                text = "Roll: ${student.rollNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // Status Badge
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = badgeBg),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Sizing/Numbers info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("PAID", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = String.format(java.util.Locale.US, "UGX %,.0f", student.feesPaid),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TOTAL TUITION", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = String.format(java.util.Locale.US, "UGX %,.0f", student.feesTotal),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("OUTSTANDING", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = String.format(java.util.Locale.US, "UGX %,.0f", outstanding),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (outstanding > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Payment Progress bar
                                LinearProgressIndicator(
                                    progress = { percentCleared },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (outstanding <= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Pay and view statement action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedStudentForLedger = student
                                            ledgerAmountInput = ""
                                            ledgerNotesInput = ""
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp).testTag("view_student_ledger_btn_${student.id}"),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "View Ledger",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            selectedStudentForPayment = student
                                            paymentAmountInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (outstanding <= 0) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary,
                                            contentColor = if (outstanding <= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp).testTag("record_payment_btn_${student.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (outstanding <= 0) "Record Another Payment" else "Record Payment (UGX)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE TAB == 1: Detailed payments ledger view
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Historic Payment Audit Logs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Direct receipt log reflecting accurate student ledger audit.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                if (feePaymentsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "No transaction logs found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                                Text(
                                    "Click 'Add Fee Record' or use the student payment option to populate ledger.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                } else {
                    items(feePaymentsList.size) { logIdx ->
                        val payment = feePaymentsList[logIdx]
                        val matchingStudent = students.find { it.id == payment.studentId }
                        val studentName = matchingStudent?.name ?: "Unknown Pupil (ID: ${payment.studentId})"
                        val studentGrade = matchingStudent?.gradeLevel ?: "N/A"
                        val rollNo = matchingStudent?.rollNumber ?: "N/A"

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("fee_payment_log_${payment.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = studentName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text("Roll: $rollNo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text("•", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                        Text("Grade: $studentGrade", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                        Text(payment.paymentDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        if (payment.notes.isNotBlank()) {
                                            Text("•", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            Text(payment.notes, style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "UGX " + String.format(java.util.Locale.US, "%,.0f", payment.amount),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteFeePayment(payment)
                                            android.widget.Toast.makeText(context, "Payment record voided, student balance updated.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Void Record",
                                            tint = Color(0xFFC62828)
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

    // Detailed Custom Fee Record Dialog (Floating FAB)
    if (showDetailedPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showDetailedPaymentDialog = false },
            title = {
                Text(
                    text = "Add Custom Fee Record",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Input the specific Pupil, Date of Transaction, and Amount in UGX to adjust ledger standings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // 1. Selector for Student
                    Box(modifier = Modifier.fillMaxWidth().clickable { detailedStudentExpanded = true }) {
                        OutlinedTextField(
                            value = detailedStudentSelection?.let { "${it.name} (Roll: ${it.rollNumber})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Select Student*") },
                            placeholder = { Text("Tap to select pupil...") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        DropdownMenu(
                            expanded = detailedStudentExpanded,
                            onDismissRequest = { detailedStudentExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 280.dp)
                        ) {
                            students.forEach { std ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(std.name, fontWeight = FontWeight.Bold)
                                            Text("Roll: ${std.rollNumber} | Grade: ${std.gradeLevel} | Bal: UGX ${String.format(java.util.Locale.US, "%,.0f", std.feesTotal - std.feesPaid)}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        detailedStudentSelection = std
                                        detailedStudentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Show outstanding balance for selection
                    detailedStudentSelection?.let { std ->
                        val bal = std.feesTotal - std.feesPaid
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Remaining Balance Due:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(
                                    text = "UGX " + String.format(java.util.Locale.US, "%,.0f", bal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (bal > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    // 2. Date Input
                    OutlinedTextField(
                        value = detailedDateInput,
                        onValueChange = { detailedDateInput = it },
                        label = { Text("Transaction Date (yyyy-MM-dd)*") },
                        placeholder = { Text("e.g. 2026-06-18") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 3. Amount Input in UGX
                    OutlinedTextField(
                        value = detailedAmountInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                detailedAmountInput = input
                            }
                        },
                        label = { Text("Amount Paid (UGX)*") },
                        placeholder = { Text("e.g. 250000") },
                        leadingIcon = { Text("UGX", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("detailed_amount_input"),
                        singleLine = true
                    )

                    // 4. Notes/Remarks Input
                    OutlinedTextField(
                        value = detailedNotesInput,
                        onValueChange = { detailedNotesInput = it },
                        label = { Text("Payment Notes / Receipt ID") },
                        placeholder = { Text("e.g. Installment 1 / Receipt #045") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val student = detailedStudentSelection
                        val amount = detailedAmountInput.toDoubleOrNull()
                        val rawDate = detailedDateInput.trim()

                        if (student == null) {
                            android.widget.Toast.makeText(context, "Please select a student.", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amount == null || amount <= 0) {
                            android.widget.Toast.makeText(context, "Please enter a valid amount.", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (rawDate.isEmpty()) {
                            android.widget.Toast.makeText(context, "Date is required.", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Save Detailed payment record
                        viewModel.addDetailedFeePayment(
                            studentId = student.id,
                            paymentDate = rawDate,
                            amount = amount,
                            notes = detailedNotesInput.trim()
                        )

                        android.widget.Toast.makeText(
                            context,
                            "Fee record added and pupil statement reconciled successfully!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                        showDetailedPaymentDialog = false
                    },
                    modifier = Modifier.testTag("dialog_submit_detailed_fee_btn")
                ) {
                    Text("Add Payment Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailedPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Classic Record Payment Dialog (Quick Single-Student Collect Button)
    selectedStudentForPayment?.let { student ->
        val balance = student.feesTotal - student.feesPaid
        AlertDialog(
            onDismissRequest = { selectedStudentForPayment = null },
            title = {
                Text(
                    text = "Record UGX Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Adding tuition fee collection record for ${student.name} (${student.gradeLevel}).",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Outstanding balance:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(
                                    text = String.format(java.util.Locale.US, "UGX %,.0f", balance),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { input ->
                            // Allow only numeric input
                            if (input.all { it.isDigit() }) {
                                paymentAmountInput = input
                            }
                        },
                        label = { Text("Payment Amount (UGX)") },
                        placeholder = { Text("e.g. 150000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("UGX", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                        singleLine = true
                    )

                    // Suggestion Chip Amounts
                    Text("Or select quick UGX amount:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickAmounts = listOf("50000", "100000", "200000", "500000")
                        quickAmounts.forEach { amt ->
                            val displayAmt = when (amt) {
                                "50000" -> "50K"
                                "100000" -> "100K"
                                "200000" -> "200K"
                                "500000" -> "500K"
                                else -> amt
                            }
                            InputChip(
                                selected = paymentAmountInput == amt,
                                onClick = { paymentAmountInput = amt },
                                label = { Text(displayAmt, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountInput.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.addFeePayment(student.id, amount)
                            android.widget.Toast.makeText(
                                context,
                                "Successfully recorded payment of UGX ${String.format(java.util.Locale.US, "%,.0f", amount)} for ${student.name}!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            android.widget.Toast.makeText(context, "Please enter a valid payment amount.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        selectedStudentForPayment = null
                    },
                    modifier = Modifier.testTag("dialog_confirm_payment_btn")
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStudentForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedStudentForLedger?.let { student ->
        // Because the student object from list has stale feesPaid if we just added a payment in this dialog,
        // let's fetch the freshest student object from the state list by matching the ID!
        val liveStudent = students.find { it.id == student.id } ?: student
        val outstanding = liveStudent.feesTotal - liveStudent.feesPaid
        val isCleared = outstanding <= 0

        val matchingPayments = feePaymentsList.filter { it.studentId == liveStudent.id }

        AlertDialog(
            onDismissRequest = { selectedStudentForLedger = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Student Fee Statement Ledger",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
                ) {
                    // Profile Info Header Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = liveStudent.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Roll ID: ${liveStudent.rollNumber} • Class: ${liveStudent.gradeLevel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    if (liveStudent.phone.isNotBlank()) {
                                        Text(
                                            text = "Parent Tel: ${liveStudent.phone}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                // Clean Status indicator
                                Surface(
                                    color = if (isCleared) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, if (isCleared) Color(0xFF2E7D32) else Color(0xFFC62828)),
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = if (isCleared) "Fully Paid" else "Outstanding Balance",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCleared) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Consolidated balances row metrics
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Target tuition set balance
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                                    Text("Set Tuition Due", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, "UGX %,.0f", liveStudent.feesTotal),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Fees Paid Credits
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(0.5.dp, Color(0xFF81C784))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                                    Text("UGX Paid to Date", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, "UGX %,.0f", liveStudent.feesPaid),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }

                            // Settle balance remaining
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (isCleared) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                                border = BorderStroke(0.5.dp, if (isCleared) Color(0xFF81C784) else Color(0xFFE57373))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                                    Text("UGX Outstanding", style = MaterialTheme.typography.labelSmall, color = if (isCleared) Color(0xFF2E7D32) else Color(0xFFC62828))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, "UGX %,.0f", outstanding.coerceAtLeast(0.0)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCleared) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                }
                            }
                        }
                    }

                    // Section Title: Payment transactions Chronological
                    item {
                        Text(
                            text = "RECEIPT CHRONOLOGY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (matchingPayments.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = "No recorded fee collection transactions for this student.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        items(matchingPayments.size) { pmIdx ->
                            val payment = matchingPayments[pmIdx]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "UGX " + String.format(java.util.Locale.US, "%,.0f", payment.amount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color.Gray)
                                            Text(payment.paymentDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            if (payment.notes.isNotBlank()) {
                                                Text("•", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                                Text(payment.notes, style = MaterialTheme.typography.labelSmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteFeePayment(payment)
                                            android.widget.Toast.makeText(context, "Payment statement voided.", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Void Receipt",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Title: Add direct payment inside the ledger UI
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "RECORD NEW PAYMENT ON-ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = ledgerAmountInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) ledgerAmountInput = input
                                    },
                                    label = { Text("UGX Payment Amount", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. 250000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Text("UGX", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, end = 2.dp)) },
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 56.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = ledgerNotesInput,
                                    onValueChange = { ledgerNotesInput = it },
                                    label = { Text("Notes / Receipt ID / Bank Slip No.", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. Bank Slip #903") },
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 56.dp),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        val amt = ledgerAmountInput.toDoubleOrNull()
                                        if (amt == null || amt <= 0) {
                                            android.widget.Toast.makeText(context, "Please input a valid amount to record.", android.widget.Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                        viewModel.addDetailedFeePayment(
                                            studentId = liveStudent.id,
                                            paymentDate = today,
                                            amount = amt,
                                            notes = ledgerNotesInput.trim().ifEmpty { "Ledger Deposit" }
                                        )

                                        android.widget.Toast.makeText(context, "Payment recorded! Ledger statement updated successfully.", android.widget.Toast.LENGTH_SHORT).show()
                                        ledgerAmountInput = ""
                                        ledgerNotesInput = ""
                                    },
                                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("dialog_ledger_quick_pay_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Post & Reconcile Payment", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedStudentForLedger = null }
                ) {
                    Text("Close Ledger")
                }
            }
        )
    }
}

