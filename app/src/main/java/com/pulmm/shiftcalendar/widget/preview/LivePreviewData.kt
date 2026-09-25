package com.pulmm.shiftcalendar.widget.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.ui.rememberRepository
import java.time.LocalDate
import java.time.YearMonth

/**
 * 설정 화면 미리보기에 쓸 데이터와, 그것이 예시(샘플)인지 여부.
 * isSample이 true이면 등록된 근무가 없어서 예시 근무표를 대신 보여주는 중이다.
 */
class LivePreview(val data: PreviewData, val isSample: Boolean)

private fun compactLunar(date: LocalDate): String? = try {
    LunarConverter.toLunar(date)?.let { "${if (it.isLeapMonth) "윤" else ""}${it.month}.${it.day}" }
} catch (e: Exception) {
    null
}

/**
 * 저장소의 실제 근무, 메모로 만든 미리보기 데이터. 지난달 1일부터 다음달 말일까지를 읽는다.
 * 근무 종류가 없거나 그 기간에 근무가 하나도 나오지 않으면(처음 설치한 앱) 예시 데이터로 대신한다.
 */
@Composable
fun rememberLivePreview(): LivePreview {
    val repository = rememberRepository()
    val today = remember { LocalDate.now() }
    val range = remember(today) {
        val month = YearMonth.from(today)
        month.minusMonths(1).atDay(1).toEpochDay() to month.plusMonths(1).atEndOfMonth().toEpochDay()
    }
    val infoFlow = remember(range) { repository.observeDayInfoRange(range.first, range.second) }
    val shiftTypes by repository.observeShiftTypes().collectAsState(initial = null)
    val dayInfos by infoFlow.collectAsState(initial = null)

    return remember(shiftTypes, dayInfos, today) {
        val types = shiftTypes
        val infos = dayInfos
        val sample = samplePreviewData(today)
        if (types == null || infos == null) {
            // 읽는 중에는 예시를 잠깐 보여준다. (예시 안내 문구는 띄우지 않는다)
            return@remember LivePreview(sample, isSample = false)
        }
        val typeById = types.associateBy { it.id }
        val shiftByEpochDay = HashMap<Long, PreviewShift>()
        val infoByEpochDay = HashMap<Long, DayInfo>()
        for (info in infos) {
            infoByEpochDay[info.epochDay] = info
            val type = info.shiftTypeId?.let { typeById[it] }
            if (type != null) shiftByEpochDay[info.epochDay] = PreviewShift(type.name, type.colorArgb)
        }
        if (types.isEmpty() || shiftByEpochDay.isEmpty()) {
            return@remember LivePreview(sample, isSample = true)
        }
        // 음력은 그릴 때 필요한 날만 계산해서 기억해 둔다. (LunarConverter는 한 번에 하나씩만 호출)
        val lunarCache = HashMap<Long, String?>()
        val data = PreviewData(today) { date ->
            val epochDay = date.toEpochDay()
            val info = infoByEpochDay[epochDay] ?: return@PreviewData null
            PreviewDay(
                date = date,
                shift = shiftByEpochDay[epochDay],
                memoText = info.memoText,
                lunarText = lunarCache.getOrPut(epochDay) { compactLunar(date) }
            )
        }
        LivePreview(data, isSample = false)
    }
}

/** [rememberLivePreview]의 데이터만 돌려주는 짧은 버전. */
@Composable
fun rememberLivePreviewData(): PreviewData = rememberLivePreview().data
