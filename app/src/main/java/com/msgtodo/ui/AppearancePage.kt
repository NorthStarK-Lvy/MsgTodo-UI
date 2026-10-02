package com.msgtodo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.msgtodo.data.Prefs
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun AppearancePage(
    floatingNav: Boolean,
    onFloatingNavChange: (Boolean) -> Unit,
    themeMode: Prefs.ThemeMode,
    onThemeModeChange: (Prefs.ThemeMode) -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
                                SmallTitle(text = "深浅色")
        GroupCard {
                                                ThemeModeRow(
                title = "跟随系统",
                summary = "系统切深色它就深色 —— 大多数人要的就是这个",
                selected = themeMode == Prefs.ThemeMode.SYSTEM,
                onClick = { onThemeModeChange(Prefs.ThemeMode.SYSTEM) },
            )
            ThemeModeRow(
                title = "浅色",
                summary = null,
                selected = themeMode == Prefs.ThemeMode.LIGHT,
                onClick = { onThemeModeChange(Prefs.ThemeMode.LIGHT) },
            )
            ThemeModeRow(
                title = "深色",
                summary = null,
                selected = themeMode == Prefs.ThemeMode.DARK,
                onClick = { onThemeModeChange(Prefs.ThemeMode.DARK) },
            )
        }
        Gap()

        SmallTitle(text = "底部导航")
        GroupCard {
            SwitchPreference(
                checked = floatingNav,
                onCheckedChange = onFloatingNavChange,
                title = "悬浮底栏",
                summary = "胶囊形底栏，浮在内容之上而不是贴着屏幕底部",
            )
        }
        Gap()

        SmallTitle(text = "说明")
        GroupCard {
            Note(
                "深浅色默认跟随系统；手动钉死之后，系统怎么切都不再变，" +
                    "想恢复跟随就选回「跟随系统」。\n\n" +
                    "悬浮底栏浮在内容之上，列表最后一条会多留出一段空白，避免被挡住。",
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ThemeModeRow(
    title: String,
    summary: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    RadioButtonPreference(
        title = title,
        summary = summary,
        selected = selected,
        onClick = onClick,
    )
}
