package com.msgtodo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.msgtodo.data.TodoItem
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodoDetailPage(
    item: TodoItem?,
    onSave: (TodoItem) -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
            if (item == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScroll)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
        ) {
            Spacer(Modifier.height(8.dp))
            SmallTitle(text = "待办")
            GroupCard {
                Note("这条待办已经不在了（可能刚被清空）。返回列表看看。")
            }
        }
        return
    }

                var subs by remember(item.id) { mutableStateOf(item.subItems) }

    fun commit(next: List<String>) {
        subs = next
        onSave(item.copy(subItems = next.filter { it.isNotBlank() }))
    }

    val dueFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        Spacer(Modifier.height(8.dp))

                                SmallTitle(text = "标题")
        GroupCard {
            Text(
                text = item.title,
                style = MiuixTheme.textStyles.title4,
                color = MiuixTheme.colorScheme.onSurfaceContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
            val meta = buildList {
                add(item.source)
                item.sender?.takeIf { it.isNotBlank() }?.let { add(it) }
                if (item.done) add("已完成") else add("未完成")
            }.joinToString(" · ")
            Note(meta)
        }
        Gap()

                if (item.dueAt != null) {
            SmallTitle(text = "时间")
            GroupCard {
                Note(dueFmt.format(Date(item.dueAt)))
            }
            Gap()
        }

                SmallTitle(
            text = if (subs.isEmpty()) "包含事项" else "包含事项（${subs.size}）",
        )

        if (subs.isEmpty()) {
            GroupCard {
                Note(
                    "这条待办没有细分事项 —— 它本身就是一件事。\n" +
                        "要拆成几项的话，点下面的「添加一项」。",
                )
            }
            Spacer(Modifier.height(10.dp))
        } else {
                                                subs.forEachIndexed { idx, value ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PagePadding),
                ) {
                    TextField(
                        value = value,
                        onValueChange = { typed ->
                            commit(subs.toMutableList().also { it[idx] = typed })
                        },
                        label = "第 ${idx + 1} 项",
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        text = "删除",
                        onClick = {
                            commit(subs.filterIndexed { i, _ -> i != idx })
                        },
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Button(
            onClick = { commit(subs + "") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
            colors = ButtonDefaults.buttonColorsPrimary(),
        ) {
            Text(text = "添加一项")
        }
        Gap()

                SmallTitle(text = "识别原文")
        GroupCard {
            val raw = item.rawText
            if (raw.isBlank() || raw == PLACEHOLDER_RAW) {
                                                Note(
                    "这次没有留下文字原文。\n" +
                        "可能是模型没返回，你也没填补充说明；\n" +
                        "也可能这条是从通知来的（原文在上面那条消息里）。",
                )
            } else {
                Text(
                    text = raw,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
        Gap()
    }
}

private const val PLACEHOLDER_RAW = "（来自作业图片）"
