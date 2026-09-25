package com.pulmm.shiftcalendar.widget.common

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

data class WidgetStyle(
    val bgColorArgb: Int,
    val opacity: Float,
    val dateFontScale: Float,
    val shiftFontScale: Float,
    val memoFontScale: Float,
    val showLunar: Boolean,
    val weekStartMonday: Boolean
)

val DEFAULT_WIDGET_STYLE = WidgetStyle(
    bgColorArgb = 0xFFFFFFFF.toInt(),
    opacity = 1f,
    dateFontScale = 1f,
    shiftFontScale = 1f,
    memoFontScale = 1f,
    showLunar = false,
    weekStartMonday = false
)

fun Preferences.toWidgetStyle(): WidgetStyle = WidgetStyle(
    bgColorArgb = this[WidgetSettingsKeys.BG_COLOR_ARGB] ?: DEFAULT_WIDGET_STYLE.bgColorArgb,
    opacity = this[WidgetSettingsKeys.OPACITY] ?: DEFAULT_WIDGET_STYLE.opacity,
    dateFontScale = this[WidgetSettingsKeys.DATE_FONT_SCALE] ?: DEFAULT_WIDGET_STYLE.dateFontScale,
    shiftFontScale = this[WidgetSettingsKeys.SHIFT_FONT_SCALE] ?: DEFAULT_WIDGET_STYLE.shiftFontScale,
    memoFontScale = this[WidgetSettingsKeys.MEMO_FONT_SCALE] ?: DEFAULT_WIDGET_STYLE.memoFontScale,
    showLunar = this[WidgetSettingsKeys.SHOW_LUNAR] ?: DEFAULT_WIDGET_STYLE.showLunar,
    weekStartMonday = this[WidgetSettingsKeys.WEEK_START_MONDAY] ?: DEFAULT_WIDGET_STYLE.weekStartMonday
)

fun MutablePreferences.applyStyle(style: WidgetStyle): MutablePreferences = apply {
    this[WidgetSettingsKeys.BG_COLOR_ARGB] = style.bgColorArgb
    this[WidgetSettingsKeys.OPACITY] = style.opacity
    this[WidgetSettingsKeys.DATE_FONT_SCALE] = style.dateFontScale
    this[WidgetSettingsKeys.SHIFT_FONT_SCALE] = style.shiftFontScale
    this[WidgetSettingsKeys.MEMO_FONT_SCALE] = style.memoFontScale
    this[WidgetSettingsKeys.SHOW_LUNAR] = style.showLunar
    this[WidgetSettingsKeys.WEEK_START_MONDAY] = style.weekStartMonday
}
