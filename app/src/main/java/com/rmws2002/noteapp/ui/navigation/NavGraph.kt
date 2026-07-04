package com.rmws2002.noteapp.ui.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rmws2002.noteapp.NoteApp
import com.rmws2002.noteapp.ui.screens.CompletedTodosContent
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
    var showCompletedSheet by remember { mutableStateOf(false) }
    val pager = rememberPagerState(pageCount = { 5 })

    // Completed todo count for badge
    val context = LocalContext.current
    val app = context.applicationContext as NoteApp
    val completedCount by app.todoRepository.getCompletedTodos()
        .collectAsState(initial = emptyList())

    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.collect { tabIndex = it }
    }
    LaunchedEffect(tabIndex) {
        pager.animateScrollToPage(tabIndex)
    }

    val showTitle = tabIndex != 0
    val showBars = overlay is Overlay.None

    // ── BackHandler: intercept back when any overlay is visible ──
    BackHandler(enabled = overlay !is Overlay.None || showCompletedSheet) {
        when {
            showCompletedSheet -> showCompletedSheet = false
            overlay !is Overlay.None -> overlay = Overlay.None
        }
    }

    val topBar: @Composable () -> Unit = if (showBars && showTitle) {{
        TopAppBar(
            title = { Text(tabLabels[tabIndex]) },
            actions = {
                // Completed todos button (✓ icon with badge)
                if (completedCount.isNotEmpty()) {
                    IconButton(onClick = { showCompletedSheet = true }) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        "${completedCount.size}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "已完成",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { showCompletedSheet = true }) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = "已完成",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
                // Settings button
                IconButton(onClick = { overlay = Overlay.Settings }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }} else {{}}

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

    Scaffold(
        topBar = topBar,
        bottomBar = bar
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (overlay is Overlay.None) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    Box(Modifier.fillMaxSize()) {
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
            }

            // Overlay screens
            AnimatedVisibility(
                visible = overlay is Overlay.NoteEdit,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                if (overlay is Overlay.NoteEdit) {
                    NoteEditScreen(
                        noteId = (overlay as Overlay.NoteEdit).id,
                        onBack = { overlay = Overlay.None }
                    )
                }
            }
            AnimatedVisibility(
                visible = overlay is Overlay.TodoEdit,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                if (overlay is Overlay.TodoEdit) {
                    TodoEditScreen(
                        todoId = (overlay as Overlay.TodoEdit).id,
                        onBack = { overlay = Overlay.None }
                    )
                }
            }
            AnimatedVisibility(
                visible = overlay is Overlay.ScheduleEdit,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                if (overlay is Overlay.ScheduleEdit) {
                    ScheduleEditScreen(onBack = { overlay = Overlay.None })
                }
            }
            AnimatedVisibility(
                visible = overlay is Overlay.Settings,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                if (overlay is Overlay.Settings) {
                    SettingsScreen(onBack = { overlay = Overlay.None })
                }
            }
        }
    }

    // ── Completed Todos BottomSheet ──
    if (showCompletedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCompletedSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            CompletedTodosContent(onBack = { showCompletedSheet = false })
        }
    }
}
