package com.msgtodo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.msgtodo.R
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsTab(
    apiSummary: String,
    callCount: Int,
    granted: Boolean,
    watchSummary: String,
    onOpenLlm: () -> Unit,
    onOpenCallLog: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenSources: () -> Unit,
    onOpenKeepAlive: () -> Unit,
    onOpenAppearance: () -> Unit,
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

                                                                                                                                Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_ai_model),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1472f / 860f),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "消息总结",
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "调用你自己的大模型，把通知里的重点整理成待办",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        }
        Gap()

        SmallTitle(text = "设置")
        GroupCard {
            ArrowPreference(
                title = "大模型管理",
                summary = apiSummary,
                onClick = onOpenLlm,
            )
            ArrowPreference(
                title = "调用记录",
                summary = if (callCount == 0) {
                    "看每次发给模型什么、模型回了什么"
                } else {
                    "已记录 $callCount 次 · 含请求与回复原文"
                },
                onClick = onOpenCallLog,
            )
            ArrowPreference(
                title = "隐私设置",
                summary = "处理范围、诊断日志、隐私政策",
                onClick = onOpenPrivacy,
            )
            ArrowPreference(
                title = "外观",
                summary = "悬浮底栏",
                onClick = onOpenAppearance,
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
                    "未授权 —— 现在一条都抓不到"
                },
                onClick = onOpenSources,
            )
            ArrowPreference(
                title = "总结范围",
                summary = watchSummary,
                onClick = onOpenSources,
            )
        }
        Gap()

        SmallTitle(text = "保活")
        GroupCard {
            ArrowPreference(
                title = "保活与权限",
                summary = "电池优化白名单、自启动、后台运行",
                onClick = onOpenKeepAlive,
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
