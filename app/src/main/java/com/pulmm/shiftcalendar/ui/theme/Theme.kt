package com.pulmm.shiftcalendar.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// primary는 DESIGN.md의 눈에 보이는 브랜드 남색인 primary-container(#1E3A8A)를 사용한다.
// 선택 알약 표시(NavigationBar indicator 등)는 secondaryContainer를 primary-fixed(연한 남색 톤)로 매핑한다.
// 배경은 CalendarColors.surfaceBg(#F8FAFC), 시트/다이얼로그/카드 계열 컨테이너는 흰색.
private val ShiftLightColorScheme = lightColorScheme(
    primary = PrimaryContainer,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryFixed,
    onPrimaryContainer = OnPrimaryFixed,
    inversePrimary = InversePrimary,
    secondary = OnPrimaryFixedVariant,
    onSecondary = OnPrimary,
    secondaryContainer = PrimaryFixed,
    onSecondaryContainer = PrimaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = CalendarColors.surfaceBg,
    onBackground = OnBackground,
    surface = CalendarColors.surfaceBg,
    onSurface = OnSurface,
    surfaceVariant = CalendarColors.borderSubtle,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = CalendarColors.borderSubtle,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainerLowest = CalendarColors.surfaceCard,
    surfaceContainerLow = CalendarColors.surfaceSheet,
    surfaceContainer = CalendarColors.surfaceCard,
    surfaceContainerHigh = CalendarColors.surfaceSheet,
    surfaceContainerHighest = SurfaceContainer
)

private val ShiftShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

// 기본 폰트 유지(Inter 미사용). 크기/굵기/줄높이만 DESIGN.md typography에 맞춘다.
private val ShiftTypography = Typography(
    displayLarge = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    displayMedium = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    displaySmall = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    headlineLarge = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    headlineMedium = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.015).em),
    headlineSmall = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.015).em),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.01.em)
)

/** 달력 칸 안의 근무 태그용 글자 스타일 (DESIGN.md shift-tag: 10.5 굵게). */
val ShiftTagTextStyle = TextStyle(
    fontSize = 10.5.sp,
    lineHeight = 12.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-0.01).em
)

/** 위젯 큰 숫자용 글자 스타일 (DESIGN.md widget-numeral). */
val WidgetNumeralTextStyle = TextStyle(
    fontSize = 28.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.03).em
)

@Composable
fun ShiftCalendarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShiftLightColorScheme,
        shapes = ShiftShapes,
        typography = ShiftTypography,
        content = content
    )
}
