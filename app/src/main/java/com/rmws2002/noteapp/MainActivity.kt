package com.rmws2002.noteapp

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rmws2002.noteapp.data.preferences.ThemeMode
import com.rmws2002.noteapp.ui.navigation.NoteAppNavGraph
import com.rmws2002.noteapp.ui.theme.NoteAppTheme
import com.rmws2002.noteapp.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── 120Hz high refresh rate for HyperOS ──
        configureHighRefreshRate()

        enableEdgeToEdge()
        NotificationHelper.createChannels(this)
        setContent {
            val app = application as NoteApp
            val themeMode by app.appPreferences.themeMode.collectAsState(
                initial = ThemeMode.SYSTEM
            )
            NoteAppTheme(themeMode = themeMode) {
                NoteAppNavGraph()
            }
        }
    }

    private fun configureHighRefreshRate() {
        @Suppress("DEPRECATION")
        val bestMode = windowManager.defaultDisplay?.supportedModes?.maxByOrNull { it.refreshRate }
        if (bestMode != null && bestMode.refreshRate > 60f) {
            @Suppress("DEPRECATION")
            window.attributes.preferredDisplayModeId = bestMode.modeId
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
    }
}
