package com.pulmm.shiftcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pulmm.shiftcalendar.ui.calendar.CalendarScreen
import com.pulmm.shiftcalendar.ui.pattern.PatternScreen
import com.pulmm.shiftcalendar.ui.shifttype.ShiftTypeScreen
import com.pulmm.shiftcalendar.ui.widgethelp.WidgetHelpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { MainScreen() }
        }
    }
}

private enum class Tab(val label: String) { CALENDAR("달력"), SHIFT_TYPE("근무종류"), PATTERN("패턴"), WIDGET_HELP("위젯추가") }

@Composable
private fun MainScreen() {
    var selectedTab by remember { mutableStateOf(Tab.CALENDAR) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.CALENDAR,
                    onClick = { selectedTab = Tab.CALENDAR },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text(Tab.CALENDAR.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.SHIFT_TYPE,
                    onClick = { selectedTab = Tab.SHIFT_TYPE },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text(Tab.SHIFT_TYPE.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.PATTERN,
                    onClick = { selectedTab = Tab.PATTERN },
                    icon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                    label = { Text(Tab.PATTERN.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.WIDGET_HELP,
                    onClick = { selectedTab = Tab.WIDGET_HELP },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = null) },
                    label = { Text(Tab.WIDGET_HELP.label) }
                )
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
