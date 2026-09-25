package com.pulmm.shiftcalendar.ui.pattern

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.theme.CalendarColors

/**
 * 근무 패턴 칩 (DESIGN.md 2/5). 색 점 + 근무 이름 알약, 배경은 근무 색 15% 투명도.
 * shiftType이 null이면(삭제된 근무 종류) 회색 "삭제됨" 칩.
 */
@Composable
fun ShiftPill(shiftType: ShiftType?, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val color = shiftType?.let { Color(it.colorArgb) } ?: CalendarColors.otherMonthText
    val textColor = lerp(color, Color.Black, 0.25f)
    val clickable = if (onClick != null) Modifier.clip(CircleShape).clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .then(clickable)
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = shiftType?.name ?: "삭제됨",
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChainArrow() {
    Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CalendarColors.otherMonthText,
            modifier = Modifier.padding(horizontal = 4.dp).size(14.dp)
        )
    }
}

/** 화살표(→)로 이어 붙인 근무 칩 한 줄. 넘치면 가로 스크롤. */
@Composable
fun ShiftChainRow(items: List<ShiftType?>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, shiftType ->
            if (index > 0) ChainArrow()
            ShiftPill(shiftType)
        }
    }
}

/** 화살표(→)로 이어 붙인 근무 칩. 넘치면 다음 줄로 넘어간다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftChainFlow(items: List<ShiftType?>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, shiftType ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShiftPill(shiftType)
                if (index < items.lastIndex) ChainArrow()
            }
        }
    }
}
