package com.pulmm.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import com.pulmm.shiftcalendar.ui.theme.ShiftTagTextStyle

/**
 * 근무 태그 알약 (DESIGN.md 2. Shift Badges & Chips, Display Tag).
 * 배경은 근무 색 15% 투명도, 글자는 근무 색 100%. 한 줄, 넘치면 말줄임.
 */
@Composable
fun ShiftBadge(name: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = name,
        style = ShiftTagTextStyle,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

/** 삭제된 근무 종류를 가리키는 날짜용 회색 테두리 알약. */
@Composable
fun DeletedShiftBadge(modifier: Modifier = Modifier) {
    Text(
        text = "삭제됨",
        style = ShiftTagTextStyle,
        color = CalendarColors.otherMonthText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .border(1.dp, CalendarColors.otherMonthText, CircleShape)
            .padding(horizontal = 6.dp, vertical = 0.dp)
    )
}
