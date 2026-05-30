package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Attendance
import com.example.data.entity.Grade
import com.example.data.entity.Student
import java.util.Locale

// ==================== 1. FINANCIAL FEES DONUT CHART ====================
@Composable
fun FinanceFeesDonutChart(
    collected: Double,
    expected: Double,
    modifier: Modifier = Modifier
) {
    val total = if (expected <= 0.0) 1.0 else expected
    val percentPaid = (collected / total).toFloat().coerceIn(0f, 1f)
    val percentUnpaid = 1f - percentPaid

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(collected, expected) {
        animationTriggered = true
    }

    val animatedPaidAngle by animateFloatAsState(
        targetValue = if (animationTriggered) 360f * percentPaid else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val animatedUnpaidAngle by animateFloatAsState(
        targetValue = if (animationTriggered) 360f * percentUnpaid else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "FEES REVENUE DISTRIBUTION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart Drawing on Canvas
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val errorColor = MaterialTheme.colorScheme.error

                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radiusHeight = diameter / 2
                        val rectSize = Size(diameter, diameter)
                        val offset = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Base light grey circle background track
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            radius = size.minDimension / 2 - strokeWidth / 2,
                            style = Stroke(width = strokeWidth)
                        )

                        // Paid Arc segment
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = animatedPaidAngle,
                            useCenter = false,
                            topLeft = offset,
                            size = rectSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Outstanding segment (if greater than 0)
                        if (percentUnpaid > 0.01f) {
                            val startUnpaid = -90f + animatedPaidAngle
                            drawArc(
                                color = errorColor.copy(alpha = 0.85f),
                                startAngle = startUnpaid,
                                sweepAngle = animatedUnpaidAngle,
                                useCenter = false,
                                topLeft = offset,
                                size = rectSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center text label indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", percentPaid * 100),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Paid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem(
                        label = "Collected Revenue",
                        amount = "UGX " + String.format(Locale.US, "%,.0f", collected),
                        percentage = String.format(Locale.US, "%.1f%%", percentPaid * 100),
                        color = MaterialTheme.colorScheme.primary
                    )

                    LegendItem(
                        label = "Outstanding Balance",
                        amount = "UGX " + String.format(Locale.US, "%,.0f", expected - collected),
                        percentage = String.format(Locale.US, "%.1f%%", percentUnpaid * 100),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    amount: String,
    percentage: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Contribution: $percentage",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}


// ==================== 2. SUBJECT PERFORMANCE COLUMN/BAR CHART ====================
@Composable
fun SubjectPerformanceBarChart(
    grades: List<Grade>,
    modifier: Modifier = Modifier
) {
    // Process grades to gather subject-wise averages
    val subjectAvgs = remember(grades) {
        if (grades.isEmpty()) {
            listOf(
                "Mathematics" to 78f,
                "English Language" to 82f,
                "Integrated Science" to 74f,
                "Social Studies" to 85f,
                "Religious Educ." to 80f
            )
        } else {
            grades.groupBy { it.subjectName }
                .map { (subject, gradesList) ->
                    val avgPct = gradesList.map { (it.score / it.maxScore) * 100.0 }.average().toFloat()
                    subject to avgPct
                }
                .sortedByDescending { it.second }
                .take(5)
        }
    }

    var selectedBarIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = "ACADEMIC AVERAGE BY SUBJECT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (selectedBarIndex != -1 && selectedBarIndex < subjectAvgs.size) {
                    val element = subjectAvgs[selectedBarIndex]
                    Text(
                        text = "${element.first}: ${String.format(Locale.US, "%.1f%%", element.second)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = "Tap any bar to inspect",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Grid Canvas for Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 10.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val accentColor = MaterialTheme.colorScheme.tertiary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomMargin = 28.dp.toPx()
                    val topMargin = 12.dp.toPx()
                    val chartHeight = canvasHeight - bottomMargin - topMargin

                    val count = subjectAvgs.size
                    val spacing = 16.dp.toPx()
                    val availableWidth = canvasWidth - (spacing * (count + 1))
                    val barWidth = availableWidth / count

                    // Draw horizontal dotted grid lines for 25%, 50%, 75%, 100%
                    val gridSteps = listOf(0.25f, 0.50f, 0.75f, 1.00f)
                    gridSteps.forEach { step ->
                        val y = topMargin + chartHeight * (1f - step)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Render Bars
                    for (i in 0 until count) {
                        val pctValue = subjectAvgs[i].second.coerceIn(0f, 100f)
                        val barHeightFraction = pctValue / 100f
                        val activeHeight = chartHeight * barHeightFraction

                        val x = spacing + i * (barWidth + spacing)
                        val y = topMargin + chartHeight - activeHeight

                        val isSelected = selectedBarIndex == i
                        val topColor = if (isSelected) accentColor else primaryColor
                        val bottomColor = if (isSelected) accentColor.copy(alpha = 0.6f) else secondaryColor.copy(alpha = 0.6f)

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(topColor, bottomColor)
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, activeHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }

                // Add glass layers on top to catch click event coordinates accurately
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    for (i in subjectAvgs.indices) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    selectedBarIndex = if (selectedBarIndex == i) -1 else i
                                }
                        )
                        if (i < subjectAvgs.lastIndex) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            // Subject Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                subjectAvgs.forEachIndexed { index, pair ->
                    val isSelected = selectedBarIndex == index
                    Text(
                        text = pair.first.take(8),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedBarIndex = if (selectedBarIndex == index) -1 else index
                            },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


// ==================== 3. GRADE LEVEL ENROLLMENT CHART (HORIZONTAL ROW BARS) ====================
@Composable
fun GradeEnrollmentChart(
    students: List<Student>,
    modifier: Modifier = Modifier
) {
    // Collect counts per grade level
    val sortedGrades = listOf("Nursery", "Middle", "Top", "P.1", "P.2", "P.3", "P.4", "P.5", "P.6", "P.7")
    
    val gradeCounts = remember(students) {
        val groups = students.groupBy { it.gradeLevel }
        sortedGrades.map { gradeName ->
            val count = groups[gradeName]?.size ?: 0
            gradeName to count
        }
    }

    val maxCount = remember(gradeCounts) {
        val maxVal = gradeCounts.maxOfOrNull { it.second } ?: 1
        if (maxVal == 0) 1 else maxVal
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        .background(MaterialTheme.colorScheme.tertiary)
                )
                Text(
                    text = "STUDENT DENSITY BY GRADE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Render top 5 most highly populated grades and overall list
                gradeCounts.filter { it.second > 0 }.sortedByDescending { it.second }.take(4).forEach { (grade, count) ->
                    val percentageRatio = count.toFloat() / maxCount.toFloat()
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grade Level $grade",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$count Pupils",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Horizontal filling bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(percentageRatio)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.tertiary,
                                                MaterialTheme.colorScheme.tertiaryContainer
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }

                // If no students enrolled in the system yet
                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "No registered students found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==================== 4. WEEKLY ATTENDANCE TREND SPLINE CHART ====================
@Composable
fun SchoolAttendanceSplineChart(
    attendanceRecords: List<Attendance>,
    modifier: Modifier = Modifier
) {
    // Group records by Date and calculate attendance percentages
    val attendanceTrends = remember(attendanceRecords) {
        if (attendanceRecords.isEmpty()) {
            listOf(
                "Monday" to 95f,
                "Tuesday" to 92f,
                "Wednesday" to 96f,
                "Thursday" to 94f,
                "Friday" to 91f
            )
        } else {
            // Group by dates & compute daily rates
            val sortedMap = attendanceRecords.groupBy { it.date }
                .map { (date, list) ->
                    val totalCount = list.size
                    val presentCount = list.count { it.status == "Present" || it.status == "Late" }
                    val ratePct = if (totalCount > 0) (presentCount.toFloat() / totalCount) * 100f else 100f
                    date to ratePct
                }
                .sortedBy { it.first }
                .takeLast(5)
            
            // Format dates simply for horizontal legend
            sortedMap.map { (originalDate, rate) ->
                val simpleDate = originalDate.substringAfter("-") // e.g., "05-24" inside "2026-05-24"
                simpleDate to rate
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        .background(Color(0xFF2E7D32))
                )
                Text(
                    text = "WEEKLY ATTENDANCE RUN RATES (%)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF2E7D32)
                )
            }

            // Spline Line Graph on Custom Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 10.dp)
            ) {
                val errorColor = MaterialTheme.colorScheme.error
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomMargin = 20.dp.toPx()
                    val topMargin = 12.dp.toPx()
                    val graphHeight = canvasHeight - bottomMargin - topMargin

                    val numPoints = attendanceTrends.size
                    val stepX = canvasWidth / (numPoints - 1).coerceAtLeast(1)

                    // Draw safety threshold horizontal dotted line at 80% (School safety warning line)
                    val safetyY = topMargin + graphHeight * (1f - 0.8f)
                    drawLine(
                        color = errorColor.copy(alpha = 0.35f),
                        start = Offset(0f, safetyY),
                        end = Offset(canvasWidth, safetyY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Gather line path points
                    val pointsList = mutableListOf<Offset>()
                    for (i in 0 until numPoints) {
                        val percentage = attendanceTrends[i].second.coerceIn(0f, 100f)
                        val ratio = percentage / 100f
                        val x = i * stepX
                        val y = topMargin + graphHeight * (1f - ratio)
                        pointsList.add(Offset(x, y))
                    }

                    // Build path and area fill path
                    val path = Path()
                    val fillPath = Path()

                    if (pointsList.isNotEmpty()) {
                        path.moveTo(pointsList[0].x, pointsList[0].y)
                        fillPath.moveTo(pointsList[0].x, pointsList[0].y)

                        // Draw curved splines or elegant lines connecting vertices
                        for (i in 1 until pointsList.size) {
                            val previous = pointsList[i - 1]
                            val current = pointsList[i]
                            val controlX = (previous.x + current.x) / 2
                            
                            path.quadraticBezierTo(
                                controlX, previous.y,
                                current.x, current.y
                            )
                            fillPath.quadraticBezierTo(
                                controlX, previous.y,
                                current.x, current.y
                            )
                        }

                        // Close bottom boundary for undergradient area fill
                        fillPath.lineTo(canvasWidth, canvasHeight - bottomMargin)
                        fillPath.lineTo(0f, canvasHeight - bottomMargin)
                        fillPath.close()

                        // Draw the shaded gradient layout background underneath
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF81C784).copy(alpha = 0.35f),
                                    Color(0xFFC8E6C9).copy(alpha = 0.05f)
                                )
                            )
                        )

                        // Draw main line path
                        drawPath(
                            path = path,
                            color = Color(0xFF2E7D32),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw high-contrast vertex indicator bubbles
                        pointsList.forEachIndexed { idx, point ->
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color(0xFF2E7D32),
                                radius = 3.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            // Attendance Dates Labels Axis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                attendanceTrends.forEach { (date, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f%%", value),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (value >= 80f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
