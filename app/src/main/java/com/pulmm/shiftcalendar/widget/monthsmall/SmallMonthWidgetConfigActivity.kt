package com.pulmm.shiftcalendar.widget.monthsmall

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.pulmm.shiftcalendar.widget.common.DEFAULT_WIDGET_STYLE
import com.pulmm.shiftcalendar.widget.common.WidgetConfigContent
import com.pulmm.shiftcalendar.widget.common.applyStyle
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import com.pulmm.shiftcalendar.widget.preview.PreviewType
import kotlinx.coroutines.launch

class SmallMonthWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val glanceId: GlanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        setContent {
            var initialStyle by remember { mutableStateOf(DEFAULT_WIDGET_STYLE) }
            LaunchedEffect(appWidgetId) {
                initialStyle = getAppWidgetState(this@SmallMonthWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId).toWidgetStyle()
            }

            WidgetConfigContent(
                initialStyle = initialStyle,
                showLunarOption = false,
                title = "작은한달위젯 설정",
                previewType = PreviewType.SMALL_MONTH,
                onCancel = { finish() }
            ) { style ->
                lifecycleScope.launch {
                    updateAppWidgetState(this@SmallMonthWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().applyStyle(style).toPreferences()
                    }
                    SmallMonthWidget().update(this@SmallMonthWidgetConfigActivity, glanceId)
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}
