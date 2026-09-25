package com.pulmm.shiftcalendar.ui.shifttype

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.calendar.ShiftBadge
import com.pulmm.shiftcalendar.ui.rememberRepository
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import com.pulmm.shiftcalendar.ui.theme.ShiftPalette

private val PALETTE = ShiftPalette.colors

private val CardShape = RoundedCornerShape(16.dp)
private val DeleteTint = Color(0xFFDC2626)
private val DeleteTintBg = Color(0xFFFEE2E2)

@Composable
fun ShiftTypeScreen() {
    val viewModel: ShiftTypeViewModel = viewModel(factory = ShiftTypeViewModel.Factory(rememberRepository()))
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingShiftType by remember { mutableStateOf<ShiftType?>(null) }
    var deletingShiftType by remember { mutableStateOf<ShiftType?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") { ShiftTypeHeader(count = shiftTypes.size) }

        if (shiftTypes.isEmpty()) {
            item(key = "empty") { EmptyState() }
        }

        items(shiftTypes, key = { it.id }) { shiftType ->
            ShiftTypeCard(
                shiftType = shiftType,
                onEdit = { editingShiftType = shiftType },
                onDelete = { deletingShiftType = shiftType }
            )
        }

        item(key = "create") { CreateShiftTypeCard(onClick = { showAddDialog = true }) }
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

    deletingShiftType?.let { target ->
        DeleteConfirmDialog(
            onDismiss = { deletingShiftType = null },
            onConfirm = {
                deletingShiftType = null
                viewModel.deleteShiftType(target)
            }
        )
    }
}

@Composable
private fun ShiftTypeHeader(count: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)) {
        Text(
            text = "근무 종류",
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "등록된 근무 종류 ${count}개",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "아직 등록된 근무 종류가 없어요",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "아래에서 첫 근무 종류를 만들어보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ShiftTypeCard(shiftType: ShiftType, onEdit: () -> Unit, onDelete: () -> Unit) {
    val color = Color(shiftType.colorArgb)

    Surface(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CalendarColors.surfaceCard,
        border = BorderStroke(1.dp, CalendarColors.borderSubtle),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = shiftType.name,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(8.dp))
            ShiftBadge(name = shiftType.name, color = color, modifier = Modifier.widthIn(max = 72.dp))
            Spacer(modifier = Modifier.size(8.dp))
            TonalIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "수정",
                tint = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.primaryContainer,
                onClick = onEdit
            )
            Spacer(modifier = Modifier.size(6.dp))
            TonalIconButton(
                icon = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = DeleteTint,
                background = DeleteTintBg,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun TonalIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(containerColor = background, contentColor = tint)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CreateShiftTypeCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CalendarColors.surfaceCard,
        border = BorderStroke(1.dp, CalendarColors.borderSubtle),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "새 근무 종류 만들기",
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "이름과 색을 정해서 추가해요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = CalendarColors.surfaceSheet,
        title = { Text("근무 종류 삭제", fontWeight = FontWeight.Bold) },
        text = { Text("이 근무 종류를 삭제하시겠어요? 이미 등록된 패턴/날짜에서는 '삭제됨'으로 표시됩니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
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
        shape = RoundedCornerShape(20.dp),
        containerColor = CalendarColors.surfaceSheet,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    label = { Text("이름 (예: 주간)") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "색상",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorGrid(selectedColor = selectedColor, onSelect = { selectedColor = it })
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "달력에서는",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    ShiftBadge(
                        name = name.trim().ifEmpty { "미리보기" },
                        color = Color(selectedColor),
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

/** 팔레트 8색을 한 줄에 4개씩 보여주는 색 선택 격자. 선택된 색은 고리와 흰 체크로 표시한다. */
@Composable
private fun ColorGrid(selectedColor: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PALETTE.chunked(4).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowColors.forEach { colorInt ->
                    ColorDot(
                        color = Color(colorInt),
                        selected = colorInt == selectedColor,
                        onClick = { onSelect(colorInt) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, color, CircleShape) else Modifier)
            .clickable(onClick = onClick)
            .padding(if (selected) 4.dp else 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
