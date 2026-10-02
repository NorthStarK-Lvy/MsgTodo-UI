package com.msgtodo.data

data class CallRecord(
    val id: Long,
    val at: Long,

    val conversation: String,

    val system: String,

    val user: String,

    val reply: String?,

    val error: String?,

    val isTodo: Boolean?,

    val title: String?,

    val due: String?,

    val saved: Boolean,
) {

    val stage: Stage
        get() = when {
            error != null -> Stage.REQUEST_FAILED
            reply.isNullOrBlank() -> Stage.EMPTY_REPLY
            isTodo == null -> Stage.PARSE_FAILED
            saved -> Stage.SAVED
            isTodo -> Stage.TODO_NOT_SAVED
            else -> Stage.NOT_TODO
        }

    enum class Stage(val label: String) {
        SAVED("已生成待办"),
        NOT_TODO("模型判定：不是待办"),
        TODO_NOT_SAVED("是待办，但没写进库"),
        PARSE_FAILED("返回内容解析不了"),
        EMPTY_REPLY("返回内容为空"),
        REQUEST_FAILED("请求失败"),
    }
}

object DailyCounter {

    fun baseAfterRollover(storedDate: String?, storedCount: Int, today: String): Int =
        if (storedDate == today) storedCount.coerceAtLeast(0) else 0

    fun next(storedDate: String?, storedCount: Int, today: String): Pair<String, Int> =
        today to (baseAfterRollover(storedDate, storedCount, today) + 1)

    fun reached(count: Int, limit: Int): Boolean = limit > 0 && count >= limit
}
