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
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun ProbeOpsPage(
    keepAlive: Boolean,

    logBytes: Long,

    logPath: String?,
    onSendAlarm: () -> Unit,

    onSendDailyPreview: () -> Unit,
    onToggleKeepAlive: () -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit,
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

        SmallTitle(text = "诊断操作")
        GroupCard {
            ArrowPreference(
                title = "发一条「闹钟流」测试通知",
                summary = "先把手机调静音 / 免打扰，再点它，看响不响",
                onClick = onSendAlarm,
            )
            ArrowPreference(
                title = "预览「每日提醒」",
                summary = "立刻发一条 21 点提醒的样例",
                onClick = onSendDailyPreview,
            )
            ArrowPreference(
                title = if (keepAlive) "停止保活服务" else "启动保活服务",
                summary = if (keepAlive) {
                    "正在运行 —— 锁屏放着，看它能撑多久"
                } else {
                    "挂一个前台服务，测后台存活时长"
                },
                onClick = onToggleKeepAlive,
            )
        }
        Gap()

        SmallTitle(text = "日志")
        GroupCard {
            ArrowPreference(
                title = "导出探针日志",
                summary = "按文件导出，大小不受限（当前 ${fmtSize(logBytes)}）",
                onClick = onExport,
            )
            ArrowPreference(
                title = "清空探针日志",
                summary = "同时会重置链路自检的统计",
                onClick = onClear,
            )
            Note(
                "探针日志记的是「通知的原始字段」（标题、正文、包名、时间等），" +
                    "用来查「消息为什么没进来」。\n\n" +
                    "日志不会自动删除，会一直积累到你手动清空 —— " +
                    "所以现在 ${fmtSize(logBytes)}。攒够了就导出。\n\n" +
                    (logPath?.let { "文件位置（用系统自带的「文件管理」可以进到这里复制）：\n$it\n\n" } ?: "") +
                    "它只在本机，导出是你主动发出去的动作 —— 发之前请自己确认内容。",
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

private fun fmtSize(bytes: Long): String = when {
    bytes <= 0L -> "还没有内容"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "%.2f MB".format(bytes / 1024.0 / 1024.0)
}
