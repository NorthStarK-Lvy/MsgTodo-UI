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
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SourcesPage(
    granted: Boolean,
    watchSummary: String,

    whitelist: Set<String>,
    onToggleApp: (pkg: String, enabled: Boolean) -> Unit,
    onOpenNotiAccess: () -> Unit,
    onOpenWatchList: () -> Unit,
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
        Spacer(Modifier.height(8.dp))

                                SmallTitle(text = "监听哪些应用")
        GroupCard {
            Prefs.MONITORED_APPS.forEach { app ->
                SwitchPreference(
                    checked = app.pkg in whitelist,
                    onCheckedChange = { on -> onToggleApp(app.pkg, on) },
                    title = app.name,
                    summary = app.note,
                )
            }
        }
                                if (whitelist.isEmpty()) {
            Note(
                "一个都没开 —— 现在任何应用的消息都不会被记录或总结。\n\n" +
                    "想让它干活，至少打开一个。",
            )
        }
        Gap()

        SmallTitle(text = "消息来源")
        GroupCard {
            ArrowPreference(
                title = "通知使用权",
                summary = if (granted) {
                    "已授权，正在监听"
                } else {
                    "未授权 —— 点这里去系统设置里勾选「消息待办」"
                },
                onClick = onOpenNotiAccess,
            )
            ArrowPreference(
                title = "总结范围",
                summary = "$watchSummary · 点这里修改",
                onClick = onOpenWatchList,
            )
            if (!granted) {
                Note(
                    "「通知使用权」不是普通权限，App 自己申请不了，" +
                        "必须你手动去系统设置里打开。\n\n" +
                        "按钮没反应的话，手动走：设置 → 应用 → 特殊权限 → 通知使用权。\n\n" +
                        "有些手机（小米、OPPO 等）打开后要重启一次才真正生效。",
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
