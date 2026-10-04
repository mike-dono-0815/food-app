package com.guttracker.app.ui.today

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
import com.guttracker.app.ui.components.ItemGrid
import com.guttracker.app.ui.components.LoggedEntriesCard
import com.guttracker.app.ui.components.SectionHeader
import com.guttracker.app.ui.components.StatusCard
import com.guttracker.app.ui.editentry.EditEntrySheet
import com.guttracker.app.ui.theme.AppColors
import com.guttracker.app.util.millisToTimeOfDay
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            Text("Gut Tracker", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
            Text(
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)),
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
                LoggedEntriesCard(entries = state.loggedToday, emptyText = "Nothing logged yet today", onEdit = { editingEntryId = it })
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
