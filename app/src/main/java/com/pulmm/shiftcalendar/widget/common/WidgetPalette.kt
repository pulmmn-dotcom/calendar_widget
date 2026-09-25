package com.pulmm.shiftcalendar.widget.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 위젯 배경색(사용자 선택)에 따라 글자색을 자동으로 고르는 팔레트.
 * 밝은 배경이면 어두운 글자, 어두운 배경이면 밝은 글자를 쓴다. (투명도는 판단에 넣지 않는다)
 */
data class WidgetPalette(
    val isDark: Boolean,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDimmed: Color,
    val sunday: Color,
    val saturday: Color,
    val memoDot: Color,
    val todayCircle: Color,
    val todayText: Color,
    val buttonBackground: Color,
    val buttonIcon: Color
) {
    /** 알약 배지 글자색: 밝은 배경에서는 근무색을 어둡게, 어두운 배경에서는 밝게 만든 색. */
    fun pillText(shiftColorArgb: Int): Color = pillTextColor(shiftColorArgb, isDark)
}

private const val DARK_BG_LUMINANCE_THRESHOLD = 0.4f

fun isDarkBackground(bgColorArgb: Int): Boolean =
    Color(bgColorArgb).copy(alpha = 1f).luminance() < DARK_BG_LUMINANCE_THRESHOLD

fun widgetPalette(style: WidgetStyle): WidgetPalette = widgetPalette(isDarkBackground(style.bgColorArgb))

fun widgetPalette(isDark: Boolean): WidgetPalette =
    if (isDark) {
        WidgetPalette(
            isDark = true,
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFFCBD5E1),
            textDimmed = Color(0xFF64748B),
            sunday = Color(0xFFF87171),
            saturday = Color(0xFF60A5FA),
            memoDot = Color(0xFFF59E0B),
            todayCircle = Color(0xFF2563EB),
            todayText = Color.White,
            buttonBackground = Color(0x29FFFFFF),
            buttonIcon = Color(0xFFE2E8F0)
        )
    } else {
        WidgetPalette(
            isDark = false,
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF64748B),
            textDimmed = Color(0xFFCBD5E1),
            sunday = Color(0xFFDC2626),
            saturday = Color(0xFF2563EB),
            memoDot = Color(0xFFF59E0B),
            todayCircle = Color(0xFF2563EB),
            todayText = Color.White,
            buttonBackground = Color(0xFFF1F5F9),
            buttonIcon = Color(0xFF475569)
        )
    }

/** 밝은 배경: RGB를 0.68배로 어둡게. 어두운 배경: 흰색 쪽으로 45% 섞어 밝게. */
fun pillTextColor(shiftColorArgb: Int, isDark: Boolean): Color {
    val c = Color(shiftColorArgb)
    return if (isDark) {
        Color(
            red = c.red + (1f - c.red) * 0.45f,
            green = c.green + (1f - c.green) * 0.45f,
            blue = c.blue + (1f - c.blue) * 0.45f
        )
    } else {
        Color(red = c.red * 0.68f, green = c.green * 0.68f, blue = c.blue * 0.68f)
    }
}
