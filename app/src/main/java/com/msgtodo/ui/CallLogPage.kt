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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msgtodo.data.CallRecord
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallLogPage(
    records: List<CallRecord>,
    expandedId: Long?,
    onToggle: (Long) -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
    val timeFmt = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll),
        contentPadding = contentPadding,
    ) {
        item(key = "ops") {
            SmallTitle(text = "共 ${records.size} 次调用")
            GroupCard {
                ArrowPreference(
                    title = "导出全部记录",
                    summary = "含请求原文和模型回复原文，可直接发出来",
                    onClick = onExport,
                )
                ArrowPreference(
                    title = "清空调用记录",
                                                                                                    summary = "只删这份记录，不影响消息、待办，也不重置今日次数",
                    onClick = onClear,
                )
            }
            Gap()
        }

        if (records.isEmpty()) {
            item(key = "empty") {
                GroupCard {
                    Note(
                        "还没有调用过模型。\n\n" +
                            "打开「设置 → 大模型管理 → 启用大模型提取」并配好接口，" +
                            "之后每收到一条消息、每次发请求，" +
                            "都会在这里留下一条完整记录。\n\n" +
                            "记录里能看到：发出去的 system 提示词、实际发送的聊天内容、" +
                            "模型返回的原文、以及最终判定结果。",
                    )
                }
            }
        } else {
            item(key = "head") {
                SmallTitle(text = "点一条展开看完整请求与回复")
            }
            items(records, key = { it.id }) { r ->
                CallCard(
                    record = r,
                    expanded = expandedId == r.id,
                    timeText = timeFmt.format(Date(r.at)),
                    onToggle = { onToggle(r.id) },
                )
            }
        }
    }
}

@Composable
private fun CallCard(
    record: CallRecord,
    expanded: Boolean,
    timeText: String,
    onToggle: () -> Unit,
) {
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

        Spacer(Modifier.height(6.dp))

                val stage = record.stage
        Text(
            text = stage.label,
            style = MiuixTheme.textStyles.footnote1,
            color = when (stage) {
                CallRecord.Stage.SAVED -> MiuixTheme.colorScheme.primary
                CallRecord.Stage.NOT_TODO -> MiuixTheme.colorScheme.onSurfaceContainerVariant
                else -> MiuixTheme.colorScheme.error
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

                if (record.title?.isNotBlank() == true) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "模型给的标题：${record.title}",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = if (expanded) "收起" else "点开看请求原文和模型回复原文",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            CallDetail(record)
        }
    }
}

@Composable
private fun CallDetail(record: CallRecord) {
    Column(modifier = Modifier.fillMaxWidth()) {

        if (record.error != null) {
            DetailLabel("请求失败原因", error = true)
            MonoBlock(record.error)
            Spacer(Modifier.height(10.dp))
        }

        DetailLabel("① 发出去的 system 提示词")
        MonoBlock(record.system)
        Spacer(Modifier.height(10.dp))

        DetailLabel("② 发出去的 user 提示词（聊天内容）")
        MonoBlock(record.user)
        Spacer(Modifier.height(10.dp))

        DetailLabel("③ 模型返回的原文")
        MonoBlock(record.reply ?: "（没有返回内容 —— 见上面的失败原因）")
        Spacer(Modifier.height(10.dp))

        DetailLabel("④ 解析结果")
        KeyValue("是否待办", record.isTodo?.toString() ?: "解析不了")
        KeyValue("标题", record.title?.takeIf { it.isNotBlank() } ?: "-")
        KeyValue("时间", record.due?.takeIf { it.isNotBlank() } ?: "-")
        KeyValue("是否写进待办", if (record.saved) "是" else "否")
    }
}

@Composable
private fun DetailLabel(text: String, error: Boolean = false) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote2,
        color = if (error) {
            MiuixTheme.colorScheme.error
        } else {
            MiuixTheme.colorScheme.primary
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MonoBlock(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote1,
        fontFamily = FontFamily.Monospace,
        color = MiuixTheme.colorScheme.onSurfaceContainer,
                        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
    )
}

@Composable
private fun KeyValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.width(104.dp),
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
