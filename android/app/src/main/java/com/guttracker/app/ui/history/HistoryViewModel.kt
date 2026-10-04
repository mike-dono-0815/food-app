package com.guttracker.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guttracker.app.AppContainer
import com.guttracker.app.data.local.ItemEntity
import com.guttracker.app.data.local.TagEntity
import com.guttracker.app.ui.components.EntryDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class HistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val drinks: List<ItemEntity> = emptyList(),
    val foods: List<ItemEntity> = emptyList(),
    val loggedEntries: List<EntryDisplay> = emptyList(),
    val medicationTakenAt: Long? = null,
    val wellbeingRating: Double? = null,
    val digestionRating: Double? = null,
    val contextTagId: Int? = null,
    val notes: String = "",
    val tags: List<TagEntity> = emptyList(),
    val loaded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val container: AppContainer) : ViewModel() {
    private val zone = ZoneId.systemDefault()
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HistoryUiState> = _selectedDate.flatMapLatest { date ->
        combine(
            container.catalogRepository.observeItemsByCategory("drink"),
            container.catalogRepository.observeItemsByCategory("food"),
            container.catalogRepository.observeTags(),
            container.entryRepository.observeForDate(date),
            container.dailyLogRepository.observeByDate(date.toString()),
        ) { drinks, foods, tags, entries, dailyLog ->
            val itemNames = (drinks + foods).associate { it.id to it.name }
            HistoryUiState(
                selectedDate = date,
                drinks = drinks,
                foods = foods,
                tags = tags,
                loggedEntries = entries.map { EntryDisplay(it.localId, it.timestamp, it.label ?: itemNames[it.itemId] ?: "Unknown") },
                medicationTakenAt = dailyLog?.medicationTakenAt,
                wellbeingRating = dailyLog?.wellbeingRating,
                digestionRating = dailyLog?.digestionRating,
                contextTagId = dailyLog?.contextTagId ?: container.tagDao.getHomeTagId(),
                notes = dailyLog?.notes ?: "",
                loaded = true,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState(loaded = false))

    /** Days to mark in the calendar picker — anything with a saved daily log or at least one entry. */
    val datesWithData: StateFlow<Set<LocalDate>> = combine(
        container.dailyLogDao.observeAllDates(),
        container.entryDao.observeDistinctEntryDates(),
    ) { logDates, entryDates ->
        (logDates + entryDates).mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        viewModelScope.launch { runCatching { container.catalogRepository.refresh() } }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /** New entries backfilled onto a past day default to noon local time; "now" only makes sense when the selected day is today. */
    private fun timestampForNewEntry(): Long {
        val date = _selectedDate.value
        return if (date == LocalDate.now()) System.currentTimeMillis()
        else date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
    }

    fun newEntryTimestamp(): Long = timestampForNewEntry()

    fun logItem(item: ItemEntity) {
        viewModelScope.launch {
            container.entryRepository.createEntry(timestampForNewEntry(), item.category, item.id, null)
        }
    }

    fun toggleMedication() {
        viewModelScope.launch {
            val takenAt = uiState.value.medicationTakenAt
            container.dailyLogRepository.updateMedication(_selectedDate.value.toString(), if (takenAt == null) timestampForNewEntry() else null)
        }
    }

    fun setWellbeing(v: Double) {
        viewModelScope.launch { container.dailyLogRepository.updateWellbeing(_selectedDate.value.toString(), v) }
    }

    fun setDigestion(v: Double) {
        viewModelScope.launch { container.dailyLogRepository.updateDigestion(_selectedDate.value.toString(), v) }
    }

    fun setContextTag(tagId: Int) {
        viewModelScope.launch { container.dailyLogRepository.updateContextTag(_selectedDate.value.toString(), tagId) }
    }

    fun setNotes(notes: String) {
        viewModelScope.launch { container.dailyLogRepository.updateNotes(_selectedDate.value.toString(), notes.ifBlank { null }) }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            container.catalogRepository.createTag(name).onSuccess { setContextTag(it.id) }
        }
    }
}
