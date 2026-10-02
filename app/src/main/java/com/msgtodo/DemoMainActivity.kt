package com.msgtodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msgtodo.core.ExtractStatus
import com.msgtodo.core.WatchFilter
import com.msgtodo.data.ApiConfig
import com.msgtodo.data.ApiConfigStore
import com.msgtodo.data.MsgRecord
import com.msgtodo.data.Prefs
import com.msgtodo.data.TodoItem
import com.msgtodo.ui.AppTheme
import com.msgtodo.ui.AppearancePage
import com.msgtodo.ui.CallLogPage
import com.msgtodo.ui.LlmSettingsPage
import com.msgtodo.ui.LlmTextPage
import com.msgtodo.ui.LlmVisionPage
import com.msgtodo.ui.MsgListPage
import com.msgtodo.ui.MsgTab
import com.msgtodo.ui.PrivacyPolicyPage
import com.msgtodo.ui.PrivacySettingsPage
import com.msgtodo.ui.ProbeTab
import com.msgtodo.ui.SettingsTab
import com.msgtodo.ui.SourcesPage
import com.msgtodo.ui.TodoDetailPage
import com.msgtodo.ui.TodoTab
import com.msgtodo.ui.WatchListPage
import com.msgtodo.data.CallRecord
import kotlinx.coroutines.flow.MutableStateFlow
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tasks
import top.yukonga.miuix.kmp.icon.extended.Tune

private val demoTodos = MutableStateFlow(
    listOf(
        TodoItem(
            id = 1, source = "班委群", sender = null,
            rawText = "明天下午三点班会，全员到齐，带好体检表。",
            title = "班会准备", subItems = listOf("三点班会", "带体检表"),
            dueAt = System.currentTimeMillis() + 26 * 3600_000L,
            createdAt = System.currentTimeMillis(), done = false,
        ),
        TodoItem(
            id = 2, source = "数学作业", sender = null,
            rawText = "P12 第 1、3、5 题，背诵第三课。",
            title = "数学作业",
            subItems = listOf("P12 第 1、3、5 题", "背诵第三课"),
            dueAt = null,
            createdAt = System.currentTimeMillis(), done = true,
            doneAt = System.currentTimeMillis(),
        ),
    ),
)

private val demoMsgs = MutableStateFlow(
    listOf(
        MsgRecord(
            id = 1, pkg = "com.tencent.mm", conversation = "班委群",
            sender = "李老师", isGroup = true,
            text = "@所有人 明天下午三点班会，全员到齐。",
            postedAt = System.currentTimeMillis() - 3600_000L,
            context = listOf("上周的班会记录已归档"),
            rawText = "[有全体消息]李老师: @所有人 明天下午三点班会，全员到齐。",
        ),
        MsgRecord(
            id = 2, pkg = "com.tencent.mm", conversation = "妈妈",
            sender = null, isGroup = false,
            text = "周末回家吃饭吗",
            postedAt = System.currentTimeMillis() - 7200_000L,
            context = emptyList(),
        ),
    ),
)

private val demoCalls = MutableStateFlow(
    listOf(
        CallRecord(
            id = 1, at = System.currentTimeMillis() - 3500_000L,
            conversation = "班委群",
            system = "（demo 存根：完整的 system prompt 在主工程）",
            user = "【会话】班委群（群聊）\n【发送者】李老师\n【本条】\n  @所有人 明天下午三点班会",
            reply = "{\"is_todo\": true, \"title\": \"班会准备\"}",
            error = null, isTodo = true, title = "班会准备", due = "明天 15:00",
            saved = true,
        ),
    ),
)

private val demoStatus = ExtractStatus(
    queued = 1, done = 3, todos = 2, skipped = 1,
    lastOkAt = System.currentTimeMillis() - 3500_000L,
    lastQueued = "班委群", lastQueuedAt = System.currentTimeMillis() - 3600_000L,
    avgMsPerItem = 4200L,
)

private const val TAB_TODO = 0
private const val TAB_MSG = 1
private const val TAB_PROBE = 2
private const val TAB_SETTINGS = 3
private val TAB_TITLES = listOf("待办", "消息", "探针", "设置")

