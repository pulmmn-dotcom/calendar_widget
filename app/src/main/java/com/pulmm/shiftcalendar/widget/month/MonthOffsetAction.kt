package com.pulmm.shiftcalendar.widget.month

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.pulmm.shiftcalendar.widget.common.WidgetSettingsKeys

class MonthOffsetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val delta = parameters[DELTA_KEY] ?: 0
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[WidgetSettingsKeys.MONTH_OFFSET] ?: 0
            prefs.toMutablePreferences().apply { this[WidgetSettingsKeys.MONTH_OFFSET] = current + delta }.toPreferences()
        }
        MonthWidget().update(context, glanceId)
    }

    companion object {
        val DELTA_KEY = ActionParameters.Key<Int>("delta")
    }
}
