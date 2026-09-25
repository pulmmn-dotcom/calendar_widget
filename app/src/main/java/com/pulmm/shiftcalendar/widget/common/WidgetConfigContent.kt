package com.pulmm.shiftcalendar.widget.common

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulmm.shiftcalendar.MainActivity
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import com.pulmm.shiftcalendar.ui.theme.OnPrimaryFixed
import com.pulmm.shiftcalendar.ui.theme.PrimaryContainer
import com.pulmm.shiftcalendar.ui.theme.PrimaryFixed
import com.pulmm.shiftcalendar.ui.theme.ShiftCalendarTheme
import com.pulmm.shiftcalendar.ui.theme.SurfaceContainerHigh
import com.pulmm.shiftcalendar.ui.theme.SurfaceContainerLow
import com.pulmm.shiftcalendar.widget.preview.DayWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.MonthWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.PreviewType
import com.pulmm.shiftcalendar.widget.preview.SmallMonthWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.WeekWidgetPreview
import com.pulmm.shiftcalendar.widget.preview.WidgetPreviewBackdrop
import com.pulmm.shiftcalendar.widget.preview.rememberLivePreview
import kotlin.math.roundToInt

private class BgSwatch(val name: String, val argb: Int)

/** 배경색 6가지. 흰색이 첫 번째(기본값). */
private val BG_PALETTE = listOf(
    BgSwatch("화이트", 0xFFFFFFFF.toInt()),
    BgSwatch("연한 회색", 0xFFF1F5F9.toInt()),
    BgSwatch("짙은 남회색", 0xFF0F172A.toInt()),
    BgSwatch("네이비", 0xFF1E3A8A.toInt()),
    BgSwatch("딥 틸", 0xFF0F766E.toInt()),
    BgSwatch("딥 바이올렛", 0xFF5B21B6.toInt())
)

private const val SCALE_MIN = 0.7f
private const val SCALE_MAX = 1.6f
private val CardShape = RoundedCornerShape(16.dp)

/**
 * 위젯 설정 화면(전체 화면). 위쪽에 지금 고른 설정이 바로 반영되는 위젯 미리보기가 있다.
 * [initialStyle]이 바뀌면(저장된 설정을 읽어 온 뒤) 화면 상태를 그 값으로 다시 시작한다.
 * [onCancel]은 아무것도 바꾸지 않고 닫기, [onSave]는 지금 고른 설정으로 완료.
 */
