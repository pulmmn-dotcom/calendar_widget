package com.pulmm.shiftcalendar.widget.week

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.widget.common.MemoEditActivity
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class WeekWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val style = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetStyle()

        val today = LocalDate.now()
        val weekStart = if (style.weekStartMonday) {
            today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
        } else {
            today.minusDays((today.dayOfWeek.value % 7).toLong())
        }
        val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }
        val dayInfos = repository.observeDayInfoRange(weekStart.toEpochDay(), weekStart.plusDays(6).toEpochDay()).first()
        val shiftTypes = repository.getShiftTypesOnce()
        val dayInfoByEpochDay = dayInfos.associateBy { it.epochDay }
        val shiftTypeById = shiftTypes.associateBy { it.id }
        val lunarByEpochDay = if (style.showLunar) {
            weekDates.associate { d -> d.toEpochDay() to LunarConverter.toLunar(d) }
        } else emptyMap()

        provideContent {
            WidgetRoot(style) {
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    weekDates.forEach { date ->
                        val dayInfo = dayInfoByEpochDay[date.toEpochDay()]
                        val shiftType = dayInfo?.shiftTypeId?.let { shiftTypeById[it] }
                        val lunar = lunarByEpochDay[date.toEpochDay()]
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(2.dp)
                                .clickable(actionStartActivity<MemoEditActivity>(
                                    actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
                                ))
                        ) {
                            Text(
                                date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.KOREAN),
                                style = TextStyle(fontSize = (9 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFAAAAAA)))
                            )
                            Text(
                                "${date.dayOfMonth}",
                                style = TextStyle(fontSize = (13 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFEEEEEE)))
                            )
                            if (shiftType != null) {
                                Text(
                                    shiftType.name,
                                    style = TextStyle(fontSize = (9 * style.shiftFontScale).sp, color = ColorProvider(Color(shiftType.colorArgb)))
                                )
                                Box(modifier = GlanceModifier.fillMaxWidth().height(2.dp).background(ColorProvider(Color(shiftType.colorArgb)))) {}
                            }
                            val memo = dayInfo?.memoText
                            if (!memo.isNullOrBlank()) {
                                Text(
                                    memo.take(2),
                                    style = TextStyle(fontSize = (8 * style.memoFontScale).sp, color = ColorProvider(Color(0xFFF1C40F)))
                                )
                            }
                            if (lunar != null) {
                                Text(lunar.toShortDisplay(), style = TextStyle(fontSize = 7.sp, color = ColorProvider(Color(0xFF888888))))
                            }
                        }
                    }
                }
            }
        }
    }
}
