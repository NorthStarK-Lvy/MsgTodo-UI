package com.msgtodo.core

data class ExtractFailure(
    val conversation: String,
    val preview: String,
    val timeText: String,
    val reason: String,
    val retried: Boolean,
)

data class ExtractStatus(
    val queued: Int = 0,
    val done: Int = 0,
    val todos: Int = 0,
    val skipped: Int = 0,
    val lastError: String? = null,
    val lastOkAt: Long = 0L,
    val lastQueued: String? = null,
    val lastQueuedAt: Long = 0L,
    val processing: String? = null,
    val processingSince: Long = 0L,
    val avgMsPerItem: Long = 0L,
    val failures: List<ExtractFailure> = emptyList(),
    val failedTotal: Int = 0,
) {
    val hasFailures: Boolean get() = failedTotal > 0

    val failureNote: String?
        get() {
            if (failures.isEmpty()) return null
            return buildString {
                append("有 ").append(failedTotal).append(" 条没能提取成待办")
                if (failedTotal > failures.size) {
                    append("（下面只列最近 ").append(failures.size).append(" 条）")
                }
                append("：")
                failures.forEach { f ->
                    append("\n\n· ").append(f.conversation)
                    if (f.timeText.isNotBlank()) append(" · ").append(f.timeText)
                    if (f.preview.isNotBlank()) append(" · ").append(f.preview)
                    append("\n   ")
                    if (!f.retried) append("没有重试（这类错误重试也没用）：")
                    append(f.reason.take(160))
                    if (f.reason.length > 160) append("…")
                }
                append("\n\n原文在「消息」页，完整的请求与返回在「调用记录」里。")
            }
        }
}
