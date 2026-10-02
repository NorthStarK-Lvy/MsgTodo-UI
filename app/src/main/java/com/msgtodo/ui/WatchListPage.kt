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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.msgtodo.core.WatchFilter
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun WatchListPage(
    rule: WatchFilter.Rule,

    knownConversations: List<String>,

    knownSenders: List<String>,
    onFilterEnabledChange: (Boolean) -> Unit,

    onConversationsChange: (Set<String>, Set<String>) -> Unit,
    onSendersChange: (Set<String>, Set<String>) -> Unit,
    onMentionAllChange: (Boolean) -> Unit,

    onOpenPrivacyPolicy: () -> Unit,
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

                SmallTitle(text = "总结范围")
        GroupCard {
            SwitchPreference(
                checked = rule.filterEnabled,
                onCheckedChange = onFilterEnabledChange,
                title = "只总结我指定的来源",
                summary = if (rule.filterEnabled) {
                    "其余消息照常记录，只是不总结"
                } else {
                    "关着 = 每条消息都发给模型"
                },
            )
        }
        Gap()

                GroupCard {
            SwitchPreference(
                checked = rule.alwaysMentionAll,
                onCheckedChange = onMentionAllChange,
                title = "「@所有人」一律总结",
                summary = "不受下面名单限制",
            )
        }
        Gap()

                if (!rule.filterEnabled) {
            GroupCard {
                Note("现在是全部总结。想只总结部分群或联系人，先打开上面的开关。")
            }
            Spacer(Modifier.height(16.dp))
            PrivacyPolicyEntry(onOpenPrivacyPolicy)
            Spacer(Modifier.height(32.dp))
            return@Column
        }

                                if (rule.isListEmpty) {
            GroupCard {
                Text(
                    text = "两个名单都是空的 —— 现在除了「@所有人」，一条都不会被总结。",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
            Gap()
        }

                NameSection(
            title = "群聊 / 单聊",
            hint = "勾群名 = 整个群；勾昵称 = 只总结和他的单聊",
            emptyHint = "还没有可选的会话。\n\n" +
                "收到几条消息之后，群名和昵称会自动出现在这里；也可以在下面手动输入。",
            inputLabel = "群名或昵称，多个用逗号隔开",
            candidates = knownConversations,
            picked = rule.conversations,
            stored = rule.displayNames,
            onChange = onConversationsChange,
        )

        Gap()

                NameSection(
            title = "发言人",
            hint = "勾某个人 = 他在任何群里说的话都总结（单聊要勾上面）",
            emptyHint = "还没有可选的发言人。\n\n" +
                "群聊消息里会带发送者名字，收到之后会自动出现在这里；也可以在下面手动输入。",
            inputLabel = "昵称，多个用逗号隔开",
            candidates = knownSenders,
            picked = rule.senders,
            stored = rule.displayNames,
            onChange = onSendersChange,
        )

        Gap()

                SmallTitle(text = "已选 ${rule.activeList.size} 条")
        GroupCard {
            ArrowPreference(
                title = "全部取消",
                summary = if (rule.isListEmpty) {
                    "两个名单都已经空了"
                } else {
                    "会话 ${rule.conversations.size} 条 · 发言人 ${rule.senders.size} 条"
                },
                onClick = {
                                                            onConversationsChange(emptySet(), emptySet())
                    onSendersChange(emptySet(), emptySet())
                },
            )
        }

        Gap()
        PrivacyPolicyEntry(onOpenPrivacyPolicy)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun NameSection(
    title: String,
    hint: String,
    emptyHint: String,
    inputLabel: String,
    candidates: List<String>,
    picked: Set<String>,
    stored: Map<String, String>,
    onChange: (Set<String>, Set<String>) -> Unit,
) {
    var input by remember { mutableStateOf("") }

                                        val items = remember(candidates, picked, stored) {
        WatchFilter.displayNameOf(picked, candidates, stored).map { (k, v) -> k to v }
    }

        val unresolved = remember(candidates, picked, stored) {
        WatchFilter.unresolvedCount(picked, candidates, stored)
    }

    SmallTitle(text = title)
    Text(
        text = hint,
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        modifier = Modifier.padding(horizontal = PagePadding),
    )
    Spacer(Modifier.height(8.dp))

    GroupCard {
        if (items.isEmpty()) {
            Note(emptyHint)
        } else {
            items.forEach { (key, name) ->
                val isPicked = key in picked
                CheckboxPreference(
                                        title = name,
                                                            summary = if (isPicked) "在总结范围内" else "不在范围内",
                    checked = isPicked,
                    onCheckedChange = { on ->
                                                                        val next = if (on) picked + key else picked - key
                        onChange(next, if (on) setOf(name) else emptySet())
                    },
                )
            }
        }
    }

                if (unresolved > 0) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "有 $unresolved 条显示的是简写（旧版本没存原名），照常生效；" +
                "重新勾一次或再输一次就会记下原名。",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = PagePadding),
        )
    }

    Gap()

        TextField(
        value = input,
        onValueChange = { input = it },
        label = inputLabel,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePadding),
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePadding),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(
            text = "全部选上",
            onClick = {
                                                onChange(picked + items.map { it.first }, items.map { it.second }.toSet())
            },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        TextButton(
            text = "添加",
            onClick = {
                                                                                val add = WatchFilter.parseInput(input)
                if (add.isNotEmpty()) onChange(picked + add.map { WatchFilter.normalize(it) }, add)
                input = ""
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrivacyPolicyEntry(onOpen: () -> Unit) {
    SmallTitle(text = "隐私")
    GroupCard {
        ArrowPreference(
            title = "隐私政策",
            summary = "数据去哪了 · 安全防线 · 匹配规则",
            onClick = onOpen,
        )
    }
}
