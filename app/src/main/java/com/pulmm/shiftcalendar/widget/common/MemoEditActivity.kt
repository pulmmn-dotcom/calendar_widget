package com.pulmm.shiftcalendar.widget.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import com.pulmm.shiftcalendar.ui.theme.ShiftCalendarTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.glance.action.ActionParameters
import com.pulmm.shiftcalendar.ShiftCalendarApp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MemoEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val epochDay = intent.getLongExtra(EPOCH_DAY_EXTRA, LocalDate.now().toEpochDay())
        val repository = (application as ShiftCalendarApp).repository

        setContent {
            val date = remember { LocalDate.ofEpochDay(epochDay) }
            var text by remember { mutableStateOf("") }
            var loadedText by remember { mutableStateOf<String?>(null) }
            var loaded by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(epochDay) {
                val initial = repository.getDayInfoOnce(epochDay).memoText ?: ""
                text = initial
                loadedText = initial
                loaded = true
            }

            // 스펙 4.x: "저장 버튼 없이 즉시 반영" — 메모 편집에는 "취소(변경 폐기)" 개념이 없다.
            // 저장 버튼, 취소 버튼, 바깥 탭, 뒤로가기 등 모든 닫기 경로가 동일하게 저장 후 종료한다.
            val saveAndFinish: () -> Unit = {
                if (loaded && text != loadedText) {
                    scope.launch {
                        repository.setMemo(epochDay, text)
                        finish()
                    }
                } else {
                    finish()
                }
            }

            ShiftCalendarTheme {
                AlertDialog(
                    onDismissRequest = saveAndFinish,
                    title = { Text(date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")) + " 메모") },
                    text = {
                        OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
                    },
                    confirmButton = {
                        TextButton(enabled = loaded, onClick = saveAndFinish) { Text("저장") }
                    },
                    dismissButton = { TextButton(onClick = saveAndFinish) { Text("취소") } }
                )
            }
        }
    }

    companion object {
        const val EPOCH_DAY_EXTRA = "epoch_day"
        val EPOCH_DAY_KEY = ActionParameters.Key<Long>(EPOCH_DAY_EXTRA)
    }
}
