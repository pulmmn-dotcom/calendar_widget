package com.pulmm.shiftcalendar.widget.day

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.R
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.logic.KoreanHolidays
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.widget.common.MemoEditActivity
import com.pulmm.shiftcalendar.widget.common.WidgetFit
import com.pulmm.shiftcalendar.widget.common.WidgetHeaderButtons
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.WidgetShiftPill
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import com.pulmm.shiftcalendar.widget.common.widgetPalette
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private const val DELETED_SHIFT_COLOR = 0xFF94A3B8.toInt()
private val COMPACT_HEIGHT = 125.dp
private const val NO_SHIFT_COLOR = 0xFF64748B.toInt()

class DayWidget : GlanceAppWidget() {
    // 실제 위젯 크기를 알아야 작은 칸(높이 110dp 안팎)에서 촘촘한 배치로 바꿀 수 있다.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val initialToday = LocalDate.now()
        // 첫 화면이 비어 보이지 않도록 처음 한 번만 미리 읽어 둔다.
        val initialDayInfo = repository.getDayInfoOnce(initialToday.toEpochDay())
        val initialShiftTypes = repository.getShiftTypesOnce()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            // 위젯 세션이 살아 있는 동안 update()는 provideGlance를 다시 부르지 않고 다시 그리기만 하므로,
            // 메모/근무/스타일 변경이 바로 보이도록 상태와 데이터를 컴포지션 안에서 읽는다.
            val rawStyle = currentState<Preferences>().toWidgetStyle()
            val today = LocalDate.now()
            val dayInfos by remember(today) {
                repository.observeDayInfoRange(today.toEpochDay(), today.toEpochDay())
            }.collectAsState(if (today == initialToday) listOf(initialDayInfo) else emptyList())
            val shiftTypes by remember { repository.observeShiftTypes() }.collectAsState(initialShiftTypes)
            val dayInfo: DayInfo? = dayInfos.firstOrNull()
            val shiftType = dayInfo?.shiftTypeId?.let { typeId -> shiftTypes.find { it.id == typeId } }
            val isDeletedShift = dayInfo?.shiftTypeId != null && shiftType == null
            val memo = dayInfo?.memoText?.takeIf { it.isNotBlank() }
            val lunar = remember(today, rawStyle.showLunar) {
                if (rawStyle.showLunar) LunarConverter.toLunar(today) else null
            }
            val size = LocalSize.current
            val compact = size.height < COMPACT_HEIGHT
            val weekday = today.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.KOREAN)
            val holiday = KoreanHolidays.nameOf(today)
            val weekdayLine = weekday + (holiday?.let { " · $it" } ?: "") +
                (lunar?.let { " (${it.toShortDisplay()})" } ?: "")
            // 위젯이 작은데 글자 배율이 크면 잘리므로, 이 크기에 들어가는 배율로 줄여서 그린다.
            val style = WidgetFit.day(
                rawStyle, size.width.value, size.height.value, compact, weekdayLine,
                "${today.monthValue}.${today.dayOfMonth}",
                shiftType?.name ?: if (isDeletedShift) "삭제됨" else "근무 없음",
                if (shiftType != null) (if (compact) 14f else 18f) else (if (compact) 12f else 14f),
                memo
            )
            val palette = widgetPalette(style)

            WidgetRoot(style) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().clickable(
                        actionStartActivity<MemoEditActivity>(
                            actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to today.toEpochDay())
                        )
                    )
                ) {
                    val weekdayStyle = TextStyle(
                        fontSize = ((if (compact) 10 else 11) * style.dateFontScale).sp,
                        color = ColorProvider(if (holiday != null) palette.sunday else palette.textSecondary)
                    )
                    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            val todayLabel = TextStyle(
                                fontSize = (10 * style.dateFontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(palette.sunday)
                            )
                            if (compact) {
                                // 낮은 칸: "오늘"과 요일을 한 줄로 합친다.
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("오늘", style = todayLabel)
                                    Spacer(modifier = GlanceModifier.width(4.dp))
                                    Text(weekdayLine, maxLines = 1, style = weekdayStyle)
                                }
                            } else {
                                Text("오늘", style = todayLabel)
                            }
                            Text(
                                "${today.monthValue}.${today.dayOfMonth}",
                                maxLines = 1,
                                style = TextStyle(
                                    fontSize = (28 * style.dateFontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(palette.textPrimary)
                                )
                            )
                        }
                        WidgetHeaderButtons(
                            palette, appWidgetId, DayWidgetConfigActivity::class.java,
                            vertical = true, boxSize = 22.dp, iconSize = 14.dp
                        )
                    }
                    if (!compact) Text(weekdayLine, maxLines = 1, style = weekdayStyle)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    when {
                        shiftType != null -> WidgetShiftPill(
                            name = shiftType.name, colorArgb = shiftType.colorArgb, style = style,
                            solid = true, baseFontSp = if (compact) 14f else 18f, cornerDp = 10f, padHorizontalDp = 10f, padVerticalDp = if (compact) 1f else 2f
                        )
                        isDeletedShift -> WidgetShiftPill(
                            name = "삭제됨", colorArgb = DELETED_SHIFT_COLOR, style = style,
                            solid = true, baseFontSp = if (compact) 12f else 14f, cornerDp = 10f, padHorizontalDp = 10f, padVerticalDp = if (compact) 1f else 2f
                        )
                        else -> WidgetShiftPill(
                            name = "근무 없음", colorArgb = NO_SHIFT_COLOR, style = style,
                            solid = false, baseFontSp = if (compact) 12f else 14f, cornerDp = 10f, padHorizontalDp = 10f, padVerticalDp = if (compact) 1f else 2f
                        )
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (memo != null) {
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(ColorProvider(palette.buttonBackground))
                                .cornerRadius(8.dp)
                                .padding(horizontal = 6.dp, vertical = if (compact) 1.dp else 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_note),
                                contentDescription = "메모",
                                modifier = GlanceModifier.size(12.dp),
                                colorFilter = ColorFilter.tint(ColorProvider(palette.memoDot))
                            )
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Text(
                                memo,
                                maxLines = if (compact) 1 else 2,
                                style = TextStyle(
                                    fontSize = (10 * style.memoFontScale).sp,
                                    color = ColorProvider(palette.textPrimary)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
