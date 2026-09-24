package com.pulmm.shiftcalendar.ui.pattern

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.dao.PatternWithItems
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.rememberRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternScreen() {
    val viewModel: PatternViewModel = viewModel(factory = PatternViewModel.Factory(rememberRepository()))
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val patterns by viewModel.patterns.collectAsState()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("근무 패턴 관리") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "근무 패턴 추가")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(patterns, key = { it.pattern.id }) { patternWithItems ->
                PatternRow(
                    patternWithItems = patternWithItems,
                    shiftTypeById = shiftTypeById,
                    onDelete = { viewModel.deletePattern(patternWithItems) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddPatternDialog(
            shiftTypes = shiftTypes,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, startDate, order ->
                viewModel.savePattern(name, startDate, order)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PatternRow(patternWithItems: PatternWithItems, shiftTypeById: Map<Long, ShiftType>, onDelete: () -> Unit) {
    val startDate = LocalDate.ofEpochDay(patternWithItems.pattern.startEpochDay)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f, fill = true)) {
                Text(patternWithItems.pattern.name, style = MaterialTheme.typography.titleMedium)
                Text("시작일: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { showDeleteConfirm = true }) { Text("삭제") }
        }
        Row(modifier = Modifier.padding(top = 8.dp)) {
            patternWithItems.items.sortedBy { it.orderIndex }.forEach { item ->
                val shiftType = shiftTypeById[item.shiftTypeId]
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shiftType?.colorArgb?.let { Color(it) } ?: Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(shiftType?.name ?: "삭제됨", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("근무 패턴 삭제") },
            text = { Text("이 근무 패턴을 삭제하시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPatternDialog(
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, List<Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var order by remember { mutableStateOf(listOf<Long>()) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("근무 패턴 추가") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("패턴 이름") })
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showDatePicker = true }) {
                    Text("시작일: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("순서대로 근무 종류를 탭해서 추가하세요", style = MaterialTheme.typography.bodySmall)
                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(shiftTypes, key = { it.id }) { shiftType ->
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(shiftType.colorArgb))
                                .clickable { order = order + shiftType.id }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(shiftType.name, color = Color.White)
                        }
                    }
                }
                Text("현재 순서: " + order.joinToString(" → ") { id -> shiftTypes.find { it.id == id }?.name ?: "?" })
                TextButton(onClick = { order = emptyList() }) { Text("순서 초기화") }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && order.isNotEmpty()) onConfirm(name, startDate, order) },
                enabled = name.isNotBlank() && order.isNotEmpty()
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )

    if (showDatePicker) {
        StartDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { date -> startDate = date; showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDatePickerDialog(initialDate: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
            }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    ) {
        DatePicker(state = state)
    }
}
