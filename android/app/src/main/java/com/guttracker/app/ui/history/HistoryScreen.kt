package com.guttracker.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guttracker.app.AppContainer
import com.guttracker.app.R
import com.guttracker.app.ui.additem.AddItemSheet
import com.guttracker.app.ui.components.AddTagDialog
import com.guttracker.app.ui.components.CalendarPickerDialog
import com.guttracker.app.ui.components.DigestionCard
import com.guttracker.app.ui.components.ItemGrid
import com.guttracker.app.ui.components.LoggedEntriesCard
import com.guttracker.app.ui.components.NotesCard
import com.guttracker.app.ui.components.SectionHeader
import com.guttracker.app.ui.components.StatusCard
import com.guttracker.app.ui.components.TagRow
import com.guttracker.app.ui.components.WellbeingCard
import com.guttracker.app.ui.editentry.EditEntrySheet
import com.guttracker.app.ui.theme.AppColors
import com.guttracker.app.util.millisToTimeOfDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(container: AppContainer) {
    val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory { initializer { HistoryViewModel(container) } })
    val state by viewModel.uiState.collectAsState()
    val datesWithData by viewModel.datesWithData.collectAsState()
    var addItemOpen by remember { mutableStateOf(false) }
    var editingEntryId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddTag by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 0.dp)) {
            Text("History", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            Text("Review or fix any past day", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        }

        if (!state.loaded) return@Column

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Box(Modifier.padding(20.dp, 16.dp, 20.dp, 0.dp)) {
                    StatusCard(
                        iconRes = R.drawable.ic_calendar_tab,
                        iconTint = AppColors.Accent,
                        iconBg = AppColors.AccentSoft,
                        title = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)),
                        subtitle = if (state.selectedDate == LocalDate.now()) "Today · tap to change date" else "Tap to change date",
                        trailing = { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = AppColors.TextMuted) },
                        onClick = { showDatePicker = true },
                    )
                }
            }
            item {
                Box(Modifier.padding(20.dp, 12.dp, 20.dp, 0.dp)) {
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
            item { Box(Modifier.padding(20.dp, 14.dp, 20.dp, 0.dp)) { WellbeingCard(state.wellbeingRating ?: 7.0, viewModel::setWellbeing) } }
            item { Box(Modifier.padding(20.dp, 14.dp, 20.dp, 0.dp)) { DigestionCard(state.digestionRating ?: 3.0, viewModel::setDigestion) } }
            item {
                Box(Modifier.padding(20.dp, 14.dp, 20.dp, 0.dp)) {
                    TagRow("That day was", state.tags, state.contextTagId, viewModel::setContextTag, onAddClick = { showAddTag = true })
                }
            }
            item { Box(Modifier.padding(20.dp, 14.dp, 20.dp, 0.dp)) { NotesCard(state.notes, viewModel::setNotes) } }
            item { SectionHeader("Drinks", hint = "tap to log") }
            item { ItemGrid(items = state.drinks, onTap = { viewModel.logItem(it) }, showAdd = false, onAdd = {}) }
            item { SectionHeader("Food") }
            item { ItemGrid(items = state.foods, onTap = { viewModel.logItem(it) }, showAdd = true, onAdd = { addItemOpen = true }) }
            item { SectionHeader("Logged entries", hint = if (state.loggedEntries.isNotEmpty()) "tap to edit" else null) }
            item { LoggedEntriesCard(entries = state.loggedEntries, emptyText = "Nothing logged on this day", onEdit = { editingEntryId = it }) }
        }
    }

    if (addItemOpen) {
        AddItemSheet(container = container, timestampMillis = viewModel.newEntryTimestamp(), onDismiss = { addItemOpen = false })
    }
    editingEntryId?.let { id ->
        EditEntrySheet(container = container, localId = id, onDismiss = { editingEntryId = null })
    }
    if (showAddTag) {
        AddTagDialog(onDismiss = { showAddTag = false }, onAdd = viewModel::addTag)
    }
    if (showDatePicker) {
        CalendarPickerDialog(
            selectedDate = state.selectedDate,
            markedDates = datesWithData,
            onDismiss = { showDatePicker = false },
            onSelect = { date ->
                viewModel.selectDate(date)
                showDatePicker = false
            },
        )
    }
}
