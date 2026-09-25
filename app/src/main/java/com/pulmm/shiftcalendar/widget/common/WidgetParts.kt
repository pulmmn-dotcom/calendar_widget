package com.pulmm.shiftcalendar.widget.common

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.R

/**
 * 근무 이름 알약 배지.
 * solid=false: 근무색 15% 배경 + 어둡게 만든 근무색 글자 (기본).
 * solid=true: 근무색 배경 + 흰 글자.
 */
@Composable
fun WidgetShiftPill(
    name: String,
    colorArgb: Int,
    style: WidgetStyle,
    modifier: GlanceModifier = GlanceModifier,
    solid: Boolean = false,
    baseFontSp: Float = 8f
) {
    val shift = Color(colorArgb)
    val palette = widgetPalette(style)
    val bg = if (solid) shift else shift.copy(alpha = 0.15f)
    val fg = if (solid) Color.White else palette.pillText(colorArgb)
    Box(
        modifier = modifier
            .background(ColorProvider(bg))
            .cornerRadius(6.dp)
            .padding(horizontal = 3.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name,
            maxLines = 1,
            style = TextStyle(
                fontSize = (baseFontSp * style.shiftFontScale).sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(fg)
            )
        )
    }
}

/** 둥근 아이콘 버튼 (탭 영역 포함). tonal=true면 연한 원형 배경을 깐다. */
@Composable
fun WidgetIconButton(
    iconRes: Int,
    description: String,
    palette: WidgetPalette,
    action: Action,
    tonal: Boolean = true,
    boxSize: Dp = 26.dp,
    iconSize: Dp = 16.dp,
    iconColor: Color = palette.buttonIcon
) {
    var mod = GlanceModifier.size(boxSize).cornerRadius(boxSize / 2)
    if (tonal) mod = mod.background(ColorProvider(palette.buttonBackground))
    Box(
        modifier = mod.clickable(action).semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = description,
            modifier = GlanceModifier.size(iconSize),
            colorFilter = ColorFilter.tint(ColorProvider(iconColor))
        )
    }
}

/** 헤더 오른쪽의 새로고침 + 설정 버튼. */
@Composable
fun WidgetHeaderButtons(
    palette: WidgetPalette,
    appWidgetId: Int,
    configActivityClass: Class<out Activity>
) {
    val context = LocalContext.current
    val configIntent = Intent()
        .setClassName(context.packageName, configActivityClass.name)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // 위젯마다 서로 다른 PendingIntent가 되도록 data를 붙인다.
        .setData(Uri.parse("shiftcalendar://widget-config/$appWidgetId"))
    Row(verticalAlignment = Alignment.CenterVertically) {
        WidgetIconButton(
            iconRes = R.drawable.ic_widget_refresh,
            description = "새로고침",
            palette = palette,
            action = actionRunCallback<RefreshWidgetAction>()
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        WidgetIconButton(
            iconRes = R.drawable.ic_widget_settings,
            description = "위젯 설정",
            palette = palette,
            action = actionStartActivity(configIntent)
        )
    }
}

/** ↻ 버튼: 모든 위젯을 다시 그린다. */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WidgetRefresher.refreshAll(context)
    }
}
