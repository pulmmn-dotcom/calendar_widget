package com.pulmm.shiftcalendar.ui.widgethelp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import com.pulmm.shiftcalendar.ui.theme.SurfaceContainer
import com.pulmm.shiftcalendar.ui.theme.SurfaceContainerLow
import com.pulmm.shiftcalendar.widget.common.DEFAULT_WIDGET_STYLE
import com.pulmm.shiftcalendar.widget.preview.DayWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.MonthWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.SmallMonthWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.WeekWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.WidgetPreviewBackdrop
import com.pulmm.shiftcalendar.widget.preview.samplePreviewData

/** 위젯 안내 화면. DB를 읽지 않고, 예시 데이터로 그린 위젯 모형만 보여준다. */
@Composable
fun WidgetHelpScreen() {
    val data = remember { samplePreviewData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "위젯 안내",
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        InstallGuideCard()

        WidgetSection(
            sizeChip = "2 × 2",
            name = "하루 위젯",
            sizeText = "110 × 110 dp",
            description = "컴팩트한 오늘 근무 한눈에 확인, 탭 시 빠른 메모 입력"
        ) { DayWidgetPreview(DEFAULT_WIDGET_STYLE, data) }

        WidgetSection(
            sizeChip = "4 × 2",
            name = "일주일 위젯",
            sizeText = "250 × 110 dp",
            description = "이번 주 7일 교대 일정을 가로 타임라인으로 파악 및 메모 요약"
        ) { WeekWidgetPreview(DEFAULT_WIDGET_STYLE, data) }

        WidgetSection(
            sizeChip = "3 × 2",
            name = "작은 한달 위젯",
            sizeText = "180 × 140 dp",
            description = "공간을 절약하며 근무 패턴 흐름을 컬러바로 직관적 조망"
        ) { SmallMonthWidgetPreview(DEFAULT_WIDGET_STYLE, data) }

        WidgetSection(
            sizeChip = "4 × 4",
            name = "한달 위젯",
            sizeText = "250 × 250 dp",
            description = "앱 실행 없이 홈에서 월간 이동, 근무명, 메모까지 완전 제공"
        ) { MonthWidgetPreview(DEFAULT_WIDGET_STYLE, data) }

        ThemeTipCard()
    }
}

// ---- 등록 방법 카드 ----

@Composable
private fun InstallGuideCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconCircle(
                icon = Icons.Default.Widgets,
                size = 40,
                iconSize = 24,
                background = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(Modifier.size(8.dp))
            Column {
                Text(
                    text = "홈 화면 위젯 등록 방법",
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "손쉬운 3단계 설치 가이드",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StepBox("STEP 1", "홈 빈 곳 길게 터치", Icons.Default.TouchApp, Modifier.weight(1f))
            StepBox("STEP 2", "[위젯] 메뉴 선택", Icons.Default.DashboardCustomize, Modifier.weight(1f))
            StepBox("STEP 3", "교대캘린더 드래그", Icons.Default.DragIndicator, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StepBox(step: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = CalendarColors.surfaceCard,
        border = BorderStroke(1.dp, CalendarColors.borderSubtle),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconCircle(icon = icon, size = 32, iconSize = 18, background = SurfaceContainer)
            Spacer(Modifier.height(6.dp))
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun IconCircle(icon: ImageVector, size: Int, iconSize: Int, background: Color) {
    Box(
        modifier = Modifier.size(size.dp).background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

// ---- 위젯 종류별 소개 ----

@Composable
private fun WidgetSection(
    sizeChip: String,
    name: String,
    sizeText: String,
    description: String,
    preview: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = sizeChip,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = name,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = sizeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        WidgetPreviewBackdrop(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
            preview()
        }
    }
}

// ---- 맞춤 팁 카드 ----

@Composable
private fun ThemeTipCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFFDBCB),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.size(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "홈 테마 맞춤 팁",
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "위젯마다 배경 색상, 배경 투명도(20~100%), 글자 크기(0.7~1.6배)를 내 폰 배경화면에 맞춰 " +
                    "바꿀 수 있어요. 위젯 위의 ⚙ 버튼을 누르면 언제든 설정을 바꿀 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}
