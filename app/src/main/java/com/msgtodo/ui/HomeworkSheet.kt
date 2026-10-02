package com.msgtodo.ui

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.msgtodo.core.HomeworkExtract
import com.msgtodo.core.ImagePrep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeworkSheet(
    onDismiss: () -> Unit,
    onGenerated: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(Stage.Pick) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var imageBytes by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<HomeworkExtract.Outcome?>(null) }
    var prepError by remember { mutableStateOf<String?>(null) }

        val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        pickedUri = uri
        prepError = null
        stage = Stage.Ready
    }

    OverlayDialog(
        show = true,
        title = "从作业生成待办",
        onDismissRequest = { if (stage != Stage.Busy) onDismiss() },
        content = {
            Column {
                when (stage) {
                    Stage.Pick -> PickBody(
                        error = prepError,
                        onPick = {
                            picker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                        onCancel = onDismiss,
                    )

                    Stage.Ready, Stage.Busy -> ReadyBody(
                        ctx = ctx,
                        uri = pickedUri,
                        bytes = imageBytes,
                        note = note,
                        onNoteChange = { note = it },
                        busy = stage == Stage.Busy,
                        error = prepError,
                        onRepick = {
                            picker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                        onSubmit = {
                            val uri = pickedUri ?: return@ReadyBody
                            stage = Stage.Busy
                            prepError = null
                            scope.launch {
                                                                val prep = withContext(Dispatchers.IO) {
                                    ImagePrep.prepare(ctx, uri)
                                }
                                if (prep == null) {
                                    prepError = "这张图读不出来，换一张试试"
                                    stage = Stage.Ready
                                    return@launch
                                }
                                imageBytes = prep.bytes

                                                                val out = HomeworkExtract.run(ctx, prep.base64, note)
                                result = out
                                if (out is HomeworkExtract.Outcome.Saved) onGenerated()
                                stage = Stage.Done
                            }
                        },
                    )

                    Stage.Done -> DoneBody(
                        result = result,
                        onClose = onDismiss,
                        onAgain = {
                            pickedUri = null
                            imageBytes = 0
                            note = ""
                            result = null
                            stage = Stage.Pick
                        },
                    )
                }
            }
        },
    )
}

private enum class Stage { Pick, Ready, Busy, Done }

@Composable
private fun PickBody(error: String?, onPick: () -> Unit, onCancel: () -> Unit) {
    Column {
        Note(
            "选一张作业 / 通知的图片，模型会看懂内容并总结成一条待办。\n" +
                "图片会先压缩再发出去，不会传原图。",
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(text = "取消", onClick = onCancel, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = "选图片",
                onClick = onPick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun ReadyBody(
    ctx: android.content.Context,
    uri: Uri?,
    bytes: Int,
    note: String,
    onNoteChange: (String) -> Unit,
    busy: Boolean,
    error: String?,
    onRepick: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column {
                Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (uri != null) {
                                                                val bmp = remember(uri) { uriToImageBitmap(ctx, uri) }
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "已选择的作业图片",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }

        if (bytes > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "压缩后约 ${bytes / 1024} KB",
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        }

        Spacer(Modifier.height(12.dp))

        TextField(
            value = note,
            onValueChange = onNoteChange,
            label = "补充说明（可留空）",
            singleLine = false,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (busy) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "正在识别…",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceContainer,
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(text = "换一张", onClick = onRepick, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = "生成待办",
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun DoneBody(
    result: HomeworkExtract.Outcome?,
    onClose: () -> Unit,
    onAgain: () -> Unit,
) {
    Column {
        when (result) {
            is HomeworkExtract.Outcome.Saved -> {
                Note("已生成待办：")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = result.title,
                    style = MiuixTheme.textStyles.title4,
                    color = MiuixTheme.colorScheme.onSurfaceContainer,
                )

                                                                if (result.subItems.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    result.subItems.forEachIndexed { idx, s ->
                        Text(
                            text = "${idx + 1}. $s",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                }

                if (result.dueAt != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "时间：" + dueText(result.dueAt),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }

                Spacer(Modifier.height(8.dp))
                Note("识别原文可以在列表点「详情」看到。")
            }

                                    HomeworkExtract.Outcome.NotTodo -> Note(
                "模型看完这张图，判断里面没有需要记住并去做的事。\n" +
                    "如果它漏了，可以在补充说明里写清楚再试一次。",
            )

            is HomeworkExtract.Outcome.Failed -> {
                Text(
                    text = "没能生成：",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.error,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = result.message,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.error,
                )
            }

            null -> Note("没有结果。")
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(text = "再来一张", onClick = onAgain, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = "完成",
                onClick = onClose,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

private fun dueText(ts: Long): String =
    java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
        .format(java.util.Date(ts))

private fun uriToImageBitmap(ctx: android.content.Context, uri: Uri): ImageBitmap? =
    runCatching {
        val source = ImageDecoder.createSource(ctx.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.setTargetSampleSize(2)
        }.asImageBitmap()
    }.getOrNull()
