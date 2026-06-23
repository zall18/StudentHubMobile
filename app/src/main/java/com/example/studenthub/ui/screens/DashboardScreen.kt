package com.example.studenthub.ui.screens

import StudentHubViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// ── Color Palette ────────────────────────────────────────────────────
private object DashboardColors {
    val background = Color(0xFFF6F7FB)
    val cardSurface = Color.White
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val textTertiary = Color(0xFF94A3B8)

    val blue = Color(0xFF3B82F6)
    val blueDark = Color(0xFF1D4ED8)
    val blueLight = Color(0xFFEFF6FF)
    val indigo = Color(0xFF6366F1)
    val indigoLight = Color(0xFFEEF2FF)
    val violet = Color(0xFF8B5CF6)
    val violetLight = Color(0xFFF5F3FF)

    val red = Color(0xFFEF4444)
    val redLight = Color(0xFFFEF2F2)
    val amber = Color(0xFFF59E0B)
    val amberLight = Color(0xFFFFFBEB)
    val emerald = Color(0xFF10B981)
    val emeraldDark = Color(0xFF059669)
    val emeraldLight = Color(0xFFECFDF5)

    val gradientHeader = listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
    val gradientFab = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
}

// ── Main Dashboard ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(viewModel: StudentHubViewModel) {
    val upcomingTasks by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val activeGoals by viewModel.activeGoals.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val highPriorityTaskCount by remember {
        derivedStateOf { upcomingTasks.count { it.priority == 3 } }
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Kuliah") }
    var taskPriority by remember { mutableStateOf(2) }

    Scaffold(
        containerColor = DashboardColors.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Hero Header ──────────────────────────────────────
            item { HeroGreetingCard() }

            // ── Stats Grid ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassStatCard(
                        icon = Icons.Rounded.Assignment,
                        value = upcomingTasks.size.toString(),
                        label = "Tugas",
                        subtitle = "minggu ini",
                        gradientColors = listOf(
                            DashboardColors.blue,
                            DashboardColors.indigo
                        ),
                        bgColor = DashboardColors.blueLight,
                        modifier = Modifier.weight(1f)
                    )
                    GlassStatCard(
                        icon = Icons.Rounded.PriorityHigh,
                        value = highPriorityTaskCount.toString(),
                        label = "Prioritas",
                        subtitle = "urgent",
                        gradientColors = listOf(
                            DashboardColors.red,
                            Color(0xFFE11D48)
                        ),
                        bgColor = DashboardColors.redLight,
                        modifier = Modifier.weight(1f)
                    )
                    GlassStatCard(
                        icon = Icons.Rounded.Flag,
                        value = activeGoals.size.toString(),
                        label = "Goals",
                        subtitle = "aktif",
                        gradientColors = listOf(
                            DashboardColors.violet,
                            Color(0xFFA855F7)
                        ),
                        bgColor = DashboardColors.violetLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Quick Add ────────────────────────────────────────
            item { QuickAddCard(onClick = { showAddTaskDialog = true }) }

            // ── Upcoming Tasks ────────────────────────────────────
            if (upcomingTasks.isNotEmpty()) {
                item {
                    PremiumSectionHeader(
                        title = "Tugas Mendatang",
                        icon = Icons.Rounded.Assignment,
                        count = upcomingTasks.size,
                        accentColor = DashboardColors.blue
                    )
                }
                items(upcomingTasks.take(5), key = { it.id }) { task ->
                    PremiumTaskCard(
                        task = task,
                        onComplete = { viewModel.completeTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }

            // ── Jadwal Kuliah ─────────────────────────────────────
            if (allSubjects.isNotEmpty()) {
                item {
                    PremiumSectionHeader(
                        title = "Jadwal Kuliah",
                        icon = Icons.Rounded.School,
                        count = allSubjects.size,
                        accentColor = DashboardColors.indigo
                    )
                }
                items(allSubjects.take(3), key = { it.id }) { subject ->
                    PremiumSubjectCard(subject)
                }
            }

            // ── Active Goals ──────────────────────────────────────
            if (activeGoals.isNotEmpty()) {
                item {
                    PremiumSectionHeader(
                        title = "Goals Aktif",
                        icon = Icons.Rounded.Flag,
                        count = activeGoals.size,
                        accentColor = DashboardColors.violet
                    )
                }
                items(activeGoals.take(3), key = { it.id }) { goal ->
                    PremiumGoalCard(goal)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── HERO GREETING CARD ───────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun HeroGreetingCard() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }
    val emoji = when (hour) {
        in 0..11 -> "☀️"
        in 12..14 -> "🌤️"
        in 15..17 -> "🌅"
        else -> "🌙"
    }
    val todayDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        .format(Date())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = DashboardColors.indigo.copy(alpha = 0.15f),
                spotColor = DashboardColors.indigo.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = DashboardColors.gradientHeader,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 260.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 220.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Date badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = todayDate,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$greeting $emoji",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Yuk selesaikan tugasmu hari ini!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GRADIENT FAB ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun GradientFab(onClick: () -> Unit) {
    LargeFloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = DashboardColors.indigo.copy(alpha = 0.3f),
                spotColor = DashboardColors.indigo.copy(alpha = 0.4f)
            )
            .background(
                brush = Brush.linearGradient(DashboardColors.gradientFab),
                shape = CircleShape
            )
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = "Tambah Tugas",
            modifier = Modifier.size(28.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── GLASS STAT CARD ──────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun GlassStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    subtitle: String,
    gradientColors: List<Color>,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = gradientColors[0].copy(alpha = 0.08f),
                spotColor = gradientColors[0].copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gradient icon container
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashboardColors.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gradientColors[0]
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = DashboardColors.textTertiary,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── QUICK ADD CARD (Dashed Border) ───────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun QuickAddCard(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quickAddScale"
    )

    val dashColor = DashboardColors.blue.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(12.dp.toPx(), 8.dp.toPx())
                        )
                    )
                )
            }
            .background(
                color = DashboardColors.blue.copy(alpha = 0.03f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                DashboardColors.blue.copy(alpha = 0.12f),
                                DashboardColors.indigo.copy(alpha = 0.12f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = DashboardColors.blue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Tambah tugas baru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DashboardColors.blue
                )
                Text(
                    text = "Ketuk untuk menambahkan",
                    fontSize = 11.sp,
                    color = DashboardColors.textTertiary
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM SECTION HEADER ───────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumSectionHeader(
    title: String,
    icon: ImageVector,
    count: Int,
    accentColor: Color
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
                    .background(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DashboardColors.textPrimary,
                letterSpacing = (-0.3).sp
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM TASK CARD ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumTaskCard(
    task: com.example.studenthub.data.Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale("id", "ID"))

    val priorityColor = when (task.priority) {
        3 -> DashboardColors.red
        2 -> DashboardColors.amber
        else -> DashboardColors.emerald
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = priorityColor.copy(alpha = 0.06f),
                spotColor = priorityColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Gradient priority strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(IntrinsicSize.Max)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(priorityColor, priorityColor.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DashboardColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = DashboardColors.textTertiary
                        )
                        Text(
                            text = dateFormat.format(Date(task.deadline)),
                            fontSize = 12.sp,
                            color = DashboardColors.textTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumPriorityChip(task.priority)
                        PremiumCategoryChip(task.category)
                    }
                }

                // Action column
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onComplete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    color = DashboardColors.emeraldLight,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Selesai",
                                tint = DashboardColors.emerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Hapus",
                                tint = DashboardColors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumPriorityChip(priority: Int) {
    val (bg, fg, text) = when (priority) {
        3 -> Triple(DashboardColors.redLight, DashboardColors.red, "Tinggi")
        2 -> Triple(DashboardColors.amberLight, DashboardColors.amber, "Sedang")
        else -> Triple(DashboardColors.emeraldLight, DashboardColors.emeraldDark, "Rendah")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

@Composable
fun PremiumCategoryChip(category: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DashboardColors.indigoLight
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = DashboardColors.indigo
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM SUBJECT CARD ─────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumSubjectCard(subject: com.example.studenthub.data.Subject) {
    val subjectColor = try {
        val colorLong = subject.color.removePrefix("#").toLong(16)
        Color(colorLong.toInt())
    } catch (e: Exception) {
        DashboardColors.blue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = subjectColor.copy(alpha = 0.06f),
                spotColor = subjectColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient subject icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(subjectColor, subjectColor.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.code.take(2).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DashboardColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (subject.scheduleDay.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = DashboardColors.textTertiary
                        )
                        Text(
                            text = "${subject.scheduleDay}, ${subject.scheduleTime}",
                            fontSize = 12.sp,
                            color = DashboardColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = DashboardColors.textTertiary
                    )
                    Text(
                        text = subject.lecturer,
                        fontSize = 12.sp,
                        color = DashboardColors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = subjectColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${subject.credits} SKS",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = subjectColor
                    )
                }

                if (subject.roomLocation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = DashboardColors.textTertiary
                        )
                        Text(
                            text = subject.roomLocation,
                            fontSize = 11.sp,
                            color = DashboardColors.textTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// ── PREMIUM GOAL CARD ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════
@Composable
fun PremiumGoalCard(goal: com.example.studenthub.data.Goal) {
    val progressColor = when {
        goal.progress >= 80 -> DashboardColors.emerald
        goal.progress >= 40 -> DashboardColors.amber
        else -> DashboardColors.blue
    }
    val progressGradient = when {
        goal.progress >= 80 -> listOf(DashboardColors.emerald, Color(0xFF34D399))
        goal.progress >= 40 -> listOf(DashboardColors.amber, Color(0xFFFBBF24))
        else -> listOf(DashboardColors.blue, DashboardColors.indigo)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = progressColor.copy(alpha = 0.06f),
                spotColor = progressColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Goal icon with gradient
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                brush = Brush.linearGradient(progressGradient),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = goal.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DashboardColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                PremiumGoalCategoryBadge(goal.category)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 12.sp,
                        color = DashboardColors.textTertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${goal.progress.roundToInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }

                // Gradient progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (goal.progress / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                brush = Brush.horizontalGradient(progressGradient)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumGoalCategoryBadge(category: String) {
    val (bg, fg) = when (category) {
        "ACADEMIC" -> DashboardColors.blueLight to DashboardColors.blue
        "PERSONAL" -> DashboardColors.violetLight to DashboardColors.violet
        else -> DashboardColors.amberLight to DashboardColors.amber
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = category.replace("_", " ").lowercase()
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}