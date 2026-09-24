package com.pulmm.shiftcalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.data.ShiftRepository

@Composable
fun rememberRepository(): ShiftRepository {
    val context = LocalContext.current
    return (context.applicationContext as ShiftCalendarApp).repository
}
