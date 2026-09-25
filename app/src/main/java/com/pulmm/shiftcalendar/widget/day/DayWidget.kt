package com.pulmm.shiftcalendar.widget.day

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.widget.common.MemoEditActivity
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class DayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val style = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetStyle()

        val today = LocalDate.now()
        val dayInfo = repository.getDayInfoOnce(today.toEpochDay())
        val shiftType = repository.getShiftTypesOnce().find { it.id == dayInfo.shiftTypeId }
        val lunar = if (style.showLunar) LunarConverter.toLunar(today) else null

        provideContent {
            WidgetRoot(style) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().clickable(
                        actionStartActivity<MemoEditActivity>(
                            actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to today.toEpochDay())
                        )
                    )
                ) {
                    Text(
                        "${today.monthValue}월 ${today.dayOfMonth}일 ${today.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.KOREAN)}",
                        style = TextStyle(fontSize = (11 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFCCCCCC)))
                    )
                    Text(
                        shiftType?.name ?: "근무 없음",
                        style = TextStyle(
                            fontSize = (22 * style.shiftFontScale).sp,
                            color = ColorProvider(shiftType?.colorArgb?.let { Color(it) } ?: Color.Gray)
                        )
                    )
                    if (!dayInfo.memoText.isNullOrBlank()) {
                        Text(
                            dayInfo.memoText,
                            style = TextStyle(fontSize = (10 * style.memoFontScale).sp, color = ColorProvider(Color(0xFFAAAAAA)))
                        )
                    }
                    if (lunar != null) {
                        Text(lunar.toShortDisplay(), style = TextStyle(fontSize = 8.sp, color = ColorProvider(Color(0xFF888888))))
                    }
                }
            }
        }
    }
}
