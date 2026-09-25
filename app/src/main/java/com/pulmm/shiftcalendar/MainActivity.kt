package com.pulmm.shiftcalendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pulmm.shiftcalendar.ui.calendar.CalendarScreen
import com.pulmm.shiftcalendar.ui.pattern.PatternScreen
import com.pulmm.shiftcalendar.ui.shifttype.ShiftTypeScreen
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import com.pulmm.shiftcalendar.ui.theme.ShiftCalendarTheme
import com.pulmm.shiftcalendar.ui.widgethelp.WidgetHelpScreen
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startTab = if (intent?.getStringExtra(EXTRA_START_TAB) == START_TAB_PATTERN) Tab.PATTERN else Tab.CALENDAR
        val startMonth = intent?.getStringExtra(EXTRA_START_MONTH)
            ?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
        setContent {
            ShiftCalendarTheme { MainScreen(startTab, startMonth) }
        }
    }

    companion object {
        /** 앱을 열 때 처음 보여줄 탭을 정하는 값. 없으면 달력. */
        const val EXTRA_START_TAB = "tab"
        const val START_TAB_PATTERN = "pattern"

        /** 달력 탭을 처음 열 때 보여줄 달("2026-10" 형식). 없으면 이번 달. */
        const val EXTRA_START_MONTH = "month"

        /** 위젯에서 앱의 달력을 [month]로 여는 인텐트. 이미 떠 있는 앱은 새로 열어 그 달로 맞춘다. */
        fun calendarIntent(context: Context, month: YearMonth): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_START_MONTH, month.toString())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}

private enum class Tab(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    CALENDAR("달력", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    SHIFT_TYPE("근무 종류", Icons.Outlined.Palette, Icons.Filled.Palette),
    PATTERN("근무 패턴", Icons.Outlined.Autorenew, Icons.Filled.Autorenew),
    WIDGET_HELP("위젯 안내", Icons.Outlined.Widgets, Icons.Filled.Widgets)
}

@Composable
private fun MainScreen(startTab: Tab, startMonth: YearMonth?) {
    var selectedTab by remember { mutableStateOf(startTab) }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = CalendarColors.borderSubtle)
                NavigationBar(containerColor = CalendarColors.surfaceCard, tonalElevation = 0.dp) {
                    Tab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.outline,
                                unselectedTextColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            when (selectedTab) {
                Tab.CALENDAR -> CalendarScreen(startMonth)
                Tab.SHIFT_TYPE -> ShiftTypeScreen()
                Tab.PATTERN -> PatternScreen()
                Tab.WIDGET_HELP -> WidgetHelpScreen()
            }
        }
    }
}
