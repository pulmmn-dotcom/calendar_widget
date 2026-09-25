package com.pulmm.shiftcalendar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val repository: ShiftRepository,
    startMonth: YearMonth? = null
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(startMonth ?: YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = visibleMonth

    val shiftTypes: StateFlow<List<ShiftType>> = repository.observeShiftTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthDayInfos: StateFlow<List<DayInfo>> = visibleMonth
        .flatMapLatest { month ->
            repository.observeDayInfoRange(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun goToPreviousMonth() { visibleMonth.value = visibleMonth.value.minusMonths(1) }
    fun goToNextMonth() { visibleMonth.value = visibleMonth.value.plusMonths(1) }
    fun goToMonth(yearMonth: YearMonth) { visibleMonth.value = yearMonth }

    fun setOverride(date: LocalDate, shiftTypeId: Long?) {
        viewModelScope.launch { repository.setOverride(date.toEpochDay(), shiftTypeId) }
    }

    fun setMemo(date: LocalDate, text: String) {
        viewModelScope.launch { repository.setMemo(date.toEpochDay(), text) }
    }

    class Factory(
        private val repository: ShiftRepository,
        private val startMonth: YearMonth? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(repository, startMonth) as T
    }
}