private sealed interface Screen {
    data class Tab(val index: Int) : Screen
    data object Llm : Screen
    data object LlmText : Screen
    data object LlmVision : Screen
    data object Privacy : Screen
    data object PrivacyPolicy : Screen
    data object WatchList : Screen
    data object CallLog : Screen
    data object MsgList : Screen
    data object Sources : Screen
    data object Appearance : Screen
    data class TodoDetail(val id: Long) : Screen
}

private fun Screen.depth(): Int = when (this) {
    is Screen.Tab -> 0
    Screen.Llm, Screen.Privacy, Screen.WatchList, Screen.CallLog, Screen.MsgList,
    Screen.Sources, Screen.Appearance,
    is Screen.TodoDetail -> 1
    Screen.LlmText, Screen.LlmVision, Screen.PrivacyPolicy -> 2
}

private fun Screen.stateKey(): String = when (this) {
    is Screen.Tab -> "tab:${index}"
    Screen.Llm -> "llm"
    Screen.LlmText -> "llmText"
    Screen.LlmVision -> "llmVision"
    Screen.Privacy -> "privacy"
    Screen.PrivacyPolicy -> "privacyPolicy"
    Screen.WatchList -> "watchList"
    Screen.CallLog -> "callLog"
    Screen.MsgList -> "msgList"
    Screen.Sources -> "sources"
    Screen.Appearance -> "appearance"
    is Screen.TodoDetail -> "todoDetail:${id}"
}

private fun Screen.title(): String = when (this) {
    is Screen.Llm -> "大模型管理"
    is Screen.LlmText -> "文本模型"
    is Screen.LlmVision -> "视觉模型"
    is Screen.Privacy -> "隐私设置"
    is Screen.PrivacyPolicy -> "隐私政策"
    is Screen.WatchList -> "总结范围"
    is Screen.CallLog -> "调用记录"
    is Screen.MsgList -> "全部消息"
    is Screen.Sources -> "消息来源"
    is Screen.Appearance -> "外观"
    is Screen.TodoDetail -> "待办详情"
    is Screen.Tab -> TAB_TITLES.getOrElse(index) { "MsgTodo UI" }
}

class DemoMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(Prefs.ThemeMode.SYSTEM) }
            AppTheme(themeMode = themeMode) {
                DemoRoot(
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                )
            }
        }
    }
}

