package com.pulmm.shiftcalendar.widget.common

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * 위젯 글자 크기 배율(0.7~1.6)이 위젯 크기보다 커서 글자가 잘리는 것을 막는 계산.
 *
 * 위젯(Glance)은 자기 크기를 알고 있으니 "이 크기에서 이 배율이 들어가는가"를 어림해 보고,
 * 안 들어가면 배율을 줄여서 그린다(저장된 설정값은 그대로 두고 화면에 쓰는 값만 줄인다).
 * 미리보기도 같은 함수를 쓰므로 실제 위젯과 똑같이 줄어든다.
 *
 * 줄이는 순서: 1) 1.0을 넘는 배율을 1.0쪽으로 줄인다  2) 그래도 안 들어가면 전부 0.7까지 같이 줄인다.
 * 1.0 이하로 고른 배율은 1단계에서는 건드리지 않는다.
 */
object WidgetFit {
    /** 글자 줄 높이 배율. 한글 글꼴은 글자 크기의 약 1.4배 높이를 차지한다(에뮬레이터에서 실제로 잰 값). */
    const val LINE = 1.43f

    /** 숫자만 있는 줄의 높이 배율. */
    private const val NUMERAL_LINE = 1.2f

    /** 줄여도 이 아래로는 내려가지 않는다. */
    private const val FLOOR = 0.7f

    /** 글자 높이 어림이 조금 모자라도 안 잘리게 남겨 두는 여유(dp). */
    private const val HEIGHT_MARGIN = 3f

    /** 글자 폭 어림(글자 크기의 몇 배인가). 한글/한자는 1.0, 숫자는 0.6, 점/공백/괄호는 좁게. */
    fun em(text: String): Float {
        var sum = 0f
        for (c in text) {
            sum += when {
                c.code >= 0x1100 -> 1.0f
                c.isDigit() -> 0.6f
                c == '.' -> 0.3f
                c == ' ' -> 0.28f
                c == '(' || c == ')' -> 0.35f
                else -> 0.62f
            }
        }
        return sum
    }

    // 어느 배율 때문에 안 들어가는지 나타내는 표시(여러 개를 더해서 쓴다).
    const val DATE = 1
    const val SHIFT = 2
    const val MEMO = 4

    /** 높이가 모자란 경우: 어느 한 배율 탓이 아니므로 가장 큰 배율부터 줄인다. */
    const val HEIGHT = 8

    /**
     * check가 0을 돌려주면(모두 들어감) 그 스타일을 쓴다. 아니면 문제가 된 배율을 0.05씩 줄여 다시 확인한다.
     * 높이 문제는 가장 큰 배율부터 줄인다. 1.0 이하로 고른 배율은 0.7 아래로 내리지 않고, 1.0을 넘는 배율은 0.7까지 줄일 수 있다.
     */
    fun fit(style: WidgetStyle, check: (WidgetStyle) -> Int): WidgetStyle {
        var d = style.dateFontScale
        var s = style.shiftFontScale
        var m = style.memoFontScale
        val fd = min(d, FLOOR)
        val fs = min(s, FLOOR)
        val fm = min(m, FLOOR)
        var current = style
        repeat(100) {
            current = style.copy(dateFontScale = d, shiftFontScale = s, memoFontScale = m)
            val v = check(current)
            if (v == 0) return current
            var dv = v and DATE != 0
            var sv = v and SHIFT != 0
            var mv = v and MEMO != 0
            if (v and HEIGHT != 0) {
                val top = max(d, max(s, m))
                if (d == top) dv = true else if (s == top) sv = true else mv = true
            }
            var moved = false
            if (dv && d > fd + 0.001f) { d = max(fd, d - 0.05f); moved = true }
            if (sv && s > fs + 0.001f) { s = max(fs, s - 0.05f); moved = true }
            if (mv && m > fm + 0.001f) { m = max(fm, m - 0.05f); moved = true }
            if (!moved) return current
        }
        return current
    }

    // ---------------------------------------------------------------- 하루 위젯

    /**
     * 하루 위젯. weekdayLine은 "금요일" 또는 "금요일 (음 8.15)", numeral은 "9.25",
     * pillName/pillBaseSp는 근무 알약 글자, memo는 오늘 메모(없으면 null).
     */
    fun day(
        style: WidgetStyle, widthDp: Float, heightDp: Float, compact: Boolean,
        weekdayLine: String, numeral: String, pillName: String, pillBaseSp: Float, memo: String?
    ): WidgetStyle = fit(style) { st ->
        val availW = widthDp - 20f
        val availH = heightDp - 20f - HEIGHT_MARGIN
        val leftW = availW - 22f - 1f          // 오른쪽 버튼 상자(22) 옆 자리
        val ds = st.dateFontScale
        var v = 0
        if (28f * ds * em(numeral) > leftW) v = v or DATE
        if (compact) {
            if (10f * ds * em("오늘") + 4f + 10f * ds * em(weekdayLine) > leftW) v = v or DATE
        } else {
            if (10f * ds * em("오늘") > leftW || 11f * ds * em(weekdayLine) > availW) v = v or DATE
        }
        if (pillBaseSp * st.shiftFontScale * em(pillName) + 20f > availW) v = v or SHIFT

        val memoLines = if (memo == null) 0 else {
            val memoW = availW - 12f - 12f - 4f
            val need = ceil(10f * st.memoFontScale * em(memo) / memoW).toInt().coerceAtLeast(1)
            min(need, if (compact) 1 else 2)
        }
        val memoH = if (memo == null) 0f
        else 10f * st.memoFontScale * LINE * memoLines + 2f * (if (compact) 1f else 4f)
        val pillH = pillBaseSp * st.shiftFontScale * LINE + 2f * (if (compact) 1f else 2f)
        // 숫자 줄은 글자 크기의 1.2배, 한글 줄은 1.43배쯤 차지한다.
        val leftH = 10f * ds * LINE + 28f * ds * NUMERAL_LINE
        val topH = if (compact) max(leftH, 48f) else max(leftH, 48f) + 11f * ds * LINE
        if (topH + pillH + memoH > availH) v = v or HEIGHT
        v
    }

