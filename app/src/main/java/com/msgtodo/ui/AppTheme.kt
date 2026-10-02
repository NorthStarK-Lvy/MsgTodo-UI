package com.msgtodo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.msgtodo.data.Prefs
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun AppTheme(
    themeMode: Prefs.ThemeMode = Prefs.ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val controller = remember(themeMode) {
        val mode = when (themeMode) {
            Prefs.ThemeMode.SYSTEM -> ColorSchemeMode.System
            Prefs.ThemeMode.LIGHT -> ColorSchemeMode.Light
            Prefs.ThemeMode.DARK -> ColorSchemeMode.Dark
        }
        ThemeController(colorSchemeMode = mode)
    }

    MiuixTheme(controller = controller) {
                                Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}
