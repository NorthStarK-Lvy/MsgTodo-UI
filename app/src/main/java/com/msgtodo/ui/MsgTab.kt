package com.msgtodo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msgtodo.core.SecretGuard
import com.msgtodo.core.WatchFilter
import com.msgtodo.data.MsgRecord
import com.msgtodo.data.MsgStore
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PREVIEW_COUNT = 5

@Composable
fun MsgTab(
    records: List<MsgRecord>,
    expandedId: Long?,
    granted: Boolean,
    watchRule: WatchFilter.Rule,
    apiEnabled: Boolean,
    callCount: Int,
    onOpenSettings: () -> Unit,
    onOpenWatchList: () -> Unit,
    onOpenCallLog: () -> Unit,
    onOpenAllMessages: () -> Unit,
    onToggle: (Long) -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
    val timeFmt = remember { SimpleDateFormat("MM-dd HH:mm", Locale.CHINA) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll),
        contentPadding = contentPadding,
    ) {
                        if (!granted) {
            item(key = "need-auth") {
                SmallTitle(text = "先授权 —— 否则一条都抓不到")
                GroupCard {
                    Note(
                        "「通知使用权」不是普通权限，App 自己申请不了，" +
                            "必须你手动去系统设置里打开。",
                    )
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp, bottom = 12.dp),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text(text = "去开启通知使用权")
                    }
                    Note(
                        "点上面的按钮 → 在列表里找到「消息待办」→ 打开开关。\n\n" +
                            "按钮没反应的话，手动走：设置 → 应用 → 特殊权限 → 通知使用权。\n\n" +
                            "有些手机（小米、OPPO 等）打开后要重启一次才真正生效。",
                    )
                }
                Gap()
            }
        }

                                if (granted && records.isEmpty()) {
            item(key = "troubleshoot") {
                SmallTitle(text = "一条都没抓到？按顺序查这四项")
                GroupCard {
                    Note(
                        "① 通知里有没有正文？\n" +
                            "微信里「设置 → 新消息通知 → 通知显示消息详情」如果关着，" +
                            "通知里只有「你收到一条新消息」，拿不到内容。\n\n" +
                            "② 手机有没有真的收到新消息？\n" +
                            "微信自己在前台开着的时候，它不弹通知 —— 我们自然收不到。" +
                            "请切到桌面，让别人给你发一条。\n\n" +
                            "③ 监听服务有没有被系统干掉？\n" +
                            "这是最常见的原因。去「探针」页看有没有「监听已断开」的红色系统事件；" +
                            "再去「设置 → 保活」里把电池优化白名单和应用后台权限打开。\n\n" +
                            "④ 权限是什么时候给的？\n" +
                            "刚开完通知使用权，有些手机需要重启一次才真正绑定。",
                    )
                    ArrowPreference(
                        title = "看诊断数据（探针页）",
                        summary = "逐环节告诉你消息卡在哪一步",
                        onClick = onOpenSettings,
                    )
                }
                Gap()
            }
        }

        item(key = "ops") {
            SmallTitle(text = "已捕获 ${records.size} 条")
            GroupCard {
                if (watchRule.isFiltering) {
                    ArrowPreference(
                        title = "只总结范围里的消息",
                        summary = if (watchRule.isListEmpty) {
                                                                                    "名单是空的 —— 现在只有「@所有人」会发出去"
                        } else {
                            "会话 ${watchRule.conversations.size} 条 · " +
                                "发言人 ${watchRule.senders.size} 条 · 点这里修改"
                        },
                        onClick = onOpenWatchList,
                    )
                }
                ArrowPreference(
                    title = "调用记录",
                    summary = if (callCount == 0) {
                        "看每次发给模型什么、模型回了什么"
                    } else {
                        "已记录 $callCount 次调用 · 点开看请求与回复原文"
                    },
                    onClick = onOpenCallLog,
                )
                ArrowPreference(
                    title = "导出全部消息",
                    summary = "把捕获到的内容（含上下文）发出来",
                    onClick = onExport,
                )
                ArrowPreference(
                    title = "清空消息",
                    summary = "同时会清掉上下文窗口",
                    onClick = onClear,
                )
            }
            Gap()
        }

        if (records.isEmpty()) {
            item(key = "empty") {
                GroupCard {
                    Note(
                        "还没有捕获到消息。\n\n" +
                            "确认已授权通知使用权，然后让手机收几条微信消息。\n\n" +
                            "如果一直没反应，去「探针」页看统计 —— " +
                            "那里会告诉你卡在哪一环。",
                    )
                }
            }
        } else {
            item(key = "list-title") {
                SmallTitle(
                    text = if (records.size > PREVIEW_COUNT) {
                        "最新 $PREVIEW_COUNT 条 · 点一条看「将发给模型的内容」"
                    } else {
                        "点一条看「将发给模型的内容」"
                    },
                )
            }
                        items(records.take(PREVIEW_COUNT), key = { it.id }) { m ->
                MsgCard(
                    record = m,
                    expanded = expandedId == m.id,
                    timeText = timeFmt.format(Date(m.postedAt)),
                    watchRule = watchRule,
                    apiEnabled = apiEnabled,
                    onToggle = { onToggle(m.id) },
                )
            }
            if (records.size > PREVIEW_COUNT) {
                item(key = "more") {
                    GroupCard {
                        ArrowPreference(
                            title = "查看更多消息",
                            summary = "还有 ${records.size - PREVIEW_COUNT} 条 · " +
                                "进二级页看全部（最多留存 ${MsgStore.MAX_KEEP} 条）",
                            onClick = onOpenAllMessages,
                        )
                    }
                    Gap()
                }
            }
        }
    }
}

