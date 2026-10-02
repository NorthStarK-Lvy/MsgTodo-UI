package com.msgtodo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.msgtodo.core.LlmClient
import com.msgtodo.core.LlmResult
import com.msgtodo.data.ApiConfig
import com.msgtodo.data.ApiConfigStore
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LlmTextPage(
    cfg: ApiConfig,
    onCfgChange: (ApiConfig) -> Unit,
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
    val scope = rememberCoroutineScope()

    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testOk by remember { mutableStateOf<Boolean?>(null) }
    var showKey by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        Spacer(Modifier.height(8.dp))

                GroupCard {
            Text(
                text = if (cfg.ready) {
                    "已配好 —— 收到通知会自动提取待办"
                } else {
                    "还没配全 —— 地址 / 密钥 / 模型名三项都要填，否则不发任何请求"
                },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        Gap()

                SmallTitle(text = "预设")
        GroupCard {
            ApiConfigStore.PRESETS.forEach { preset ->
                ArrowPreference(
                    title = preset.name,
                    summary = preset.note,
                    onClick = {
                        onCfgChange(cfg.copy(baseUrl = preset.baseUrl, model = preset.model))
                        testResult = null
                        testOk = null
                    },
                )
            }
            Note("点一下自动填好地址和模型名，密钥要自己填。")
        }
        Gap()

                SmallTitle(text = "接口参数")
        TextField(
            value = cfg.baseUrl,
            onValueChange = { onCfgChange(cfg.copy(baseUrl = it.trim())) },
            label = "接口地址",
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = cfg.apiKey,
            onValueChange = { onCfgChange(cfg.copy(apiKey = it.trim())) },
            label = "API 密钥",
            singleLine = true,
            visualTransformation = if (showKey) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    text = if (showKey) "隐藏" else "显示",
                    onClick = { showKey = !showKey },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = cfg.model,
            onValueChange = { onCfgChange(cfg.copy(model = it.trim())) },
            label = "模型名",
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
        )

        Spacer(Modifier.height(16.dp))

                Button(
            onClick = {
                testing = true
                testResult = null
                testOk = null
                scope.launch {
                    val r = LlmClient.test(cfg)
                    testing = false
                    when (r) {
                        is LlmResult.Ok -> {
                            testOk = true
                            testResult = "连接成功。模型回了：${r.content.take(60)}"
                        }

                        is LlmResult.Fatal -> {
                            testOk = false
                            testResult = r.message
                        }

                        is LlmResult.Retryable -> {
                            testOk = false
                            testResult = r.message
                        }
                    }
                }
            },
            enabled = !testing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
            colors = ButtonDefaults.buttonColorsPrimary(),
        ) {
            Text(text = if (testing) "测试中…" else "测试连接")
        }

        if (testResult != null) {
            Spacer(Modifier.height(12.dp))
            GroupCard {
                Text(
                    text = testResult!!,
                    style = MiuixTheme.textStyles.footnote1,
                    color = if (testOk == true) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }

        Gap()

                SmallTitle(text = "关于中转站")
        GroupCard {
            Note(ApiConfigStore.RELAY_WARNING)
        }

        Spacer(Modifier.height(32.dp))
    }
}
