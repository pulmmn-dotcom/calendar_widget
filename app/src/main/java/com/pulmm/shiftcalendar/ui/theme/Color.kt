package com.pulmm.shiftcalendar.ui.theme

import androidx.compose.ui.graphics.Color

// DESIGN.md (Shift Rhythm Mobile) 색상 토큰

val Surface = Color(0xFFF8F9FF)
val SurfaceDim = Color(0xFFCBDBF5)
val SurfaceBright = Color(0xFFF8F9FF)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFEFF4FF)
val SurfaceContainer = Color(0xFFE5EEFF)
val SurfaceContainerHigh = Color(0xFFDCE9FF)
val SurfaceContainerHighest = Color(0xFFD3E4FE)
val OnSurface = Color(0xFF0B1C30)
val OnSurfaceVariant = Color(0xFF444651)
val InverseSurface = Color(0xFF213145)
val InverseOnSurface = Color(0xFFEAF1FF)
val Outline = Color(0xFF757682)
val OutlineVariant = Color(0xFFC5C5D3)
val SurfaceTint = Color(0xFF4059AA)

val PrimaryDeep = Color(0xFF00236F)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF1E3A8A)
val OnPrimaryContainer = Color(0xFF90A8FF)
val InversePrimary = Color(0xFFB6C4FF)

val Secondary = Color(0xFF006A61)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF86F2E4)
val OnSecondaryContainer = Color(0xFF006F66)

val Tertiary = Color(0xFF4B1C00)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF6E2C00)
val OnTertiaryContainer = Color(0xFFF39461)

val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

val PrimaryFixed = Color(0xFFDCE1FF)
val PrimaryFixedDim = Color(0xFFB6C4FF)
val OnPrimaryFixed = Color(0xFF00164E)
val OnPrimaryFixedVariant = Color(0xFF264191)

val Background = Color(0xFFF8F9FF)
val OnBackground = Color(0xFF0B1C30)
val SurfaceVariant = Color(0xFFD3E4FE)

/** 달력/카드 관련 보조 색 (DESIGN.md calendar-*, surface-*, border-subtle) */
object CalendarColors {
    val sunday = Color(0xFFDC2626)
    val saturday = Color(0xFF2563EB)
    val todayBg = Color(0xFFEFF6FF)
    val todayRing = Color(0xFF3B82F6)
    val memoDot = Color(0xFFF59E0B)
    val otherMonthText = Color(0xFF94A3B8)
    val borderSubtle = Color(0xFFE2E8F0)
    val gridLine = Color(0xFFF1F5F9)
    val surfaceCard = Color(0xFFFFFFFF)
    val surfaceBg = Color(0xFFF8FAFC)
    val surfaceSheet = Color(0xFFFFFFFF)
}

/** 근무 8색. 근무 종류 색 선택 팔레트, Room colorArgb 저장용 ARGB Int. */
object ShiftPalette {
    private data class Entry(val name: String, val argb: Int)

    private val entries = listOf(
        Entry("주간", 0xFF2563EB.toInt()),
        Entry("오후", 0xFFF59E0B.toInt()),
        Entry("야간", 0xFF7C3AED.toInt()),
        Entry("휴무", 0xFF64748B.toInt()),
        Entry("당직", 0xFFE11D48.toInt()),
        Entry("연차", 0xFF10B981.toInt()),
        Entry("대기", 0xFF06B6D4.toInt()),
        Entry("교육", 0xFFEC4899.toInt())
    )

    /** 8색 ARGB 목록 (주간, 오후, 야간, 휴무, 당직, 연차, 대기, 교육 순서). */
    val colors: List<Int> = entries.map { it.argb }

    /** 8색 한글 이름 목록 (colors와 같은 순서). */
    val names: List<String> = entries.map { it.name }

    /** 팔레트에 있는 색이면 한글 이름, 아니면 null. */
    fun nameOf(argb: Int): String? = entries.firstOrNull { it.argb == argb }?.name
}
