package com.pulmm.shiftcalendar

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiftCalendarTheme { MainScreen() }
        }
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
private fun MainScreen() {
    var selectedTab by remember { mutableStateOf(Tab.CALENDAR) }

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
                Tab.CALENDAR -> CalendarScreen()
                Tab.SHIFT_TYPE -> ShiftTypeScreen()
                Tab.PATTERN -> PatternScreen()
                Tab.WIDGET_HELP -> WidgetHelpScreen()
            }
        }
    }
}