@Composable
fun WidgetConfigContent(
    initialStyle: WidgetStyle,
    showLunarOption: Boolean,
    title: String,
    previewType: PreviewType,
    onCancel: () -> Unit,
    onSave: (WidgetStyle) -> Unit
) {
    var bgColor by remember(initialStyle) { mutableStateOf(initialStyle.bgColorArgb) }
    var opacity by remember(initialStyle) { mutableStateOf(initialStyle.opacity) }
    var dateFontScale by remember(initialStyle) { mutableStateOf(initialStyle.dateFontScale) }
    var shiftFontScale by remember(initialStyle) { mutableStateOf(initialStyle.shiftFontScale) }
    var memoFontScale by remember(initialStyle) { mutableStateOf(initialStyle.memoFontScale) }
    var showLunar by remember(initialStyle) { mutableStateOf(initialStyle.showLunar) }
    var weekStartMonday by remember(initialStyle) { mutableStateOf(initialStyle.weekStartMonday) }

    val currentStyle = WidgetStyle(bgColor, opacity, dateFontScale, shiftFontScale, memoFontScale, showLunar, weekStartMonday)
    val livePreview = rememberLivePreview()

    BackHandler(onBack = onCancel)

    ShiftCalendarTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CalendarColors.surfaceBg)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            ConfigHeader(title = title, onCancel = onCancel)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreviewPanel(
                    previewType = previewType,
                    style = currentStyle,
                    livePreview = livePreview
                )

                ConfigCard(icon = Icons.Filled.Palette, title = "배경 색상 테마", trailing = {
                    Text(
                        text = BG_PALETTE.firstOrNull { it.argb == bgColor }?.name ?: "이전에 고른 색",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BG_PALETTE.forEach { swatch ->
                            ColorSwatch(
                                argb = swatch.argb,
                                name = swatch.name,
                                selected = swatch.argb == bgColor,
                                onClick = { bgColor = swatch.argb }
                            )
                        }
                    }
                }

                ConfigCard(icon = Icons.Filled.Opacity, title = "배경 투명도", trailing = {
                    ValuePill("${(opacity * 100).roundToInt()}%")
                }) {
                    Text(
                        text = "내부 텍스트와 뱃지는 선명하게 유지되며, 배경 투명도만 조절됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("20%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = (it * 20).roundToInt() / 20f },
                            valueRange = 0.2f..1f,
                            steps = 15,
                            colors = configSliderColors(),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                ConfigCard(icon = Icons.Filled.FormatSize, title = "글자 크기 스케일 (0.7x ~ 1.6x)") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "항목별로 글자 크기를 조절합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    dateFontScale = 1f
                                    shiftFontScale = 1f
                                    memoFontScale = 1f
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("기본값 초기화", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    FontScaleRow("날짜 / 요일 글자 크기", dateFontScale) { dateFontScale = it }
                    FontScaleRow("근무 이름 글자 크기", shiftFontScale) { shiftFontScale = it }
                    FontScaleRow("메모 글자 크기", memoFontScale) { memoFontScale = it }
                }

                ConfigCard(icon = Icons.Filled.Tune, title = "부가 기능 설정") {
                    if (showLunarOption) {
                        SwitchRow(
                            title = "음력 표시",
                            tag = "하루/일주일 전용",
                            description = "오늘 날짜 옆에 음력 날짜를 함께 보여줍니다.",
                            checked = showLunar,
                            onCheckedChange = { showLunar = it }
                        )
                        Box(Modifier.fillMaxWidth().height(1.dp).background(CalendarColors.gridLine))
                    }
                    SwitchRow(
                        title = "월요일부터 시작",
                        tag = null,
                        description = "주의 첫 요일을 일요일 대신 월요일로 배치합니다.",
                        checked = weekStartMonday,
                        onCheckedChange = { weekStartMonday = it }
                    )
                }

                if (livePreview.isSample) {
                    PatternGuideCard()
                }
                Spacer(Modifier.height(4.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalendarColors.surfaceCard)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onSave(currentStyle) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ConfigHeader(title: String, onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(50))
                .background(SurfaceContainerHigh)
                .clickable(onClick = onCancel)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("취소", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

/** 지금 고른 설정이 바로 반영되는 위젯 미리보기. */
@Composable
private fun PreviewPanel(
    previewType: PreviewType,
    style: WidgetStyle,
    livePreview: com.pulmm.shiftcalendar.widget.preview.LivePreview
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("미리보기", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryFixed)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(Modifier.width(4.dp))
                Text("실시간", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        WidgetPreviewBackdrop(modifier = Modifier.fillMaxWidth()) {
            when (previewType) {
                PreviewType.DAY -> DayWidgetPreview(style, livePreview.data)
                PreviewType.WEEK -> WeekWidgetPreview(style, livePreview.data)
                PreviewType.SMALL_MONTH -> SmallMonthWidgetPreview(style, livePreview.data)
                PreviewType.MONTH -> MonthWidgetPreview(style, livePreview.data)
            }
        }
        if (livePreview.isSample) {
            Text(
                text = "근무 패턴을 등록하면 내 근무가 미리보기에 나와요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfigCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = CardShape,
        color = CalendarColors.surfaceCard,
        border = BorderStroke(1.dp, CalendarColors.borderSubtle),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun ValuePill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = OnPrimaryFixed,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryFixed)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun configSliderColors() = SliderDefaults.colors(
    thumbColor = MaterialTheme.colorScheme.primary,
    activeTrackColor = MaterialTheme.colorScheme.primary,
    inactiveTrackColor = SurfaceContainerHigh,
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent
)

@Composable
private fun ColorSwatch(argb: Int, name: String, selected: Boolean, onClick: () -> Unit) {
    val color = Color(argb)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick)
            .padding(if (selected) 4.dp else 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color)
                .border(1.dp, CalendarColors.borderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "$name 선택됨",
                    tint = if (isDarkBackground(argb)) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** 글자 크기 한 줄: 이름, 현재 배율, 둥근 -/+ 버튼, 아래에 슬라이더. 0.1 단위로 움직인다. */
@Composable
private fun FontScaleRow(label: String, value: Float, onChange: (Float) -> Unit) {
    fun snap(v: Float) = ((v * 10).roundToInt() / 10f).coerceIn(SCALE_MIN, SCALE_MAX)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, CalendarColors.gridLine, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(
                text = "%.1fx".format(value),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            StepButton(Icons.Filled.Remove, "$label 줄이기") { onChange(snap(value - 0.1f)) }
            Spacer(Modifier.width(6.dp))
            StepButton(Icons.Filled.Add, "$label 키우기") { onChange(snap(value + 0.1f)) }
        }
        Slider(
            value = value.coerceIn(SCALE_MIN, SCALE_MAX),
            onValueChange = { onChange(snap(it)) },
            valueRange = SCALE_MIN..SCALE_MAX,
            steps = 8,
            colors = configSliderColors()
        )
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    tag: String?,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                if (tag != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryFixed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/** 등록된 근무 패턴이 없을 때만 나오는 안내. 앱의 근무 패턴 탭으로 바로 보내 준다. */
@Composable
private fun PatternGuideCard() {
    val context = LocalContext.current
    Surface(
        shape = CardShape,
        color = SurfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("근무 패턴 연동 확인", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "근무 패턴이 등록되어 있어야 위젯에 근무가 표시돼요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "근무 패턴 설정하러 가기 →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable { openPatternTab(context) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun openPatternTab(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
        .putExtra(MainActivity.EXTRA_START_TAB, MainActivity.START_TAB_PATTERN)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
