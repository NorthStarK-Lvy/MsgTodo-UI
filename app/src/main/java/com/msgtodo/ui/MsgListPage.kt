package com.msgtodo.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.msgtodo.core.WatchFilter
import com.msgtodo.data.MsgRecord
import com.msgtodo.data.MsgStore
import top.yukonga.miuix.kmp.basic.SmallTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MsgListPage(
    records: List<MsgRecord>,
    expandedId: Long?,
    watchRule: WatchFilter.Rule,
    apiEnabled: Boolean,
    onToggle: (Long) -> Unit,
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
        item(key = "head") {
            SmallTitle(
                text = "全部消息 · 共 ${records.size} 条" +
                    "（应用内最多留存 ${MsgStore.MAX_KEEP} 条）",
            )
        }
        items(records, key = { it.id }) { m ->
            MsgCard(
                record = m,
                expanded = expandedId == m.id,
                timeText = timeFmt.format(Date(m.postedAt)),
                watchRule = watchRule,
                apiEnabled = apiEnabled,
                onToggle = { onToggle(m.id) },
            )
        }
    }
}
