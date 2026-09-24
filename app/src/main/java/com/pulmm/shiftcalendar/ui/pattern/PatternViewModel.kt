package com.pulmm.shiftcalendar.ui.pattern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.data.dao.PatternWithItems
import com.pulmm.shiftcalendar.data.entity.ShiftType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class PatternViewModel(private val repository: ShiftRepository) : ViewModel() {

    val shiftTypes: StateFlow<List<ShiftType>> = repository.observeShiftTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patterns: StateFlow<List<PatternWithItems>> = repository.observePatterns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun savePattern(name: String, startDate: LocalDate, shiftTypeIdsInOrder: List<Long>) {
        viewModelScope.launch { repository.savePattern(name, startDate.toEpochDay(), shiftTypeIdsInOrder) }
    }

    fun deletePattern(patternWithItems: PatternWithItems) {
        viewModelScope.launch { repository.deletePattern(patternWithItems.pattern) }
    }

    class Factory(private val repository: ShiftRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PatternViewModel(repository) as T
    }
}
