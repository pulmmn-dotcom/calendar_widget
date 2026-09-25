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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    var editingShiftType by remember { mutableStateOf<ShiftType?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("근무 종류 관리") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "근무 종류 추가")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(shiftTypes, key = { it.id }) { shiftType ->
                ShiftTypeRow(
                    shiftType = shiftType,
                    onEdit = { editingShiftType = shiftType },
                    onDelete = { viewModel.deleteShiftType(shiftType) }
                )
            }
        }
    }

    if (showAddDialog) {
        ShiftTypeDialog(
            title = "근무 종류 추가",
            initialName = "",
            initialColor = PALETTE.first(),
            confirmLabel = "추가",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addShiftType(name, color)
                showAddDialog = false
            }
        )
    }

    editingShiftType?.let { target ->
        ShiftTypeDialog(
            title = "근무 종류 수정",
            initialName = target.name,
            initialColor = target.colorArgb,
            confirmLabel = "저장",
            onDismiss = { editingShiftType = null },
            onConfirm = { name, color ->
                viewModel.updateShiftType(target.copy(name = name, colorArgb = color))
                editingShiftType = null
            }
        )
    }
}

@Composable
private fun ShiftTypeRow(shiftType: ShiftType, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(shiftType.colorArgb))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(shiftType.name, modifier = Modifier.weight(1f, fill = true))
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "수정")
        }
        TextButton(onClick = { showDeleteConfirm = true }) { Text("삭제") }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("근무 종류 삭제") },
            text = { Text("이 근무 종류를 삭제하시겠어요? 이미 등록된 패턴/날짜에서는 '삭제됨'으로 표시됩니다.") },
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

@Composable
private fun ShiftTypeDialog(
    title: String,
    initialName: String,
    initialColor: Int,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                                .then(
                                    if (colorInt == selectedColor) {
                                        Modifier.border(2.dp, Color.Black, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedColor = colorInt }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) }, enabled = name.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
