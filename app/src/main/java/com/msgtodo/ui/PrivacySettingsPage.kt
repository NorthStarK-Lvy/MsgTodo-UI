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
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun PrivacySettingsPage(
    whitelist: Set<String>,
    probeLog: Boolean,
    onProbeLogChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,

    onOpenPrivacyPolicy: () -> Unit,
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

                SmallTitle(text = "处理范围")
        GroupCard {
            whitelist.sorted().forEach { pkg ->
                                BasicComponent(
                    title = if (pkg == "com.tencent.mm") "微信" else pkg,
                    summary = pkg,
                )
            }
            Note("只有这些应用的通知会被处理，其余的连日志都不记。")
        }
        Gap()

                                Gap()

                SmallTitle(text = "诊断")
        GroupCard {
            SwitchPreference(
                checked = probeLog,
                onCheckedChange = onProbeLogChange,
                title = "记录探针日志",
                summary = "记录每条通知的全部字段，用于排查问题",
            )
        }
        Gap()

                SmallTitle(text = "数据")
        GroupCard {
            ArrowPreference(
                title = "清空全部本地数据",
                summary = "消息、上下文、待办、探针日志、统计一次清掉",
                onClick = onClearAll,
            )
            Note("所有数据只存在这台手机上，卸载应用即彻底删除。")
        }
        Gap()

                                SmallTitle(text = "隐私")
        GroupCard {
            ArrowPreference(
                title = "隐私政策",
                summary = "数据去哪了 · 用途边界 · 安全防线",
                onClick = onOpenPrivacyPolicy,
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
