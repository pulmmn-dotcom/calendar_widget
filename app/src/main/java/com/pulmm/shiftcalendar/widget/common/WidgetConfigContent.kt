package com.pulmm.shiftcalendar.widget.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BG_PALETTE = listOf(
    0xFF2A2A2A.toInt(), 0xFFFFFFFF.toInt(), 0xFF1E3A5F.toInt(),
    0xFF4A2E2A.toInt(), 0xFF2E4A2E.toInt(), 0xFF3A2E4A.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigContent(initialStyle: WidgetStyle, showLunarOption: Boolean, onSave: (WidgetStyle) -> Unit) {
    var bgColor by remember { mutableStateOf(initialStyle.bgColorArgb) }
    var opacity by remember { mutableStateOf(initialStyle.opacity) }
    var dateFontScale by remember { mutableStateOf(initialStyle.dateFontScale) }
    var shiftFontScale by remember { mutableStateOf(initialStyle.shiftFontScale) }
    var memoFontScale by remember { mutableStateOf(initialStyle.memoFontScale) }
    var showLunar by remember { mutableStateOf(initialStyle.showLunar) }
    var weekStartMonday by remember { mutableStateOf(initialStyle.weekStartMonday) }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("위젯 디자인 설정") }) },
            bottomBar = {
                Button(
                    onClick = {
                        onSave(WidgetStyle(bgColor, opacity, dateFontScale, shiftFontScale, memoFontScale, showLunar, weekStartMonday))
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("완료") }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text("배경색")
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    BG_PALETTE.forEach { colorInt ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .then(
                                    if (colorInt == bgColor) {
                                        Modifier.border(2.dp, Color.Black, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { bgColor = colorInt }
                        )
                    }
                }
                Text("배경 투명도: ${(opacity * 100).toInt()}%")
                Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.2f..1f)

                Text("날짜/요일 글자 크기: ${"%.1f".format(dateFontScale)}배")
                Slider(value = dateFontScale, onValueChange = { dateFontScale = it }, valueRange = 0.7f..1.6f)

                Text("근무 이름 글자 크기: ${"%.1f".format(shiftFontScale)}배")
                Slider(value = shiftFontScale, onValueChange = { shiftFontScale = it }, valueRange = 0.7f..1.6f)

                Text("메모 글자 크기: ${"%.1f".format(memoFontScale)}배")
                Slider(value = memoFontScale, onValueChange = { memoFontScale = it }, valueRange = 0.7f..1.6f)

                if (showLunarOption) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Text("음력 표시", modifier = Modifier.weight(1f, fill = true))
                        Switch(checked = showLunar, onCheckedChange = { showLunar = it })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("월요일부터 시작", modifier = Modifier.weight(1f, fill = true))
                    Switch(checked = weekStartMonday, onCheckedChange = { weekStartMonday = it })
                }
            }
        }
    }
}
