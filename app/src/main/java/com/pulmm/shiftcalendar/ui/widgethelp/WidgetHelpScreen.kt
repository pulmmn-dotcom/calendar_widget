package com.pulmm.shiftcalendar.ui.widgethelp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetHelpScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("위젯 추가 방법") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(
                "홈 화면의 빈 곳을 길게 눌러 '위젯' 메뉴로 들어간 뒤, '교대캘린더'를 찾아 아래 4종류 중 원하는 크기를 골라 추가하세요.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            listOf(
                "한달위젯" to "한 달 전체 달력, 4x4 크기",
                "작은한달위젯" to "한 달을 더 작게, 3x2 크기",
                "일주일위젯" to "이번 주 7일, 가로로 긴 4x1 크기",
                "하루위젯" to "오늘 하루만, 가장 작은 1x1 크기"
            ).forEach { (title, desc) ->
                ListItem(headlineContent = { Text(title) }, supportingContent = { Text(desc) })
            }
        }
    }
}
