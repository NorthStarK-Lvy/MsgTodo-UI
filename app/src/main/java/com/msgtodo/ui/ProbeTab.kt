package com.msgtodo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msgtodo.core.ExtractStatus
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProbeTab(
    granted: Boolean,
    counters: Map<String, Int>,
    capturedCount: Int,
    status: ExtractStatus,
    extractModel: String?,
    watchSummary: String,
    onOpenStats: () -> Unit,
    onOpenOps: () -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
    val statusFmt = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll),
        contentPadding = contentPadding,
    ) {
                        item(key = "chain") {
            SmallTitle(text = "链路自检")
            GroupCard {
                val arrived = counters["通知到达（全部应用）"] ?: 0
                val inWhite = counters["通知到达（白名单内）"] ?: 0
                val recorded = counters["收录（有正文）"] ?: 0
                val noBody = counters["收录但无正文"] ?: 0
                val skipped = counters.entries
                    .filter { it.key.startsWith("跳过：") }
                    .sumOf { it.value }
                val blocked = counters["不发：不在总结范围"] ?: 0
                val guarded = counters["不发：命中安全防线"] ?: 0
                val sent = counters["发出总结"] ?: 0

                ChainRow("① 系统通知到达", arrived, "所有应用的都算")
                ChainRow("② 在白名单内", inWhite, "默认只算微信")
                ChainRow("③ 被去重 / 无会话名跳过", skipped, "重复通知会走到这里")
                ChainRow("④ 落库为消息", recorded + noBody, "含没有正文的")
                ChainRow("⑤ 其中没有正文", noBody, "微信没给内容，发不了")
                ChainRow("⑥ 被安全防线封死", guarded, "含凭据字样或网址，绝不外发")
                ChainRow("⑦ 被总结范围挡下", blocked, "仍在本机，只是不发")
                ChainRow("⑧ 投递去总结", sent, "进队列，等接口返回")

                Spacer(Modifier.height(8.dp))
                Note(
                    when {
                        !granted ->
                            "通知使用权没开 —— 所以 ① 永远是 0。先去开启它。"

                        arrived == 0 ->
                            "① 是 0 说明系统根本没把通知给我们。\n" +
                                "可能：通知使用权没生效（重启一次）／服务被系统回收了" +
                                "（看下面有没有红色的「监听已断开」）／手机这段时间确实没收到通知。"

                        recorded + noBody == 0 ->
                            "通知到了但没有一条被收录。看上面 ③ 的数字：" +
                                "如果全是重复，说明微信在反复刷新同一条通知（正常）。"

                        sent == 0 && guarded > 0 && blocked == 0 ->
                            "有 $guarded 条被安全防线封死了 —— 内容里含凭据字样或可打开的网址。" +
                                "这是硬底线，没有开关。去「消息」页能看到具体是哪几条、为什么。"

                        sent == 0 && blocked > 0 ->
                            "消息都到了，但全被「总结范围」挡下 —— 名单里没有它们。"

                        sent == 0 ->
                            "消息收录了但一条都没投递。检查「大模型管理」里的总开关有没有打开。"

                        else -> "链路是通的：已经有 $sent 条被送去总结。"
                    },
                )
            }
            Gap()
        }

                item(key = "extract") {
            SmallTitle(text = "提取状态")
            GroupCard {
                StatusRow("接口", extractModel ?: "未配置")
                StatusRow("总结范围", watchSummary)
                StatusRow("队列中", status.queued.toString())
                StatusRow("已处理", status.done.toString())
                StatusRow("其中生成待办", status.todos.toString())
                StatusRow("判定为非待办", status.skipped.toString())
                StatusRow(
                    "最近一次投递",
                    status.lastQueued?.let { "$it · ${statusFmt.format(Date(status.lastQueuedAt))}" }
                        ?: "还没有",
                )
                StatusRow(
                    "最近一次成功",
                    if (status.lastOkAt > 0) statusFmt.format(Date(status.lastOkAt)) else "还没有",
                )
                if (status.lastError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "最近一次错误：${status.lastError}",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
                if (status.lastQueued == null && status.done == 0) {
                    Note(
                        "全部是 0 是正常的 —— 从应用启动起算。" +
                            "关键看「最近一次投递」：有它就说明消息确实在往外走。",
                    )
                }
            }
            Gap()
        }

                                        item(key = "more") {
            SmallTitle(text = "更多")
            GroupCard {
                ArrowPreference(
                    title = "各环节计数明细",
                    summary = if (counters.isEmpty()) {
                        "还没有数据"
                    } else {
                        "共 ${counters.size} 项 · 看每一项的原始计数"
                    },
                    onClick = onOpenStats,
                )
                ArrowPreference(
                    title = "诊断操作",
                    summary = "发测试通知、保活服务、导出 / 清空日志",
                    onClick = onOpenOps,
                )
            }
            Gap()
        }
    }
}

@Composable
private fun ChainRow(label: String, count: Int, hint: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body1,
            color = if (count > 0) {
                MiuixTheme.colorScheme.onSurfaceContainer
            } else {
                MiuixTheme.colorScheme.onSurfaceContainerVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$count",
            style = MiuixTheme.textStyles.title4,
            color = if (count > 0) {
                MiuixTheme.colorScheme.primary
            } else {
                MiuixTheme.colorScheme.onSurfaceContainerVariant
            },
        )
    }
    Text(
        text = hint,
        style = MiuixTheme.textStyles.footnote2,
        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            maxLines = 1,
            modifier = Modifier.width(96.dp),
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
        )
    }
}
