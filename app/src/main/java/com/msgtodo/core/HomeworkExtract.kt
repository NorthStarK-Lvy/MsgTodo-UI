package com.msgtodo.core

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImagePrep {

    data class Result(val base64: String, val bytes: Int)

    fun prepare(ctx: Context, uri: Uri): Result? = null
}

object HomeworkExtract {

    const val SOURCE = "作业图片"

    sealed interface Outcome {
        data class Saved(
            val title: String,
            val subItems: List<String>,
            val raw: String,
            val dueAt: Long?,
        ) : Outcome

        data object NotTodo : Outcome
        data class Failed(val message: String) : Outcome
    }

    suspend fun run(
        ctx: Context,
        base64Jpeg: String,
        note: String = "",
    ): Outcome = withContext(Dispatchers.IO) {
        Outcome.Failed("demo 不含模型调用实现")
    }
}
