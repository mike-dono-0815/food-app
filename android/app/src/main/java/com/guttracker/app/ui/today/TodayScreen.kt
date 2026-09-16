package com.guttracker.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.data.local.ItemEntity
import com.guttracker.app.ui.additem.AddItemSheet
import com.guttracker.app.ui.components.AddIconTile
import com.guttracker.app.ui.components.IconTile
import com.guttracker.app.ui.components.StatusCard
import com.guttracker.app.ui.editentry.EditEntrySheet
import com.guttracker.app.ui.theme.AppColors
import com.guttracker.app.util.millisToTimeOfDay
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(container: AppContainer, onOpenRating: () -> Unit) {
    val viewModel: TodayViewModel = viewModel(factory = viewModelFactory {
        initializer { TodayViewModel(container) }
    })
    val state by viewModel.uiState.collectAsState()
    var addItemOpen by remember { mutableStateOf(false) }
    var editingEntryId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 0.dp)) {
            Text("Today", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            Text(
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
            )
        }

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Box(Modifier.padding(20.dp, 16.dp, 20.dp, 0.dp)) {
                    StatusCard(
                        iconRes = R.drawable.ic_medication,
                        iconTint = if (state.medicationTakenAt != null) AppColors.Sage else AppColors.Accent,
                        iconBg = if (state.medicationTakenAt != null) AppColors.Surface2 else AppColors.AccentSoft,
                        title = "Medication",
                        subtitle = state.medicationTakenAt?.let { "Taken at ${millisToTimeOfDay(it)}" } ?: "Not taken yet",
                        subtitleColor = if (state.medicationTakenAt != null) AppColors.Sage else AppColors.TextSecondary,
                        cardBg = if (state.medicationTakenAt != null) AppColors.SageSoft else AppColors.Surface,
                        trailing = {
                            Icon(
                                painterResource(if (state.medicationTakenAt != null) R.drawable.ic_check else R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = if (state.medicationTakenAt != null) AppColors.Sage else AppColors.TextMuted,
                            )
                        },
                        onClick = { viewModel.toggleMedication() },
                    )
                }
            }
            item {
                Box(Modifier.padding(20.dp, 12.dp, 20.dp, 0.dp)) {
                    StatusCard(
                        iconRes = R.drawable.ic_pulse,
                        iconTint = AppColors.Accent,
                        iconBg = AppColors.AccentSoft,
                        title = "Log today's rating",
                        subtitle = if (state.wellbeingRating != null) "Wellbeing & digestion · done" else "Wellbeing & digestion · not done yet",
                        trailing = { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = AppColors.Accent) },
                        onClick = onOpenRating,
                    )
                }
            }
            item { SectionHeader("Drinks", hint = "tap to log") }
            item {
                ItemGrid(items = state.drinks, onTap = { viewModel.logItem(it) }, showAdd = false, onAdd = {})
            }
            item { SectionHeader("Food") }
            item {
                ItemGrid(items = state.foods, onTap = { viewModel.logItem(it) }, showAdd = true, onAdd = { addItemOpen = true })
            }
            item { SectionHeader("Logged today", hint = if (state.loggedToday.isNotEmpty()) "tap to edit" else null) }
            item {
                Box(Modifier.padding(20.dp, 10.dp, 20.dp, 0.dp)) {
                    if (state.loggedToday.isEmpty()) {
                        Text("Nothing logged yet today", color = AppColors.TextMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(4.dp))
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(AppColors.Surface)
                                .border(BorderStroke(1.dp, AppColors.Border), RoundedCornerShape(22.dp)),
                        ) {
                            state.loggedToday.forEachIndexed { index, entry ->
                                if (index > 0) HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { editingEntryId = entry.localId }
                                        .padding(16.dp, 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(millisToTimeOfDay(entry.timestampMillis), color = AppColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                                        Text(entry.name, color = AppColors.TextPrimary, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Edit", tint = AppColors.TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (addItemOpen) {
        AddItemSheet(container = container, onDismiss = { addItemOpen = false })
    }
    editingEntryId?.let { id ->
        EditEntrySheet(container = container, localId = id, onDismiss = { editingEntryId = null })
    }
}

@Composable
private fun SectionHeader(title: String, hint: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp, 16.dp, 20.dp, 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
        if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
    }
}

@Composable
private fun ItemGrid(items: List<ItemEntity>, onTap: (ItemEntity) -> Unit, showAdd: Boolean, onAdd: () -> Unit) {
    val columns = 5
    val rows = kotlin.math.ceil((items.size + if (showAdd) 1 else 0) / columns.toFloat()).toInt().coerceAtLeast(1)
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            index < items.size -> {
                                val item = items[index]
                                IconTile(name = item.name, category = item.category, onClick = { onTap(item) })
                            }
                            showAdd && index == items.size -> AddIconTile(onClick = onAdd)
                            else -> Spacer(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}
