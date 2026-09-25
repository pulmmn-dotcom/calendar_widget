package com.pulmm.shiftcalendar.widget.preview

import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.ui.theme.ShiftPalette
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** 미리보기용 근무 한 종류. (DB의 ShiftType 대신 이름과 색만 가진다) */
data class PreviewShift(val name: String, val colorArgb: Int)

/**
 * 미리보기용 하루.
 * lunarText는 위젯 안에 들어가는 짧은 음력 표기 "9.2" (윤달은 "윤9.2"). 하루위젯이 앞에 "음 "을 붙여 쓴다.
 * shift가 null이면 근무 없는 날, memoText가 null이면 메모 없는 날.
 */
data class PreviewDay(
    val date: LocalDate,
    val shift: PreviewShift? = null,
    val memoText: String? = null,
    val lunarText: String? = null
)

/**
 * 미리보기에 그릴 데이터 묶음. DB에 의존하지 않는다.
 * today: "오늘"로 표시할 날짜. dayAt: 날짜로 그날 정보를 찾는 함수(없으면 null).
 * 실제 데이터로 미리보기를 만들 때는 저장소에서 읽은 값으로 dayAt만 채워 넣으면 된다.
 */
class PreviewData(
    val today: LocalDate,
    val dayAt: (LocalDate) -> PreviewDay?
)

private val SAMPLE_DAY = PreviewShift("주간", ShiftPalette.colors[0])
private val SAMPLE_NIGHT = PreviewShift("야간", ShiftPalette.colors[2])
private val SAMPLE_OFF = PreviewShift("휴무", ShiftPalette.colors[3])

// 4조2교대: 주간, 주간, 야간, 야간, 휴무, 휴무 (6일 반복)
private val SAMPLE_CYCLE = listOf(SAMPLE_DAY, SAMPLE_DAY, SAMPLE_NIGHT, SAMPLE_NIGHT, SAMPLE_OFF, SAMPLE_OFF)

private fun compactLunar(date: LocalDate): String? = try {
    LunarConverter.toLunar(date)?.let { "${if (it.isLeapMonth) "윤" else ""}${it.month}.${it.day}" }
} catch (e: Exception) {
    null
}

/**
 * 오늘이 든 달 전후로 보기 좋은 예시 데이터를 만든다.
 * 4조2교대가 반복되고, 메모는 오늘과 그 옆 날(토요일이면 전날) 두 곳, 음력은 실제 변환값이다.
 */
fun samplePreviewData(today: LocalDate = LocalDate.now()): PreviewData {
    val month = YearMonth.from(today)
    val from = month.atDay(1).minusDays(7)
    val to = month.atEndOfMonth().plusDays(7)
    val secondMemoDate =
        if (today.dayOfWeek == DayOfWeek.SATURDAY) today.minusDays(1) else today.plusDays(1)
    val memos = mapOf(today to "정기검진", secondMemoDate to "가족모임")

    val days = HashMap<LocalDate, PreviewDay>()
    var date = from
    while (!date.isAfter(to)) {
        // 오늘이 주기의 세 번째 칸(야간 첫날)이 되도록 맞춘다. 오늘 하루위젯에 "야간"이 보이는 예시.
        val offset = (date.toEpochDay() - today.toEpochDay() + 2).mod(SAMPLE_CYCLE.size.toLong()).toInt()
        days[date] = PreviewDay(
            date = date,
            shift = SAMPLE_CYCLE[offset],
            memoText = memos[date],
            lunarText = compactLunar(date)
        )
        date = date.plusDays(1)
    }
    return PreviewData(today) { days[it] }
}
