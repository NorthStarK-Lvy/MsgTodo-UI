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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProbeStatsPage(
    counters: Map<String, Int>,
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

        SmallTitle(text = "各环节计数明细")
        GroupCard {
            if (counters.isEmpty()) {
                Note("还没有数据。收到通知后，这里会显示各个环节的数量。")
            } else {
                counters.entries
                    .sortedByDescending { it.value }
                    .forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = key,
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurfaceContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = value.toString(),
                                style = MiuixTheme.textStyles.title4,
                                color = MiuixTheme.colorScheme.onSurfaceContainer,
                            )
                        }
                    }
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        GroupCard {
            Note(
                "数字从应用启动起算，重启会归零 —— 它反映的是「这一次运行」的情况，" +
                    "不是历史累计。\n\n" +
                    "排查时重点看相邻两项之间的落差：比如「通知到达（全部应用）」有几十条、" +
                    "但「在白名单内」是 0，说明来的都不是微信。",
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
