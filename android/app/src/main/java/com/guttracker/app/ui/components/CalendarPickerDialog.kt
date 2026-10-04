package com.guttracker.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.guttracker.app.R
import com.guttracker.app.ui.theme.AppColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val SUNDAY_FIRST_WEEK = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

/**
 * A month calendar for picking a day, with a dot under any day present in [markedDates].
 * The stock Material3 DatePicker (compose-bom 2024.10.01 / material3 1.3.1) has no hook to
 * render per-day content, so this is a small hand-rolled replacement scoped to what History needs.
 */
@Composable
fun CalendarPickerDialog(
    selectedDate: LocalDate,
    markedDates: Set<LocalDate>,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    var visibleMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = AppColors.Surface) {
            Column(modifier = Modifier.padding(20.dp).width(300.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        visibleMonth.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MonthNavButton(rotationDegrees = 180f, onClick = { visibleMonth = visibleMonth.minusMonths(1) })
                        MonthNavButton(
                            rotationDegrees = 0f,
                            enabled = visibleMonth.isBefore(currentMonth),
                            onClick = { visibleMonth = visibleMonth.plusMonths(1) },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dow in SUNDAY_FIRST_WEEK) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                dow.getDisplayName(TextStyle.NARROW, Locale.ENGLISH),
                                color = AppColors.TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))

                val firstOfMonth = visibleMonth.atDay(1)
                val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Sunday-first grid (DayOfWeek.SUNDAY.value == 7)
                val daysInMonth = visibleMonth.lengthOfMonth()
                val totalCells = leadingBlanks + daysInMonth
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - leadingBlanks + 1
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                if (dayNum in 1..daysInMonth) {
                                    val date = visibleMonth.atDay(dayNum)
                                    DayCell(
                                        day = dayNum,
                                        isSelected = date == selectedDate,
                                        isToday = date == today,
                                        isFuture = date.isAfter(today),
                                        isMarked = date in markedDates,
                                        onClick = { onSelect(date) },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AppColors.Sage))
                    Text("has logged data", color = AppColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MonthNavButton(rotationDegrees: Float, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(AppColors.Surface2)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = if (enabled) AppColors.TextPrimary else AppColors.TextMuted.copy(alpha = 0.35f),
            modifier = Modifier.size(12.dp).rotate(rotationDegrees),
        )
    }
}

@Composable
private fun DayCell(day: Int, isSelected: Boolean, isToday: Boolean, isFuture: Boolean, isMarked: Boolean, onClick: () -> Unit) {
    val textColor = when {
        isFuture -> AppColors.TextMuted.copy(alpha = 0.35f)
        isSelected -> Color.White
        else -> AppColors.TextPrimary
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) AppColors.Accent else Color.Transparent)
                .then(if (isToday && !isSelected) Modifier.border(BorderStroke(1.5.dp, AppColors.Accent), CircleShape) else Modifier)
                .then(if (!isFuture) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(day.toString(), color = textColor, fontSize = 13.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isMarked && !isFuture) AppColors.Sage else Color.Transparent),
        )
    }
}
