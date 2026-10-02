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
fun KeepAlivePage(
    onOpenBatterySettings: () -> Unit,
    onOpenAppDetails: () -> Unit,
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

        SmallTitle(text = "保活")
        GroupCard {
            ArrowPreference(
                title = "加入电池优化白名单",
                summary = "让系统别在息屏后回收它",
                onClick = onOpenBatterySettings,
            )
            ArrowPreference(
                title = "打开应用详情",
                summary = "在里面找「自启动」「后台运行」「锁定后台」并允许",
                onClick = onOpenAppDetails,
            )
        }
        Gap()

        GroupCard {
            Note(
                "监听服务被系统回收之后，App 界面看起来一切正常，" +
                    "但消息再也进不来了 —— 这是最常见的问题。上面两项能大幅降低概率。\n\n" +
                    "排查方法：去「探针」页看链路自检。如果「① 系统通知到达」一直是 0、" +
                    "而且日志里有「监听已断开」的系统事件，那就是被回收了。",
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
