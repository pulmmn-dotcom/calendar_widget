package com.pulmm.shiftcalendar.widget.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetFitTest {
    private fun style(date: Float = 1f, shift: Float = 1f, memo: Float = 1f) =
        DEFAULT_WIDGET_STYLE.copy(dateFontScale = date, shiftFontScale = shift, memoFontScale = memo)

    @Test
    fun `하루 위젯 기본 배율은 가장 작은 칸에서도 줄어들지 않는다`() {
        val fitted = WidgetFit.day(style(), 110f, 110f, true, "금요일", "9.25", "주간", 14f, "정기검진")
        assertEquals(style(), fitted)
    }

    @Test
    fun `하루 위젯 날짜 배율 1점6은 작은 칸에서 숫자가 들어가는 크기로 줄어든다`() {
        val fitted = WidgetFit.day(style(date = 1.6f), 110f, 110f, true, "금요일", "9.25", "주간", 14f, null)
        assertTrue(fitted.dateFontScale < 1.6f)
        assertTrue(28f * fitted.dateFontScale * WidgetFit.em("9.25") <= 110f - 20f - 23f)
    }

    @Test
    fun `위젯이 충분히 크면 큰 배율도 그대로 쓴다`() {
        val fitted = WidgetFit.day(style(1.6f, 1.6f, 1.6f), 176f, 223f, false, "금요일", "9.25", "주간", 18f, "정기검진")
        assertEquals(style(1.6f, 1.6f, 1.6f), fitted)
    }

    @Test
    fun `1점0 이하로 고른 배율은 0점7 아래로 줄이지 않는다`() {
        val fitted = WidgetFit.day(style(0.7f, 1f, 1f), 60f, 60f, true, "금요일 (음 8.15)", "12.31", "주간", 14f, "정기검진")
        assertTrue(fitted.dateFontScale >= 0.7f - 0.0001f)
        assertTrue(fitted.shiftFontScale >= 0.7f - 0.0001f)
    }
}
