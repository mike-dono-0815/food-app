package com.guttracker.app.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guttracker.app.AppContainer
import com.guttracker.app.data.local.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RatingUiState(
    val tags: List<TagEntity> = emptyList(),
    val wellbeingRating: Int = 7,
    val digestionRating: Int = 3,
    val contextTagId: Int? = null,
    val notes: String = "",
    val loaded: Boolean = false,
    val saved: Boolean = false,
)

class RatingViewModel(private val container: AppContainer) : ViewModel() {
    private val todayKey = com.guttracker.app.util.todayDateString()
    private val _state = MutableStateFlow(RatingUiState())
    val state: StateFlow<RatingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.catalogRepository.observeTags().collect { tags ->
                _state.value = _state.value.copy(tags = tags)
            }
        }
        viewModelScope.launch {
            val existing = container.dailyLogDao.getByDate(todayKey)
            val homeId = container.tagDao.getHomeTagId()
            _state.value = _state.value.copy(
                wellbeingRating = existing?.wellbeingRating ?: 7,
                digestionRating = existing?.digestionRating ?: 3,
                contextTagId = existing?.contextTagId ?: homeId,
                notes = existing?.notes ?: "",
                loaded = true,
            )
        }
    }

    fun setWellbeing(v: Int) { _state.value = _state.value.copy(wellbeingRating = v) }
    fun setDigestion(v: Int) { _state.value = _state.value.copy(digestionRating = v) }
    fun setTag(id: Int) { _state.value = _state.value.copy(contextTagId = id) }
    fun setNotes(v: String) { _state.value = _state.value.copy(notes = v) }

    fun addTag(name: String) {
        viewModelScope.launch {
            container.catalogRepository.createTag(name).onSuccess { setTag(it.id) }
        }
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            container.dailyLogRepository.saveRating(todayKey, s.wellbeingRating, s.digestionRating, s.contextTagId, s.notes.ifBlank { null })
            _state.value = s.copy(saved = true)
        }
    }
}
