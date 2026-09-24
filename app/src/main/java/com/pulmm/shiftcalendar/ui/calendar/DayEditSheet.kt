package com.pulmm.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditSheet(
    date: LocalDate,
    dayInfo: DayInfo?,
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onSelectShiftType: (Long) -> Unit,
    onClearShiftType: () -> Unit,
    onSaveMemo: (String) -> Unit
) {
    var memoText by remember(date) { mutableStateOf(dayInfo?.memoText ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text("근무 선택", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                shiftTypes.forEach { shiftType ->
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(shiftType.colorArgb))
                            .clickable { onSelectShiftType(shiftType.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(shiftType.name, color = Color.White)
                    }
                }
            }
            TextButton(onClick = onClearShiftType) { Text("이 날짜를 패턴 기본값으로 되돌리기") }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = memoText,
                onValueChange = { memoText = it },
                label = { Text("메모") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { onSaveMemo(memoText); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text("저장") }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
