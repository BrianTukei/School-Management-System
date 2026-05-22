package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.SmsLog
import com.example.ui.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsBroadcastScreen(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsStateWithLifecycle()
    val smsLogs by viewModel.smsLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Compose, 1: Sent Logs
    
    // Form Inputs
    var audienceType by remember { mutableStateOf("Class") } // "Class" or "Individual"
    var selectedGrade by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableStateOf<Int?>(null) }
    var messageText by remember { mutableStateOf("") }
    
    // Dropdowns
    var showGradeDropdown by remember { mutableStateOf(false) }
    var showStudentDropdown by remember { mutableStateOf(false) }
    
    // Status SnackBar/Message Feed
    var snackbarMessage by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Unique classes extracted from student list
    val classes = remember(students) {
        students.map { it.gradeLevel }.distinct().sorted()
    }

    // Set initial grade if not set
    LaunchedEffect(classes) {
        if (selectedGrade.isEmpty() && classes.isNotEmpty()) {
            selectedGrade = classes.first()
        }
    }

    // Target parent selection count & numbers
    val targetNumbersAndLabel = remember(audienceType, selectedGrade, selectedStudentId, students) {
        if (audienceType == "Class") {
            val matches = students.filter { it.gradeLevel == selectedGrade && it.phone.isNotBlank() }
            val numbers = matches.map { it.phone }
            val label = "All Parents in $selectedGrade"
            Pair(numbers, label)
        } else {
            val match = students.find { it.id == selectedStudentId }
            val numbers = if (match != null && match.phone.isNotBlank()) listOf(match.phone) else emptyList()
            val label = match?.let { "${it.name}'s Parent" } ?: "Select a pupil"
            Pair(numbers, label)
        }
    }

    // Permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (numbers, label) = targetNumbersAndLabel
            if (numbers.isNotEmpty() && messageText.isNotBlank()) {
                viewModel.broadcastSms(label, numbers, messageText)
                snackbarMessage = "SMS Broadcast Sent successfully!"
                showSnackbar = true
                messageText = ""
            }
        } else {
            snackbarMessage = "Permission denied. Message registered in local logs only."
            showSnackbar = true
            val (numbers, label) = targetNumbersAndLabel
            viewModel.broadcastSms(label, numbers, messageText)
            messageText = ""
        }
    }

    // Preset templates
    val templates = listOf(
        Pair("Fees Notice", "Dear Parent/Guardian, please be informed that school fees payments for Term 2 must be completed or arranged. Thank you. pearl Junior School Office."),
        Pair("Emergency Closing", "Dear Parent/Guardian, safe notice: school will be closed tomorrow due to heavy localized community floods. Classes resume Tuesday. Principal."),
        Pair("PTA Invite", "Dear Parent/Guardian, you are kindly invited to our General Primary Teacher-Parent Association (PTA) assembly on Saturday at 10:00 AM. Thank you."),
        Pair("Absent Alert", "Dear Parent/Guardian, your child was marked ABSENT during today's general assembly roll call records. Please confirm safety status.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers using modern custom rounded card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                listOf("Compose Mail", "Sent Broadcasts").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedTab == 0) {
                // COMPOSE VIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Header card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SendToMobile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "Guardian SMS Broadcasting Core",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Broadcast general notices, school fees balances or security instant updates.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Recipient Selector
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "1. Define Audience Target",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Row Selection
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    listOf("Class" to "Bulk Grade Broadcast", "Individual" to "Single Parent").forEach { (type, label) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (audienceType == type) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                                .clickable { audienceType = type }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (audienceType == type) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (audienceType == "Class") {
                                    // Class Dropdown Selection
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = if (selectedGrade.isEmpty()) "Select Grade Level class" else selectedGrade,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Target Class Level") },
                                            modifier = Modifier.fillMaxWidth(),
                                            trailingIcon = {
                                                IconButton(onClick = { showGradeDropdown = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Show Classes")
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                        DropdownMenu(
                                            expanded = showGradeDropdown,
                                            onDismissRequest = { showGradeDropdown = false },
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                            if (classes.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("No active classes found") },
                                                    onClick = { showGradeDropdown = false }
                                                )
                                            } else {
                                                classes.forEach { grade ->
                                                    DropdownMenuItem(
                                                        text = { Text(grade) },
                                                        onClick = {
                                                            selectedGrade = grade
                                                            showGradeDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Individual Student Dropdown Selection
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        val chosenStudent = students.find { it.id == selectedStudentId }
                                        val displayValue = chosenStudent?.let { "${it.name} (${it.rollNumber}) - ${it.phone}" } ?: "Select Pupil / Guardian Contact"
                                        OutlinedTextField(
                                            value = displayValue,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Target Pupil Guardian") },
                                            modifier = Modifier.fillMaxWidth(),
                                            trailingIcon = {
                                                IconButton(onClick = { showStudentDropdown = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Show Pupils")
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                        DropdownMenu(
                                            expanded = showStudentDropdown,
                                            onDismissRequest = { showStudentDropdown = false },
                                            modifier = Modifier.heightIn(max = 280.dp).fillMaxWidth(0.9f)
                                        ) {
                                            if (students.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("No registered students found") },
                                                    onClick = { showStudentDropdown = false }
                                                )
                                            } else {
                                                students.forEach { st ->
                                                    DropdownMenuItem(
                                                        text = { Column {
                                                            Text(st.name, fontWeight = FontWeight.SemiBold)
                                                            Text("Parent: ${st.phone} | ${st.gradeLevel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                        }},
                                                        onClick = {
                                                            selectedStudentId = st.id
                                                            showStudentDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Audience Stats indicator
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "This message will be dispatched to ${targetNumbersAndLabel.first.size} verified numbers.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Presets
                    item {
                        Column {
                            Text(
                                "Fast Template Drafts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(templates) { (title, content) ->
                                    Card(
                                        onClick = {
                                            messageText = content
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Column {
                                                Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Text(content, style = MaterialTheme.typography.bodySmall, maxLines = 1, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Message box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "2. Compose Message Body",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    placeholder = { Text("Type Pearl Junior official message content...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                // Part calculator display
                                val chars = messageText.length
                                val parts = if (chars == 0) 0 else (chars / 160) + 1
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Character limits: $chars / 160 chars per SMS",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (chars > 160) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "$parts ${if (parts == 1) "Message Part" else "Message Parts"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    item {
                        Button(
                            onClick = {
                                val (numbers, label) = targetNumbersAndLabel
                                if (numbers.isEmpty()) {
                                    snackbarMessage = "No parent phone numbers identified. Verify directory."
                                    showSnackbar = true
                                    return@Button
                                }
                                if (messageText.isBlank()) {
                                    snackbarMessage = "Message content fails sanity check. Cannot send blank text."
                                    showSnackbar = true
                                    return@Button
                                }

                                // Check real runtime cellular sending permissions
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.broadcastSms(label, numbers, messageText)
                                    snackbarMessage = "Bulk SMS successfully dispatched to system and device."
                                    showSnackbar = true
                                    messageText = ""
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                } else {
                                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Broadcast SMS Instantly", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else {
                // HISTORIC SENT LOGS VIEW
                if (smsLogs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkChatRead,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Pearl SMS Outbox is Empty",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Any broadcasts or messages sent to guardians/parents will be locally archived and tracked here for accountability.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(smsLogs) { log ->
                            SmsLogCard(log = log)
                        }
                    }
                }
            }
        }

        // Beautiful material design Snackbar for alert notifications
        if (showSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                    }
                }
            ) {
                Text(snackbarMessage)
            }
            LaunchedEffect(snackbarMessage) {
                kotlinx.coroutines.delay(4000L)
                showSnackbar = false
            }
        }
    }
}

@Composable
fun SmsLogCard(log: SmsLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = log.recipientName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = log.dateSent,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Sent tag status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SENT SUCCESS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneIphone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Phone contacts: ${log.phoneNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}
