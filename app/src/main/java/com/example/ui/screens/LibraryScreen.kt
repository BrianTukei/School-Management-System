@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NotificationsActive
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
import com.example.data.entity.Book
import com.example.data.entity.BookCheckout
import com.example.ui.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LibraryScreen(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Inventory", "Circulation", "Overdue Alerts")

    val booksState by viewModel.books.collectAsStateWithLifecycle()
    val loansState by viewModel.checkouts.collectAsStateWithLifecycle()
    val studentsState by viewModel.students.collectAsStateWithLifecycle()

    // Dialog trigger states
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var preselectedBookIdForCheckout by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 2.dp,
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
                                    imageVector = Icons.Default.Book,
                                    contentDescription = "Library Icon",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                "School Library Hub",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Track books, students circulation & automated reminders",
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val icon = when(index) {
                                    0 -> Icons.Default.LibraryBooks
                                    1 -> Icons.Default.SwapCalls
                                    else -> Icons.Default.NotificationsActive
                                }
                                Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
                                Text(title, fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("library_tab_$index")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTab) {
                    0 -> InventoryTab(
                        books = booksState,
                        onAddBookClick = { showAddBookDialog = true },
                        onCheckoutBook = { bookId ->
                            preselectedBookIdForCheckout = bookId
                            showCheckoutDialog = true
                        },
                        onDeleteBook = { book ->
                            viewModel.deleteBook(book)
                            Toast.makeText(context, "${book.title} removed from library", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> CirculationTab(
                        loans = loansState,
                        books = booksState,
                        students = studentsState,
                        onIssueBookClick = {
                            preselectedBookIdForCheckout = null
                            showCheckoutDialog = true
                        },
                        onReturnBook = { checkoutId ->
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            viewModel.returnBook(checkoutId, todayStr)
                            Toast.makeText(context, "Book marked as returned successfully", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteLoan = { checkout ->
                            viewModel.deleteCheckout(checkout)
                        }
                    )
                    2 -> OverdueAlertsTab(
                        loans = loansState,
                        books = booksState,
                        students = studentsState,
                        onSendAlert = { studentName, phone, bookTitle, dueDate ->
                            val message = "Dear parent, this is a friendly reminder from Pearl Academy that student $studentName has an outstanding library book loans \"$bookTitle\" which was due on $dueDate. Kindly helper return it soon."
                            viewModel.broadcastSms(studentName, listOf(phone), message)
                            Toast.makeText(context, "Overdue SMS Reminder dispatched for $studentName!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }

    // Dialog: Add Book
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { title, author, isbn, category, total ->
                viewModel.insertBook(title, author, isbn, category, total)
                showAddBookDialog = false
                Toast.makeText(context, "Book \"$title\" added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Issue Book (Checkout)
    if (showCheckoutDialog) {
        CheckoutBookDialog(
            books = booksState.filter { it.copiesAvailable > 0 },
            students = studentsState.filter { it.status == "Active" },
            initialBookId = preselectedBookIdForCheckout,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { bId, sId, checkDate, durDate, notes ->
                val chosenBook = booksState.find { it.id == bId }
                val chosenStudent = studentsState.find { it.id == sId }
                if (chosenBook != null && chosenStudent != null) {
                    viewModel.checkoutBook(
                        bookId = bId,
                        studentId = sId,
                        studentName = chosenStudent.name,
                        bookTitle = chosenBook.title,
                        checkoutDate = checkDate,
                        dueDate = durDate,
                        notes = notes
                    )
                    showCheckoutDialog = false
                    Toast.makeText(context, "Book checked out successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error: Book or Student not valid", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun InventoryTab(
    books: List<Book>,
    onAddBookClick: () -> Unit,
    onCheckoutBook: (Int) -> Unit,
    onDeleteBook: (Book) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Mathematics", "Integrated Science", "Social Studies", "English Language", "Fiction", "General")

    val filteredBooks = books.filter { book ->
        val matchesSearch = book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true) ||
                book.isbn.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || book.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBookClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_book_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Title, Author, or ISBN") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_search_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Layout
            Text(
                "Filter by General Subjects",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "No books match current criteria.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBooks, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onCheckoutClick = { onCheckoutBook(book.id) },
                            onDeleteClick = { onDeleteBook(book) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(
    book: Book,
    onCheckoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("book_card_${book.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = book.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (book.isbn.isNotBlank()) {
                            Text(
                                "ISBN: ${book.isbn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        book.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val avail = book.copiesAvailable
                    val total = book.copiesTotal
                    val statusText = "$avail / $total Available"
                    val isAvailable = avail > 0

                    Surface(
                        shape = CircleShape,
                        color = if (isAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            statusText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            onClick = onCheckoutClick,
                            enabled = book.copiesAvailable > 0,
                            modifier = Modifier.testTag("book_checkout_btn_${book.id}")
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Issue Book")
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.testTag("book_delete_btn_${book.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Book")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CirculationTab(
    loans: List<BookCheckout>,
    books: List<Book>,
    students: List<com.example.data.entity.Student>,
    onIssueBookClick: () -> Unit,
    onReturnBook: (Int) -> Unit,
    onDeleteLoan: (BookCheckout) -> Unit
) {
    var filterStatus by remember { mutableStateOf("All") } // "All", "Active", "Returned"

    val filteredLoans = loans.filter { loan ->
        when (filterStatus) {
            "Active" -> loan.returnDate == null
            "Returned" -> loan.returnDate != null
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onIssueBookClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("issue_book_fab")
            ) {
                Icon(Icons.Default.Assignment, contentDescription = "Issue Book Record")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Active", "Returned").forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text("$status Loans") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredLoans.isEmpty()) {
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
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "No circulation records found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLoans, key = { it.id }) { loan ->
                        val matchingBook = books.find { it.id == loan.bookId }
                        val matchingStudent = students.find { it.id == loan.studentId }

                        CirculationCard(
                            loan = loan,
                            book = matchingBook,
                            student = matchingStudent,
                            onReturn = { onReturnBook(loan.id) },
                            onDelete = { onDeleteLoan(loan) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CirculationCard(
    loan: BookCheckout,
    book: Book?,
    student: com.example.data.entity.Student?,
    onReturn: () -> Unit,
    onDelete: () -> Unit
) {
    val isReturned = loan.returnDate != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReturned) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isReturned) MaterialTheme.colorScheme.outlineVariant
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isReturned) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isReturned) "Returned" else "Active Borrow",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isReturned) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = book?.title ?: "Unknown Book (ID: ${loan.bookId})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = student?.let { "${it.name} (Grade: ${it.gradeLevel})" } ?: "Unknown Student (ID: ${loan.studentId})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Issued on: ${loan.checkoutDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Due date: ${loan.dueDate}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isReturned) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
                    if (isReturned) {
                        Text(
                            "Returned: ${loan.returnDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (!isReturned) {
                    Button(
                        onClick = onReturn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("loan_return_btn_${loan.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Return")
                    }
                }
            }

            if (loan.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notes: ${loan.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OverdueAlertsTab(
    loans: List<BookCheckout>,
    books: List<Book>,
    students: List<com.example.data.entity.Student>,
    onSendAlert: (studentName: String, phone: String, bookTitle: String, dueDate: String) -> Unit
) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // An active loan is overdue if returnDate is null AND current date is past the loan's due date
    val overdueLoans = loans.filter { loan ->
        if (loan.returnDate != null) return@filter false
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val due = sdf.parse(loan.dueDate) ?: Date()
            val today = sdf.parse(todayStr) ?: Date()
            due.before(today)
        } catch (e: Exception) {
            false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Surface(
            tonalElevation = 1.dp,
            color = if (overdueLoans.isNotEmpty()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, if (overdueLoans.isNotEmpty()) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = if (overdueLoans.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (overdueLoans.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = if (overdueLoans.isNotEmpty()) "${overdueLoans.size} Overdue Book Loans Detected!"
                        else "No Overdue Books Currently",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (overdueLoans.isNotEmpty()) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (overdueLoans.isNotEmpty()) "Send immediate parent alerts to expedite safe book returns to Pearl Academy."
                        else "All outstanding circulations are well within their active borrow limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (overdueLoans.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Recommend,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Library Status: All Clear!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(overdueLoans, key = { it.id }) { loan ->
                    val mBook = books.find { it.id == loan.bookId }
                    val mStudent = students.find { it.id == loan.studentId }

                    OverdueAlertCard(
                        loan = loan,
                        book = mBook,
                        student = mStudent,
                        onSendAlert = {
                            if (mStudent != null && mBook != null) {
                                onSendAlert(mStudent.name, mStudent.phone, mBook.title, loan.dueDate)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OverdueAlertCard(
    loan: BookCheckout,
    book: Book?,
    student: com.example.data.entity.Student?,
    onSendAlert: () -> Unit
) {
    val context = LocalContext.current
    var lastAlertSentTime by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            "OVERDUE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = book?.title ?: "Unknown Book",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = student?.let { "${it.name} (Roll: ${it.rollNumber}, Class: ${it.gradeLevel})" } ?: "Unknown Student",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Expected Return Date: ${loan.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (student != null) {
                        Text(
                            "Parent Contact: ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (lastAlertSentTime != null) {
                        Text(
                            "Last Notice Sent: $lastAlertSentTime",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Button(
                    onClick = {
                        onSendAlert()
                        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
                        lastAlertSentTime = sdf.format(Date())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("send_overdue_alert_btn_${loan.id}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SMS Alert")
                }
            }
        }
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, isbn: String, category: String, copiesTotal: Int) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var copiesString by remember { mutableStateOf("1") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Mathematics", "Integrated Science", "Social Studies", "English Language", "Fiction", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Register New Library Book",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth().testTag("add_book_title_field"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_book_author_field"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN (Optional)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_book_isbn_field"),
                    singleLine = true
                )

                // Category selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { categoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = copiesString,
                    onValueChange = { copiesString = it },
                    label = { Text("Total Copies Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_book_copies_field"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || author.isBlank()) {
                        Toast.makeText(context, "Error: Title and Author are mandatory", Toast.LENGTH_SHORT).show()
                    } else {
                        val total = copiesString.toIntOrNull() ?: 1
                        onConfirm(title, author, isbn, selectedCategory, total)
                    }
                },
                modifier = Modifier.testTag("add_book_dialog_confirm")
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CheckoutBookDialog(
    books: List<Book>,
    students: List<com.example.data.entity.Student>,
    initialBookId: Int?,
    onDismiss: () -> Unit,
    onConfirm: (bookId: Int, studentId: Int, checkoutDate: String, dueDate: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var selectedBookId by remember { mutableStateOf(initialBookId ?: books.firstOrNull()?.id ?: 0) }
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: 0) }
    
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Default system due date: 7 days relative ahead
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
    val defaultDue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

    var checkoutDate by remember { mutableStateOf(today) }
    var dueDate by remember { mutableStateOf(defaultDue) }
    var notes by remember { mutableStateOf("") }

    var bookExpanded by remember { mutableStateOf(false) }
    var studentExpanded by remember { mutableStateOf(false) }

    val activeSelectedBook = books.find { it.id == selectedBookId }
    val activeSelectedStudent = students.find { it.id == selectedStudentId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Issue Book to Student",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (books.isEmpty()) {
                    Text("Error: No books in inventory with available copies.", color = MaterialTheme.colorScheme.error)
                } else if (students.isEmpty()) {
                    Text("Error: No registered active students available.", color = MaterialTheme.colorScheme.error)
                } else {
                    // Book selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = activeSelectedBook?.let { "${it.title} (By: ${it.author})" } ?: "Select Book",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Book to Issue") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { bookExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = bookExpanded,
                            onDismissRequest = { bookExpanded = false }
                        ) {
                            books.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.title} (${b.copiesAvailable} copies left)") },
                                    onClick = {
                                        selectedBookId = b.id
                                        bookExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Student selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = activeSelectedStudent?.let { "${it.name} (Grade: ${it.gradeLevel})" } ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Issue to Student") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { studentExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = studentExpanded,
                            onDismissRequest = { studentExpanded = false }
                        ) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.name} (${s.gradeLevel})") },
                                    onClick = {
                                        selectedStudentId = s.id
                                        studentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Issue Date Picker
                    OutlinedTextField(
                        value = checkoutDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Checkout Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val parsed = sdf.parse(checkoutDate) ?: Date()
                                    val c = Calendar.getInstance().apply { time = parsed }
                                    DatePickerDialog(
                                        context,
                                        { _: DatePicker, y: Int, m: Int, d: Int ->
                                            val newC = Calendar.getInstance().apply { set(y, m, d) }
                                            checkoutDate = sdf.format(newC.time)
                                        },
                                        c.get(Calendar.YEAR),
                                        c.get(Calendar.MONTH),
                                        c.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                } catch (ex: Exception) {}
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Choose Date")
                            }
                        }
                    )

                    // Due Date Picker
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val parsed = sdf.parse(dueDate) ?: Date()
                                    val c = Calendar.getInstance().apply { time = parsed }
                                    DatePickerDialog(
                                        context,
                                        { _: DatePicker, y: Int, m: Int, d: Int ->
                                            val newC = Calendar.getInstance().apply { set(y, m, d) }
                                            dueDate = sdf.format(newC.time)
                                        },
                                        c.get(Calendar.YEAR),
                                        c.get(Calendar.MONTH),
                                        c.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                } catch (ex: Exception) {}
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Choose Due Date")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Notes (e.g., Condition)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedBookId == 0 || selectedStudentId == 0) {
                        Toast.makeText(context, "Error: Book and Student must be selected", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(selectedBookId, selectedStudentId, checkoutDate, dueDate, notes)
                    }
                },
                enabled = books.isNotEmpty() && students.isNotEmpty(),
                modifier = Modifier.testTag("checkout_confirm_btn")
            ) {
                Text("Issue Book")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
