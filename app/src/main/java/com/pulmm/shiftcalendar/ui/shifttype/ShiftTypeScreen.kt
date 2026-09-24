package com.pulmm.shiftcalendar.ui.shifttype

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.rememberRepository

private val PALETTE = listOf(
    0xFF4A90D9.toInt(), 0xFF8E44AD.toInt(), 0xFF27AE60.toInt(),
    0xFFE67E22.toInt(), 0xFFE74C3C.toInt(), 0xFF7F8C8D.toInt(),
    0xFF2C3E50.toInt(), 0xFFF1C40F.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftTypeScreen() {
    val viewModel: ShiftTypeViewModel = viewModel(factory = ShiftTypeViewModel.Factory(rememberRepository()))
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("근무 종류 관리") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(shiftTypes, key = { it.id }) { shiftType ->
                ShiftTypeRow(shiftType = shiftType, onDelete = { viewModel.deleteShiftType(shiftType) })
            }
        }
    }

    if (showAddDialog) {
        AddShiftTypeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addShiftType(name, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ShiftTypeRow(shiftType: ShiftType, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(shiftType.colorArgb))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(shiftType.name, modifier = Modifier.weight(1f, fill = true))
        TextButton(onClick = onDelete) { Text("삭제") }
    }
}

@Composable
private fun AddShiftTypeDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PALETTE.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("근무 종류 추가") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름 (예: 주간)") })
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    PALETTE.forEach { colorInt ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .clickable { selectedColor = colorInt }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) }, enabled = name.isNotBlank()) {
                Text("추가")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
