package com.msgtodo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.msgtodo.R
import com.msgtodo.data.ApiConfig
import com.msgtodo.data.ApiConfigStore
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LlmSettingsPage(
    cfg: ApiConfig,

    todayCount: Int,
    onCfgChange: (ApiConfig) -> Unit,

    onOpenText: () -> Unit,

    onOpenVision: () -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
                    val limitIndex = ApiConfigStore.DAILY_LIMIT_OPTIONS
        .indexOfFirst { it.value == cfg.dailyLimit }
        .takeIf { it >= 0 }
        ?: ApiConfigStore.DAILY_LIMIT_OPTIONS.lastIndex

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_ai_illustrate),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1104f / 645f),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "通知 → 总结",
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "自动把群里的安排、作业、通知收进待办",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        }
        Gap()

                GroupCard {
            SwitchPreference(
                checked = cfg.enabled,
                onCheckedChange = { onCfgChange(cfg.copy(enabled = it)) },
                title = "启用大模型提取",
                summary = "关闭后只记录消息，不发送任何请求",
            )
        }
        Gap()

                                                                SmallTitle(text = "模型配置")
        GroupCard {
            ArrowPreference(
                title = "文本模型",
                summary = buildString {
                    append("把通知文字总结成待办 · ")
                    append(if (cfg.ready) "已配好（${cfg.model}）" else "还没配全")
                },
                onClick = onOpenText,
            )
            ArrowPreference(
                title = "视觉模型",
                summary = buildString {
                    append("看懂作业图片再总结 · ")
                    append(if (cfg.visionReady) "已配好（${cfg.visionModel}）" else "还没配全")
                },
                onClick = onOpenVision,
            )
            Note("两套配置各填各的，不共用。")
        }
        Gap()

                                                SmallTitle(text = "用量与上限")
        GroupCard {
            BasicComponent(
                title = "今天已调用",
                summary = when {
                    !cfg.enabled -> "大模型提取关着，不会产生任何调用"
                    cfg.dailyLimit > 0 && todayCount >= cfg.dailyLimit ->
                        "$todayCount 次 · 已达上限，今天不再发请求"

                    cfg.dailyLimit > 0 -> "$todayCount 次 / 上限 ${cfg.dailyLimit} 次"
                    else -> "$todayCount 次 · 未设上限"
                },
            )
            OverlayDropdownPreference(
                items = ApiConfigStore.DAILY_LIMIT_OPTIONS.map { it.label },
                selectedIndex = limitIndex,
                title = "每日调用上限",
                summary = "到上限当天不再发请求，明天自动重置",
                onSelectedIndexChange = { idx ->
                    ApiConfigStore.DAILY_LIMIT_OPTIONS.getOrNull(idx)?.let { opt ->
                        onCfgChange(cfg.copy(dailyLimit = opt.value))
                    }
                },
            )
        }
        Gap()

                SmallTitle(text = "关于中转站")
        GroupCard {
            Note(ApiConfigStore.RELAY_WARNING)
        }

        Spacer(Modifier.height(32.dp))
    }
}
