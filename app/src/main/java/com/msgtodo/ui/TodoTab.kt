package com.msgtodo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msgtodo.core.ExtractStatus
import com.msgtodo.core.TimeParse
import com.msgtodo.data.TodoItem
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Alarm
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun TodoTab(
    items: List<TodoItem>,
    status: ExtractStatus,
    apiEnabled: Boolean,
    apiReady: Boolean,
    onToggleDone: (Long, Boolean) -> Unit,
    onSave: (TodoItem) -> Unit,
    onDelete: (Long) -> Unit,
    onClearDone: () -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,

    visionReady: Boolean,
    onOpenSettings: () -> Unit,

    onOpenDetail: (Long) -> Unit,

    onClearFailures: () -> Unit,
) {
    val dueFmt = remember { SimpleDateFormat("MM-dd HH:mm", Locale.CHINA) }

    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<TodoItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<TodoItem?>(null) }
        var homeworkOpen by remember { mutableStateOf(false) }
    var visionHintOpen by remember { mutableStateOf(false) }

    val shown = remember(items, query) {
        if (query.isBlank()) {
            items
        } else {
            items.filter {
                it.title.contains(query, true) ||
                    it.rawText.contains(query, true) ||
                    it.source.contains(query, true) ||
                                                            it.subItems.any { s -> s.contains(query, true) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScroll),
            contentPadding = contentPadding,
        ) {
                        if (!apiEnabled || !apiReady) {
                item(key = "api-hint") {
                    SmallTitle(text = if (apiEnabled) "接口还没配全" else "大模型提取已关闭")
                    GroupCard {
                        Note(
                            if (apiEnabled) {
                                "「启户大模型提取」已经打开，但接口地址 / 密钥 / 模型名还没填全，" +
                                    "现在不会发任何请求。"
                            } else {
                                "现在处于纯本地模式：只记录消息，不会自动提取待办，" +
                                    "也不发任何网络请求。"
                            },
                        )
                        Note("去「设置 → 模型接口」填好，点「测试连接」通过就能户了。")
                    }
                    Gap()
                }
            }

                        item(key = "new") {
                Button(
                    onClick = { creating = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(text = "新建待办")
                }
                Spacer(Modifier.height(SectionGap))
            }

                        if (items.isNotEmpty()) {
                item(key = "search") {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        label = "搜索标题 / 原文 / 来源",
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PagePadding),
                    )
                    Spacer(Modifier.height(SectionGap))
                }
            }

                        item(key = "status") {
                SmallTitle(text = "提取状态")
                GroupCard {
                    StatusRow("排队中", status.queued)
                    StatusRow("已处理", status.done)
                    StatusRow("其中提取出待办", status.todos)
                    StatusRow("判定为非待办", status.skipped)
                                                                                                                        StatusRow("提取失败", status.failedTotal, highlight = status.failedTotal > 0)
                                                            if (status.processing != null) {
                        ProcessingRow(status)
                    }
                                                                                                                                            status.failureNote?.let { note ->
                        Note(note)
                        ArrowPreference(
                            title = "清掉失败提醒",
                            summary = "只清提醒，消息和记录都保留",
                            onClick = onClearFailures,
                        )
                    }
                    if (status.lastError != null) {
                        Note("最近一次错误：${status.lastError}")
                    }
                                                            if (items.isNotEmpty()) {
                        Note("长按某张待办卡片可以直接删除（会先让你确认）。")
                    }
                }
                Gap()
            }

                        if (items.isEmpty()) {
                item(key = "empty") {
                    SmallTitle(text = "待办")
                    GroupCard {
                        Note(
                            "还没有待办。\n\n" +
                                "可以点上面的「新建待办」手动加一条；\n" +
                                "配好接口之后，收到像通知、安排一类的微信消息也会自动进来 —— " +
                                "比如「明天下午三点开班会」。\n\n" +
                                "闲聊不会变成待办，这是刻意设计的。",
                        )
                    }
                }
            } else {
                item(key = "list-head") {
                    SmallTitle(
                        text = if (query.isBlank()) {
                            "待办（${items.count { !it.done }} 项未完成 / 共 ${items.size} 项）"
                        } else {
                            "搜索「$query」命中 ${shown.size} 条"
                        },
                    )
                    GroupCard {
                        ArrowPreference(
                            title = "清空已完成",
                            summary = "把打过勾的删掉",
                            onClick = onClearDone,
                        )
                    }
                    Gap()
                }

                if (shown.isEmpty()) {
                    item(key = "no-match") {
                        GroupCard {
                            Note("没有匹配的待办。换个词试试。")
                        }
                    }
                } else {
                    items(shown, key = { it.id }) { t ->
                        TodoCard(
                            item = t,
                            dueText = t.dueAt?.let { dueFmt.format(Date(it)) },
                            overdue = t.dueAt?.let { it < System.currentTimeMillis() && !t.done } ?: false,
                            onToggle = { onToggleDone(t.id, !t.done) },
                            onEdit = { editing = t },
                            onDelete = { confirmDelete = t },
                            onOpenDetail = { onOpenDetail(t.id) },
                        )
                    }
                }
            }
        }

        if (creating || editing != null) {
            TodoEditDialog(
                source = editing,
                onDismiss = {
                    creating = false
                    editing = null
                },
                onConfirm = { title, dueAt ->
                    val base = editing
                    onSave(
                        if (base == null) {
                            TodoItem(
                                id = 0L,                                               source = "手动添加",
                                sender = null,
                                rawText = title,
                                title = title,
                                dueAt = dueAt,
                                createdAt = System.currentTimeMillis(),
                                done = false,
                            )
                        } else {
                            base.copy(title = title, dueAt = dueAt)
                        },
                    )
                    creating = false
                    editing = null
                },
            )
        }

        val pending = confirmDelete
        if (pending != null) {
            OverlayDialog(
                show = true,
                title = "删除这条待办？",
                summary = "「${pending.title}」删掉就找不回来了。",
                onDismissRequest = { confirmDelete = null },
                content = {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(
                            text = "取消",
                            onClick = { confirmDelete = null },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(20.dp))
                        TextButton(
                            text = "删除",
                            onClick = {
                                onDelete(pending.id)
                                confirmDelete = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                },
            )
        }

                                FloatingActionButton(
            onClick = {
                                                if (visionReady) homeworkOpen = true else visionHintOpen = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp),
        ) {
            Icon(
                imageVector = MiuixIcons.Add,
                contentDescription = "从作业图片生成待办",
                tint = MiuixTheme.colorScheme.onPrimary,
            )
        }

        if (homeworkOpen) {
            HomeworkSheet(
                onDismiss = { homeworkOpen = false },
                                                onGenerated = {},
            )
        }

        if (visionHintOpen) {
            OverlayDialog(
                show = true,
                title = "还没配视觉模型",
                summary = "「作业图片 → 待办」需要一个能读图的模型，" +
                    "和现在提取通知文字户的模型是分开配置的。",
                onDismissRequest = { visionHintOpen = false },
                content = {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(
                            text = "知道了",
                            onClick = { visionHintOpen = false },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(20.dp))
                        TextButton(
                            text = "去配置",
                            onClick = {
                                visionHintOpen = false
                                onOpenSettings()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, value: Int, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainer,
        )
        Text(
            text = value.toString(),
            style = MiuixTheme.textStyles.title4,
                                                color = if (highlight) {
                MiuixTheme.colorScheme.error
            } else {
                MiuixTheme.colorScheme.onSurfaceContainer
            },
        )
    }
}

private const val SLOW_SEC = 90L

@Composable
private fun ProcessingRow(status: ExtractStatus) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
            LaunchedEffect(status.processing, status.processingSince) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val elapsed = ((now - status.processingSince) / 1000).coerceAtLeast(0)
    val color = if (elapsed >= SLOW_SEC) {
        MiuixTheme.colorScheme.error
    } else {
        MiuixTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "正在处理：${status.processing}",
            style = MiuixTheme.textStyles.body1,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${elapsed}s",
            style = MiuixTheme.textStyles.title4,
            color = color,
        )
    }

        val rest = (status.queued - 1).coerceAtLeast(0)
    if (rest > 0 && status.avgMsPerItem > 0) {
        val restSec = (status.avgMsPerItem * rest / 1000).coerceAtLeast(1)
        Note("队列里还有 $rest 条，按最近平均约 $restSec 秒")
    }
}

@Composable
private fun TodoCard(
    item: TodoItem,
    dueText: String?,
    overdue: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenDetail: () -> Unit,
) {
                                                        ContentCard(
        onClick = onOpenDetail,
        onLongPress = onDelete,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                state = if (item.done) ToggleableState.On else ToggleableState.Off,
                onClick = onToggle,
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                                                            style = MiuixTheme.textStyles.title4,
                    color = if (item.done) {
                        MiuixTheme.colorScheme.onSurfaceContainerVariant
                    } else {
                        MiuixTheme.colorScheme.onBackground
                    },
                    textDecoration = if (item.done) TextDecoration.LineThrough else null,
                )

                                                if (item.subItems.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    item.subItems.take(SUB_ITEM_SHOWN).forEach { sub ->
                        Text(
                            text = "· $sub",
                            style = MiuixTheme.textStyles.body2,                               color = MiuixTheme.colorScheme.onSurfaceContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val rest = item.subItems.size - SUB_ITEM_SHOWN
                    if (rest > 0) {
                        Text(
                            text = "还有 $rest 项",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                }

                                                Spacer(Modifier.height(8.dp))
                val meta = buildList {
                    add(item.source)
                    item.sender?.takeIf { it.isNotBlank() }?.let { add(it) }
                }.joinToString(" · ")
                Text(
                    text = meta,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )

                                                                if (dueText != null) {
                    Spacer(Modifier.height(8.dp))
                    DueChip(text = dueText, overdue = overdue)
                }

                Spacer(Modifier.height(10.dp))

                                                                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.weight(1f))
                    CompactButton(text = "编辑", onClick = onEdit)
                    CompactButton(text = "详情", onClick = onOpenDetail, primary = true)
                }
            }
        }
    }
}

private const val SUB_ITEM_SHOWN = 2

@Composable
private fun DueChip(text: String, overdue: Boolean) {
    val bg = if (overdue) {
        MiuixTheme.colorScheme.errorContainer
    } else {
        MiuixTheme.colorScheme.primaryContainer
    }
    val fg = if (overdue) {
        MiuixTheme.colorScheme.onErrorContainer
    } else {
        MiuixTheme.colorScheme.onPrimaryContainer
    }
    Row(
        modifier = Modifier
            .squircleSurface(color = bg, cornerRadius = 8.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = MiuixIcons.Alarm,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = fg,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote1,
            color = fg,
        )
    }
}

@Composable
private fun CompactButton(
    text: String,
    onClick: () -> Unit,
    primary: Boolean = false,
) {
    TextButton(
        text = text,
        onClick = onClick,
                                                minWidth = 0.dp,
        textStyle = MiuixTheme.textStyles.footnote1,
        colors = if (primary) {
            ButtonDefaults.textButtonColorsPrimary()
        } else {
            ButtonDefaults.textButtonColors()
        },
    )
}

@Composable
private fun TodoEditDialog(
    source: TodoItem?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, dueAt: Long?) -> Unit,
) {
    var title by remember(source) { mutableStateOf(source?.title.orEmpty()) }
    var dueText by remember(source) {
        mutableStateOf(
            source?.dueAt?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(it)) }
                .orEmpty(),
        )
    }
    var error by remember(source) { mutableStateOf<String?>(null) }

    OverlayDialog(
        show = true,
        title = if (source == null) "新建待办" else "编辑待办",
        onDismissRequest = onDismiss,
        content = {
            Column {
                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = null
                    },
                    label = "要做什么",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(10.dp))

                TextField(
                    value = dueText,
                    onValueChange = {
                        dueText = it
                        error = null
                    },
                    label = "时间（可留空，如「明天下午三点」）",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = "保存",
                        onClick = {
                            val t = title.trim()
                                                                                    when {
                                t.isEmpty() -> error = "标题不能为空"

                                dueText.isBlank() -> onConfirm(t, null)

                                else -> {
                                    val due = TimeParse.extract(dueText)
                                    if (due == null) {
                                        error = "这个时间没看懂，换个写法试试" +
                                            "（如「明天下午3点」「9月25日 14:00」）"
                                    } else {
                                        onConfirm(t, due)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        },
    )
}
