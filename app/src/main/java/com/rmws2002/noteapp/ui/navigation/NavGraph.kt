package com.rmws2002.noteapp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.ui.screens.HomeScreen
import com.rmws2002.noteapp.ui.screens.NoteEditScreen
import com.rmws2002.noteapp.ui.screens.NoteListScreen
import com.rmws2002.noteapp.ui.screens.ScheduleEditScreen
import com.rmws2002.noteapp.ui.screens.ScheduleScreen
import com.rmws2002.noteapp.ui.screens.SearchScreen
import com.rmws2002.noteapp.ui.screens.SettingsScreen
import com.rmws2002.noteapp.ui.screens.TodoEditScreen
import com.rmws2002.noteapp.ui.screens.TodoListScreen

private val tabLabels = listOf("首页", "笔记", "待办", "日程", "搜索")
private val tabOutlined = listOf(
    Icons.Outlined.Home, Icons.Outlined.Description,
    Icons.Outlined.Checklist, Icons.Outlined.CalendarMonth, Icons.Outlined.Search
)
private val tabFilled = listOf(
    Icons.Filled.Home, Icons.Filled.Description,
    Icons.Filled.Checklist, Icons.Filled.CalendarMonth, Icons.Filled.Search
)

sealed class Overlay {
    data object None : Overlay()
    data class NoteEdit(val id: Long?) : Overlay()
    data class TodoEdit(val id: Long?) : Overlay()
    data object ScheduleEdit : Overlay()
    data object Settings : Overlay()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteAppNavGraph() {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf<Overlay>(Overlay.None) }
    val pager = rememberPagerState(pageCount = { 5 })

    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.collect { tabIndex = it }
    }
    LaunchedEffect(tabIndex) {
        pager.animateScrollToPage(tabIndex)
    }

    val showTitle = tabIndex != 0
    val showBars = overlay is Overlay.None

    val bar: @Composable () -> Unit = if (showBars) {{
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            tabLabels.forEachIndexed { i, label ->
                val sel = tabIndex == i
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (sel) tabFilled[i] else tabOutlined[i],
                            contentDescription = label
                        )
                    },
                    label = { Text(label) },
                    selected = sel,
                    onClick = { tabIndex = i },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }} else {{}}

    Scaffold(bottomBar = bar) { pad ->
        Box(Modifier.padding(pad)) {
            if (overlay is Overlay.None) {
                if (showTitle) {
                    TopAppBar(
                        title = { Text(tabLabels[tabIndex]) },
                        actions = {
                            IconButton(onClick = { overlay = Overlay.Settings }) {
                                Icon(Icons.Filled.Settings, "设置",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize().padding(top = if (showTitle) 56.dp else 0.dp)
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            onNoteClick = { overlay = Overlay.NoteEdit(it) },
                            onTodoClick = { overlay = Overlay.TodoEdit(it) },
                            onNewNote = { overlay = Overlay.NoteEdit(null) },
                            onNewTodo = { overlay = Overlay.TodoEdit(null) },
                            onNewSchedule = { overlay = Overlay.ScheduleEdit }
                        )
                        1 -> NoteListScreen(
                            onAddNote = { overlay = Overlay.NoteEdit(null) },
                            onNoteClick = { overlay = Overlay.NoteEdit(it) }
                        )
                        2 -> TodoListScreen(
                            onAddTodo = { overlay = Overlay.TodoEdit(null) },
                            onTodoClick = { overlay = Overlay.TodoEdit(it) }
                        )
                        3 -> ScheduleScreen(
                            onAddSchedule = { overlay = Overlay.ScheduleEdit }
                        )
                        4 -> SearchScreen(
                            onNoteClick = { overlay = Overlay.NoteEdit(it) },
                            onTodoClick = { overlay = Overlay.TodoEdit(it) }
                        )
                    }
                }
            }
            when (val o = overlay) {
                is Overlay.NoteEdit -> {
                    NoteEditScreen(
                        noteId = o.id,
                        onBack = { overlay = Overlay.None }
                    )
                }
                is Overlay.TodoEdit -> {
                    TodoEditScreen(
                        todoId = o.id,
                        onBack = { overlay = Overlay.None }
                    )
                }
                is Overlay.ScheduleEdit -> {
                    ScheduleEditScreen(
                        onBack = { overlay = Overlay.None }
                    )
                }
                is Overlay.Settings -> {
                    SettingsScreen(
                        onBack = { overlay = Overlay.None }
                    )
                }
                is Overlay.None -> {}
            }
        }
    }
}
