package com.rmws2002.noteapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rmws2002.noteapp.ui.screens.HomeScreen
import com.rmws2002.noteapp.ui.screens.NoteEditScreen
import com.rmws2002.noteapp.ui.screens.NoteListScreen
import com.rmws2002.noteapp.ui.screens.ScheduleEditScreen
import com.rmws2002.noteapp.ui.screens.ScheduleScreen
import com.rmws2002.noteapp.ui.screens.SearchScreen
import com.rmws2002.noteapp.ui.screens.TodoEditScreen
import com.rmws2002.noteapp.ui.screens.TodoListScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Outlined.Home, Icons.Filled.Home)
    data object Notes : Screen("notes", "笔记", Icons.Outlined.Description, Icons.Filled.Description)
    data object Todos : Screen("todos", "待办", Icons.Outlined.Checklist, Icons.Filled.Checklist)
    data object Schedule : Screen("schedule", "日程", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    data object Search : Screen("search", "搜索", Icons.Outlined.Search, Icons.Filled.Search)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Notes,
    Screen.Todos,
    Screen.Schedule,
    Screen.Search
)

@Composable
fun NoteAppNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                    onNavigateToTodos = { navController.navigate(Screen.Todos.route) },
                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                    onNoteClick = { id -> navController.navigate("note_edit/$id") },
                    onTodoClick = { id -> navController.navigate("todo_edit/$id") }
                )
            }

            composable(Screen.Notes.route) {
                NoteListScreen(
                    onAddNote = { navController.navigate("note_edit/0") },
                    onNoteClick = { id -> navController.navigate("note_edit/$id") }
                )
            }

            composable(
                route = "note_edit/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                NoteEditScreen(
                    noteId = if (noteId == 0L) null else noteId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Todos.route) {
                TodoListScreen(
                    onAddTodo = { navController.navigate("todo_edit/0") },
                    onTodoClick = { id -> navController.navigate("todo_edit/$id") }
                )
            }

            composable(
                route = "todo_edit/{todoId}",
                arguments = listOf(navArgument("todoId") { type = NavType.LongType })
            ) { backStackEntry ->
                val todoId = backStackEntry.arguments?.getLong("todoId") ?: 0L
                TodoEditScreen(
                    todoId = if (todoId == 0L) null else todoId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onAddSchedule = { navController.navigate("schedule_edit") }
                )
            }

            composable("schedule_edit") {
                ScheduleEditScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onNoteClick = { id -> navController.navigate("note_edit/$id") },
                    onTodoClick = { id -> navController.navigate("todo_edit/$id") }
                )
            }
        }
    }
}
