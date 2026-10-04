package com.guttracker.app.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guttracker.app.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayPoint(val date: LocalDate, val wellbeing: Double?, val digestion: Double?, val isVacation: Boolean)

data class OverviewUiState(
    val windowDays: Int = 30,
    val points: List<DayPoint> = emptyList(),
    val wellbeingAvg: Double? = null,
    val digestionAvg: Double? = null,
)

class OverviewViewModel(private val container: AppContainer) : ViewModel() {
    private val _windowDays = MutableStateFlow(30)
    private val _state = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { runCatching { container.catalogRepository.refresh() } }
        viewModelScope.launch {
            combine(_windowDays, container.dailyLogRepository.observeSince(LocalDate.now().minusDays(90).toString()), container.catalogRepository.observeTags()) { window, logs, tags ->
                val vacationId = tags.find { it.name == "Vacation" }?.id
                val start = LocalDate.now().minusDays((window - 1).toLong())
                val byDate = logs.associateBy { it.date }
                val points = (0 until window).map { i ->
                    val date = start.plusDays(i.toLong())
                    val log = byDate[date.toString()]
                    DayPoint(date, log?.wellbeingRating, log?.digestionRating, log?.contextTagId != null && log.contextTagId == vacationId)
                }
                val wellbeingVals = points.mapNotNull { it.wellbeing }
                val digestionVals = points.mapNotNull { it.digestion }
                OverviewUiState(
                    windowDays = window,
                    points = points,
                    wellbeingAvg = if (wellbeingVals.isNotEmpty()) wellbeingVals.average() else null,
                    digestionAvg = if (digestionVals.isNotEmpty()) digestionVals.average() else null,
                )
            }.collect { _state.value = it }
        }
    }

    fun setWindow(days: Int) { _windowDays.value = days }
}
