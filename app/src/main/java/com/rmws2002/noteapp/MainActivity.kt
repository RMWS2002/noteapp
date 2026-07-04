package com.rmws2002.noteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rmws2002.noteapp.ui.navigation.NoteAppNavGraph
import com.rmws2002.noteapp.ui.theme.NoteAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as NoteApp
            val themeMode by app.appPreferences.themeMode.collectAsState(
                initial = com.rmws2002.noteapp.data.preferences.ThemeMode.SYSTEM
            )
            NoteAppTheme(themeMode = themeMode) {
                NoteAppNavGraph()
            }
        }
    }
}