@Composable
internal fun MsgCard(
    record: MsgRecord,
    expanded: Boolean,
    timeText: String,
    watchRule: WatchFilter.Rule,
    apiEnabled: Boolean,
    onToggle: () -> Unit,
) {
                    val watch = WatchFilter.decide(
        rule = watchRule,
        conversation = record.conversation,
        sender = record.sender,
        text = record.text,
        isGroup = record.isGroup,
    )
            val guard = SecretGuard.check(record.promptText())
    val willSend = watch.send && !guard.blocked && record.hasBody && apiEnabled

            ContentCard(onClick = onToggle) {
                        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = record.conversation,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurfaceContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = timeText,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        }

        if (record.isGroup || !record.sender.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            val tags = listOfNotNull(
                if (record.isGroup) "群聊" else null,
                record.sender?.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            Text(
                text = tags,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

                Spacer(Modifier.height(6.dp))
        Text(
            text = when {
                willSend -> "会发给模型"
                                guard.blocked -> "已封死：${guard.reason}"
                !record.hasBody -> "不发送：通知里没有正文"
                !apiEnabled -> "不发送：大模型提取没开"
                else -> "不发送：${watch.reason}"
            },
            style = MiuixTheme.textStyles.footnote1,
            color = when {
                willSend -> MiuixTheme.colorScheme.primary
                guard.blocked -> MiuixTheme.colorScheme.error
                else -> MiuixTheme.colorScheme.onSurfaceContainerVariant
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text(
                        text = if (record.hasBody) record.text else "（这条通知里没有正文）",
            style = MiuixTheme.textStyles.body1,
            color = if (record.hasBody) {
                MiuixTheme.colorScheme.onSurfaceContainer
            } else {
                MiuixTheme.colorScheme.error
            },
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (expanded) "收起" else "上文 ${record.context.size} 条 · 点开看将发给模型的内容",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            PromptPreview(record, willSend, watch.reason, guard)
            Spacer(Modifier.height(10.dp))
                                    Text(
                text = "这只是将要发送的内容预览。" +
                    "想核对实际发出去的完整请求和模型返回的原文，去「调用记录」。",
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PromptPreview(
    record: MsgRecord,
    willSend: Boolean,
    watchReason: String,
    guard: SecretGuard.Result,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PreviewLabel(
            when {
                                                guard.blocked -> "已封死，绝不外发 —— ${guard.reason}"
                willSend -> "将发给模型的内容"
                else -> "以下内容不会发出去 —— $watchReason"
            },
        )
        Spacer(Modifier.height(6.dp))

        PreviewField("会话", record.conversation + if (record.isGroup) "（群聊）" else "")
        if (!record.sender.isNullOrBlank()) {
            PreviewField("发送者", record.sender)
        }

        if (record.context.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PreviewLabel("上文 ${record.context.size} 条")
            record.context.forEach { line ->
                Text(
                    text = "· $line",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        PreviewLabel("本条")
        Text(
            text = record.text.ifBlank { "（无正文）" },
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        )

                                        if (record.hasRawText) {
            Spacer(Modifier.height(8.dp))
            PreviewLabel("通知里的原始正文（用于核对）")
            Text(
                text = record.rawText,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PreviewLabel(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote2,
        color = MiuixTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PreviewField(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.width(56.dp),
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