    // ---------------------------------------------------------------- 일주일 위젯

    /** 일주일 위젯 한 칸(하루)에 그려질 내용. */
    data class WeekDay(
        val isToday: Boolean,
        val shiftName: String?,
        val shiftDeleted: Boolean,
        val memo: String?,
        val hasLunar: Boolean
    )

    fun week(style: WidgetStyle, widthDp: Float, heightDp: Float, compact: Boolean, days: List<WeekDay>): WidgetStyle =
        fit(style) { st ->
            val availW = widthDp - 20f
            val availH = heightDp - 20f - HEIGHT_MARGIN
            val colW = availW / 7f - 2f
            val headerH = if (compact) 20f + 1f else 24f + 4f
            val colH = availH - headerH
            val ds = st.dateFontScale
            var v = 0
            for (d in days) {
                val w = if (d.isToday) colW - 3f else colW
                if (10f * ds * em(if (d.isToday) "오늘" else "월") > w) v = v or DATE
                if (14f * ds * em("30") > w) v = v or DATE
                var h = (if (d.isToday) 3f else 0f) + 2f * (if (compact) 1f else 3f) +
                    10f * ds * LINE + 14f * ds * NUMERAL_LINE + (if (compact) 1f else 2f)
                if (d.shiftName != null) {
                    val base = if (d.shiftDeleted) 8f else 10f
                    if (base * st.shiftFontScale * em(d.shiftName) > w - 6f) v = v or SHIFT
                    h += base * st.shiftFontScale * LINE + 4f
                } else {
                    h += 15f
                }
                if (d.memo != null) {
                    if (9f * st.memoFontScale * em(d.memo.take(3)) > w) v = v or MEMO
                    h += 9f * st.memoFontScale * LINE
                }
                if (d.hasLunar) h += 8f * LINE
                if (h > colH) v = v or HEIGHT
            }
            v
        }

    // ---------------------------------------------------------------- 작은 한달 위젯

    /**
     * 작은 한달 위젯: 날짜 숫자 원(높이가 빠듯하면 위젯 안에서 이미 줄어든 크기)과 그 옆 메모 점이
     * 칸 폭 밖으로 나가 잘리지 않게 한다. rowHeightDp는 한 주 줄의 높이.
     */
    fun smallMonth(style: WidgetStyle, widthDp: Float, rootPaddingDp: Float, rowHeightDp: Float): WidgetStyle =
        fit(style) { st ->
            val cellW = (widthDp - 2f * rootPaddingDp) / 7f - 2f
            val tight = rowHeightDp < 26f
            val barH = if (tight) 2f else 3f
            val room = if (tight) rowHeightDp - 2f else rowHeightDp - 2f - barH - 1f
            val circle = min(20f * st.dateFontScale, max(room, 12f))
            // 메모 점은 원 오른쪽 위에 5dp 튀어나온다. 대부분(약 80%)은 보이도록 여유를 6dp만 잡는다.
            if (circle + 6f > cellW) DATE else 0
        }

    // ---------------------------------------------------------------- 한달 위젯

    /**
     * 한달 위젯. 이 위젯은 기본 크기(250 x 250dp, 5주)에서 이미 칸이 빠듯하게 맞으므로,
     * 미리보기의 실제 배치(글자 높이 1.2배, 칸 안쪽 여백 위아래 1dp씩)와 똑같이 계산한다.
     * 넘침을 1dp만 넘겨도 근무 알약 글자 아래쪽이 잘려 보이므로 여유 없이 맞춘다
     * (그래서 250 x 250dp 5주 달의 기본 날짜 배율은 0.95쯤으로 살짝 줄어든다).
     */
    fun month(
        style: WidgetStyle, widthDp: Float, heightDp: Float, weekCount: Int,
        shiftNames: List<Pair<String, Boolean>>, // (이름, 삭제됨 여부)
        hasAnyShift: Boolean
    ): WidgetStyle = fit(style) { st ->
        val availW = widthDp - 20f
        val availH = heightDp - 20f
        val cellW = availW / 7f - 2f
        val ds = st.dateFontScale
        val weekdayH = 11f * ds * 1.2f + 4f
        val cellH = (availH - 32f - weekdayH) / weekCount
        var v = 0
        if (20f * ds > cellW || 11f * ds * em("월") > availW / 7f) v = v or DATE
        for ((name, deleted) in shiftNames) {
            val base = if (deleted) 8f else 9f
            if (base * st.shiftFontScale * em(name) > cellW - 8f) v = v or SHIFT
        }
        val need = 2f + if (hasAnyShift) 20f * ds + 9f * st.shiftFontScale * 1.2f + 4f else 20f * ds
        if (need > cellH) v = v or HEIGHT
        v
    }
}