@Composable
private fun DemoRoot(
    themeMode: Prefs.ThemeMode,
    onThemeModeChange: (Prefs.ThemeMode) -> Unit,
) {
    var screen by remember { mutableStateOf<Screen>(Screen.Tab(TAB_TODO)) }
    var lastTab by remember { mutableStateOf(TAB_TODO) }

    val todos by demoTodos.collectAsStateWithLifecycle()
    val msgs by demoMsgs.collectAsStateWithLifecycle()
    val calls by demoCalls.collectAsStateWithLifecycle()

    var apiCfg by remember {
        mutableStateOf(
            ApiConfig(
                enabled = true,
                baseUrl = ApiConfigStore.PRESETS[0].baseUrl,
                apiKey = "", model = ApiConfigStore.PRESETS[0].model,
                dailyLimit = 200,
            ),
        )
    }
    var watchRule by remember { mutableStateOf(WatchFilter.Rule()) }
    var expandedMsg by remember { mutableStateOf<Long?>(null) }
    var expandedCall by remember { mutableStateOf<Long?>(null) }

    val scrollBehavior = MiuixScrollBehavior()
    val nestedScroll = scrollBehavior.nestedScrollConnection

    val counters = mapOf(
        "通知到达（全部应用）" to 42,
        "通知到达（白名单内）" to 18,
        "收录（有正文）" to 16,
        "收录但无正文" to 2,
        "不发：不在总结范围" to 3,
        "已投递提取" to 13,
    )

    val watchSummary = if (!watchRule.filterEnabled) {
        "全部总结"
    } else {
        buildString {
            append("只总结指定来源")
            val parts = mutableListOf<String>()
            if (watchRule.conversations.isNotEmpty()) parts += "会话 ${watchRule.conversations.size}"
            if (watchRule.senders.isNotEmpty()) parts += "发言人 ${watchRule.senders.size}"
            if (parts.isNotEmpty()) append("（").append(parts.joinToString(" · ")).append("）")
            if (watchRule.alwaysMentionAll) append(" +@所有人")
        }
    }

    val inSecondary = screen !is Screen.Tab
    val goBack = {
        screen = when (screen) {
            is Screen.LlmText, is Screen.LlmVision -> Screen.Llm
            is Screen.PrivacyPolicy -> Screen.Privacy
            else -> Screen.Tab(lastTab)
        }
    }
    BackHandler(enabled = inSecondary) { goBack() }
    val currentTab = (screen as? Screen.Tab)?.index ?: lastTab

    Scaffold(
        topBar = {
            TopAppBar(
                title = screen.title(),
                largeTitle = screen.title(),
                subtitle = when {
                    inSecondary -> "demo 数据 · 仅界面演示"
                    currentTab == TAB_TODO -> "待办 ${todos.count { !it.done }} 项未完成"
                    currentTab == TAB_MSG -> "已捕获 ${msgs.size} 条"
                    currentTab == TAB_PROBE -> "诊断工具"
                    else -> "demo 数据 · 仅界面演示"
                },
                navigationIcon = {
                    if (inSecondary) {
                        IconButton(onClick = { goBack() }) {
                            Icon(MiuixIcons.Back, contentDescription = "返回")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (!inSecondary) {
                NavigationBar {
                    listOf(
                        Triple(TAB_TODO, MiuixIcons.Tasks, "待办"),
                        Triple(TAB_MSG, MiuixIcons.Messages, "消息"),
                        Triple(TAB_PROBE, MiuixIcons.Tune, "探针"),
                        Triple(TAB_SETTINGS, MiuixIcons.Settings, "设置"),
                    ).forEach { (tab, icon, label) ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { lastTab = tab; screen = Screen.Tab(tab) },
                            icon = icon, label = label,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val tabPadding = PaddingValues(bottom = 24.dp)
            val stateHolder = rememberSaveableStateHolder()
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    val fromDepth = initialState.depth()
                    val toDepth = targetState.depth()
                    when {
                        toDepth > fromDepth ->
                            (slideInHorizontally { it } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally { -it / 4 } + fadeOut(tween(160)))
                        toDepth < fromDepth ->
                            (slideInHorizontally { -it / 4 } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally { it } + fadeOut(tween(160)))
                        else -> {
                            val f = (initialState as? Screen.Tab)?.index ?: 0
                            val t = (targetState as? Screen.Tab)?.index ?: 0
                            val dir = if (t >= f) 1 else -1
                            (slideInHorizontally { dir * it / 12 } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally { -dir * it / 12 } + fadeOut(tween(180)))
                        }
                    }
                },
                label = "screen",
            ) { target ->
                stateHolder.SaveableStateProvider(target.stateKey()) {
                    when (target) {
                        is Screen.Llm -> LlmSettingsPage(
                            cfg = apiCfg, todayCount = calls.size,
                            onCfgChange = { apiCfg = it },
                            onOpenText = { screen = Screen.LlmText },
                            onOpenVision = { screen = Screen.LlmVision },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.LlmText -> LlmTextPage(
                            cfg = apiCfg, onCfgChange = { apiCfg = it },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.LlmVision -> LlmVisionPage(
                            cfg = apiCfg, onCfgChange = { apiCfg = it },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.Privacy -> PrivacySettingsPage(
                            whitelist = setOf("com.tencent.mm"),
                            probeLog = true,
                            onProbeLogChange = {},
                            onClearAll = {},
                            onOpenPrivacyPolicy = { screen = Screen.PrivacyPolicy },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.PrivacyPolicy -> PrivacyPolicyPage(
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.WatchList -> WatchListPage(
                            rule = watchRule,
                            knownConversations = msgs.map { it.conversation }.distinct(),
                            knownSenders = msgs.mapNotNull { it.sender }.distinct(),
                            onFilterEnabledChange = { watchRule = watchRule.copy(filterEnabled = it) },
                            onConversationsChange = { keys, raws ->
                                val norm = WatchFilter.normalizeSet(keys)
                                watchRule = watchRule.copy(
                                    conversations = norm,
                                    displayNames = WatchFilter.mergeNames(norm, raws, watchRule.displayNames),
                                )
                            },
                            onSendersChange = { keys, raws ->
                                val norm = WatchFilter.normalizeSet(keys)
                                watchRule = watchRule.copy(
                                    senders = norm,
                                    displayNames = WatchFilter.mergeNames(norm, raws, watchRule.displayNames),
                                )
                            },
                            onMentionAllChange = { watchRule = watchRule.copy(alwaysMentionAll = it) },
                            onOpenPrivacyPolicy = { screen = Screen.PrivacyPolicy },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.CallLog -> CallLogPage(
                            records = calls, expandedId = expandedCall,
                            onToggle = { id -> expandedCall = if (expandedCall == id) null else id },
                            onExport = {}, onClear = { demoCalls.value = emptyList(); expandedCall = null },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.MsgList -> MsgListPage(
                            records = msgs, expandedId = expandedMsg,
                            watchRule = watchRule, apiEnabled = apiCfg.enabled,
                            onToggle = { id -> expandedMsg = if (expandedMsg == id) null else id },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.Sources -> SourcesPage(
                            granted = true, watchSummary = watchSummary,
                            whitelist = setOf("com.tencent.mm"),
                            onToggleApp = { _, _ -> },
                            onOpenNotiAccess = {},
                            onOpenWatchList = { screen = Screen.WatchList },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.Appearance -> AppearancePage(
                            floatingNav = false,
                            onFloatingNavChange = {},
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.TodoDetail -> TodoDetailPage(
                            item = todos.firstOrNull { it.id == target.id },
                            onSave = { item ->
                                demoTodos.value = demoTodos.value.map { if (it.id == item.id) item else it }
                            },
                            contentPadding = tabPadding, nestedScroll = nestedScroll,
                        )
                        is Screen.Tab -> when (target.index) {
                            TAB_TODO -> TodoTab(
                                items = todos, status = demoStatus,
                                apiEnabled = apiCfg.enabled, apiReady = apiCfg.ready,
                                onToggleDone = { id, done ->
                                    demoTodos.value = demoTodos.value.map {
                                        if (it.id == id) it.copy(done = done, doneAt = if (done) System.currentTimeMillis() else null) else it
                                    }
                                },
                                onSave = { item ->
                                    demoTodos.value = demoTodos.value.map { if (it.id == item.id) item else it }
                                },
                                onDelete = { id -> demoTodos.value = demoTodos.value.filterNot { it.id == id } },
                                onClearDone = { demoTodos.value = demoTodos.value.filterNot { it.done } },
                                contentPadding = tabPadding, nestedScroll = nestedScroll,
                                visionReady = apiCfg.visionReady,
                                onOpenSettings = { screen = Screen.Llm },
                                onOpenDetail = { id -> screen = Screen.TodoDetail(id) },
                                onClearFailures = {},
                            )
                            TAB_MSG -> MsgTab(
                                records = msgs, expandedId = expandedMsg,
                                granted = true, watchRule = watchRule,
                                apiEnabled = apiCfg.enabled, callCount = calls.size,
                                onOpenSettings = {},
                                onOpenWatchList = { screen = Screen.WatchList },
                                onOpenCallLog = { screen = Screen.CallLog },
                                onOpenAllMessages = { screen = Screen.MsgList },
                                onToggle = { id -> expandedMsg = if (expandedMsg == id) null else id },
                                onExport = {}, onClear = { demoMsgs.value = emptyList(); expandedMsg = null },
                                contentPadding = tabPadding, nestedScroll = nestedScroll,
                            )
                            TAB_PROBE -> ProbeTab(
                                granted = true, counters = counters,
                                capturedCount = msgs.size, status = demoStatus,
                                extractModel = if (apiCfg.ready) apiCfg.model else "配置不完整",
                                watchSummary = watchSummary,
                                onOpenStats = {}, onOpenOps = {},
                                contentPadding = tabPadding, nestedScroll = nestedScroll,
                            )
                            else -> SettingsTab(
                                apiSummary = if (apiCfg.ready) apiCfg.model else "还没配全",
                                callCount = calls.size,
                                granted = true,
                                watchSummary = watchSummary,
                                onOpenLlm = { screen = Screen.Llm },
                                onOpenCallLog = { screen = Screen.CallLog },
                                onOpenPrivacy = { screen = Screen.Privacy },
                                onOpenSources = { screen = Screen.Sources },
                                onOpenKeepAlive = {},
                                onOpenAppearance = { screen = Screen.Appearance },
                                contentPadding = tabPadding, nestedScroll = nestedScroll,
                            )
                        }
                    }
                }
            }
        }
    }
}
