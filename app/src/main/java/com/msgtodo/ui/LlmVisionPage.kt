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
import com.msgtodo.data.VisionPresets
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
fun LlmVisionPage(
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
                text = if (cfg.visionReady) {
                    "已配好 —— 待办页右下角「+」可用"
                } else {
                    "还没配全 —— 下面三项都要填，否则「+」会提示来配置"
                },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        Gap()

                SmallTitle(text = "常用模型")
        GroupCard {
            VisionPresets.VISION_MODEL_PRESETS.forEach { preset ->
                ArrowPreference(
                    title = preset.name,
                    summary = preset.note,
                    onClick = {
                                                                        onCfgChange(
                            cfg.copy(
                                visionBaseUrl = preset.baseUrl,
                                visionModel = preset.model,
                            ),
                        )
                        testResult = null
                        testOk = null
                    },
                )
            }
            Note("点一下地址和模型名一起填好，密钥要自己填。")
        }
        Gap()

                SmallTitle(text = "其他地址")
        GroupCard {
            VisionPresets.BASE_URL_EXAMPLES.forEach { preset ->
                ArrowPreference(
                    title = preset.name,
                    summary = preset.note,
                    onClick = {
                                                                        onCfgChange(cfg.copy(visionBaseUrl = preset.baseUrl))
                        testResult = null
                        testOk = null
                    },
                )
            }
            Note("这一栏只填地址；模型名请自己填。")
        }
        Gap()

                SmallTitle(text = "接口参数")
        TextField(
            value = cfg.visionBaseUrl,
            onValueChange = { onCfgChange(cfg.copy(visionBaseUrl = it.trim())) },
            label = "接口地址",
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = cfg.visionApiKey,
            onValueChange = { onCfgChange(cfg.copy(visionApiKey = it.trim())) },
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
            value = cfg.visionModel,
            onValueChange = { onCfgChange(cfg.copy(visionModel = it.trim())) },
            label = "模型名",
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePadding),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "密钥与文本模型不共用。两家服务商通常不是同一家，" +
                "混着填会认证失败。",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = PagePadding),
        )

        Spacer(Modifier.height(16.dp))

                                Button(
            onClick = {
                testing = true
                testResult = null
                testOk = null
                scope.launch {
                    val r = LlmClient.testVision(cfg)
                    testing = false
                    when (r) {
                        is LlmResult.Ok -> {
                            testOk = true
                            testResult = "视觉连接成功，模型看懂了测试图：${r.content.take(60)}"
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
            Text(text = if (testing) "测试中…" else "测试视觉连接（会发一张小图）")
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

                SmallTitle(text = "图片怎么处理")
        GroupCard {
            Note(
                "· 选图用系统的照片选择器，不需要相册权限，你选哪张就只读哪张\n" +
                    "· 图片会先压缩（长边 1024）再发出去，不会传原图\n" +
                    "· 图片不落盘，生成完待办就没了，只留下识别出的标题",
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
