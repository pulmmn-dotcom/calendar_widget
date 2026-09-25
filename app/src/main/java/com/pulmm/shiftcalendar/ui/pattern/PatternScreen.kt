package com.pulmm.shiftcalendar.ui.pattern

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.dao.PatternWithItems
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.rememberRepository
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val CardShape = RoundedCornerShape(16.dp)
private val DeleteTint = Color(0xFFDC2626)
private val DeleteTintBg = Color(0xFFFEE2E2)
private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy. MM. dd")

@Composable
fun PatternScreen() {
    val viewModel: PatternViewModel = viewModel(factory = PatternViewModel.Factory(rememberRepository()))
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val patterns by viewModel.patterns.collectAsState()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    var showAddDialog by remember { mutableStateOf(false) }
    var deletingPattern by remember { mutableStateOf<PatternWithItems?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") { PatternHeader(count = patterns.size) }

        if (patterns.isEmpty()) {
            item(key = "empty") { EmptyState() }
        }

        items(patterns, key = { it.pattern.id }) { patternWithItems ->
            PatternCard(
                patternWithItems = patternWithItems,
                shiftTypeById = shiftTypeById,
                onDelete = { deletingPattern = patternWithItems }
            )
        }

        item(key = "create") { CreatePatternCard(onClick = { showAddDialog = true }) }
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

    deletingPattern?.let { target ->
        DeleteConfirmDialog(
            onDismiss = { deletingPattern = null },
            onConfirm = {
                deletingPattern = null
                viewModel.deletePattern(target)
            }
        )
    }
}

@Composable
private fun PatternHeader(count: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)) {
        Text(
            text = "근무 패턴",
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "등록된 패턴 ${count}개",
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
                imageVector = Icons.Default.Autorenew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "아직 등록된 패턴이 없어요",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "아래에서 첫 근무 패턴을 만들어보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PatternCard(
    patternWithItems: PatternWithItems,
    shiftTypeById: Map<Long, ShiftType>,
    onDelete: () -> Unit
) {
    val startDate = LocalDate.ofEpochDay(patternWithItems.pattern.startEpochDay)
    val sortedItems = remember(patternWithItems) { patternWithItems.items.sortedBy { it.orderIndex } }
    val chain = sortedItems.map { shiftTypeById[it.shiftTypeId] }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CalendarColors.surfaceCard,
        border = BorderStroke(1.dp, CalendarColors.borderSubtle),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = patternWithItems.pattern.name,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("${startDate.format(DateFormat)}부터 무한 반복 • ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                    append("${sortedItems.size}일 주기")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = DeleteTintBg, contentColor = DeleteTint)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f))
                    .padding(12.dp)
            ) {
                ShiftChainRow(items = chain)
            }
        }
    }
}

@Composable
private fun CreatePatternCard(onClick: () -> Unit) {
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
                    text = "새 패턴 만들기",
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "주기별 순환 근무를 손쉽게 추가하세요",
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
        title = { Text("근무 패턴 삭제", fontWeight = FontWeight.Bold) },
        text = { Text("이 근무 패턴을 삭제하시겠어요?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
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
    val canSave = name.isNotBlank() && order.isNotEmpty()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = CalendarColors.surfaceSheet,
        title = { Text("근무 패턴 추가", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    label = { Text("패턴 이름") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CalendarColors.borderSubtle, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "시작일",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = startDate.format(DateFormat),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "근무 종류를 순서대로 눌러 추가하세요",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (shiftTypes.isEmpty()) {
                    Text(
                        text = "먼저 '근무 종류'에서 근무를 만들어주세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        shiftTypes.forEach { shiftType ->
                            ShiftPill(shiftType = shiftType, onClick = { order = order + shiftType.id })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "현재 순서",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (order.isNotEmpty()) {
                        Text(
                            text = "${order.size}일 주기",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f))
                        .padding(12.dp)
                ) {
                    if (order.isEmpty()) {
                        Text(
                            text = "아직 비어 있어요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        ShiftChainFlow(items = order.map { shiftTypeById[it] })
                    }
                }
                TextButton(onClick = { order = emptyList() }) { Text("순서 초기화") }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (canSave) onConfirm(name, startDate, order) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
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
