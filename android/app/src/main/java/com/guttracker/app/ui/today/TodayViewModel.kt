package com.guttracker.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guttracker.app.AppContainer
import com.guttracker.app.data.local.ItemEntity
import com.guttracker.app.ui.components.EntryDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val drinks: List<ItemEntity> = emptyList(),
    val foods: List<ItemEntity> = emptyList(),
    val loggedToday: List<EntryDisplay> = emptyList(),
    val medicationTakenAt: Long? = null,
    val wellbeingRating: Double? = null,
    val digestionRating: Double? = null,
)

class TodayViewModel(private val container: AppContainer) : ViewModel() {
    private val today = LocalDate.now()
    private val todayKey = today.toString()

    val uiState: StateFlow<TodayUiState> = combine(
        container.catalogRepository.observeItemsByCategory("drink"),
        container.catalogRepository.observeItemsByCategory("food"),
        container.entryRepository.observeForDate(today),
        container.dailyLogRepository.observeByDate(todayKey),
    ) { drinks, foods, entries, dailyLog ->
        val itemNames = (drinks + foods).associate { it.id to it.name }
        TodayUiState(
            drinks = drinks,
            foods = foods,
            loggedToday = entries.map {
                EntryDisplay(it.localId, it.timestamp, it.label ?: itemNames[it.itemId] ?: "Unknown")
            },
            medicationTakenAt = dailyLog?.medicationTakenAt,
            wellbeingRating = dailyLog?.wellbeingRating,
            digestionRating = dailyLog?.digestionRating,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())

    init {
        viewModelScope.launch { runCatching { container.catalogRepository.refresh() } }
    }

    fun logItem(item: ItemEntity) {
        viewModelScope.launch {
            container.entryRepository.createEntry(System.currentTimeMillis(), item.category, item.id, null)
        }
    }

    fun toggleMedication() {
        viewModelScope.launch {
            val takenAt = uiState.value.medicationTakenAt
            container.dailyLogRepository.updateMedication(todayKey, if (takenAt == null) System.currentTimeMillis() else null)
        }
    }
}
