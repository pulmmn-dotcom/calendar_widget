package com.pulmm.shiftcalendar.ui.shifttype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.data.entity.ShiftType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShiftTypeViewModel(private val repository: ShiftRepository) : ViewModel() {

    val shiftTypes: StateFlow<List<ShiftType>> = repository.observeShiftTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addShiftType(name: String, colorArgb: Int) {
        viewModelScope.launch { repository.addShiftType(name, colorArgb, shiftTypes.value.size) }
    }

    fun updateShiftType(shiftType: ShiftType) {
        viewModelScope.launch { repository.updateShiftType(shiftType) }
    }

    fun deleteShiftType(shiftType: ShiftType) {
        viewModelScope.launch { repository.deleteShiftType(shiftType) }
    }

    class Factory(private val repository: ShiftRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShiftTypeViewModel(repository) as T
    }
}
