package com.pulmm.shiftcalendar.widget.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
            var loaded by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(epochDay) {
                text = repository.getDayInfoOnce(epochDay).memoText ?: ""
                loaded = true
            }

            MaterialTheme {
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text(date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")) + " 메모") },
                    text = {
                        OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
                    },
                    confirmButton = {
                        TextButton(
                            enabled = loaded,
                            onClick = {
                                scope.launch {
                                    repository.setMemo(epochDay, text)
                                    finish()
                                }
                            }
                        ) { Text("저장") }
                    },
                    dismissButton = { TextButton(onClick = { finish() }) { Text("취소") } }
                )
            }
        }
    }

    companion object {
        const val EPOCH_DAY_EXTRA = "epoch_day"
        val EPOCH_DAY_KEY = ActionParameters.Key<Long>(EPOCH_DAY_EXTRA)
    }
}
