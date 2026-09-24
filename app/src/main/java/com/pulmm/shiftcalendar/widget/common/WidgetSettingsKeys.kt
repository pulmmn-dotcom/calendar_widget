package com.pulmm.shiftcalendar.widget.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object WidgetSettingsKeys {
    val BG_COLOR_ARGB = intPreferencesKey("bg_color_argb")
    val OPACITY = floatPreferencesKey("opacity")
    val DATE_FONT_SCALE = floatPreferencesKey("date_font_scale")
    val SHIFT_FONT_SCALE = floatPreferencesKey("shift_font_scale")
    val MEMO_FONT_SCALE = floatPreferencesKey("memo_font_scale")
    val SHOW_LUNAR = booleanPreferencesKey("show_lunar")
    val WEEK_START_MONDAY = booleanPreferencesKey("week_start_monday")
    val MONTH_OFFSET = intPreferencesKey("month_offset")
}
