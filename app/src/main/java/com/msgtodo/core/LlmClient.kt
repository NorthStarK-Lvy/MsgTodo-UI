package com.msgtodo.core

import com.msgtodo.data.ApiConfig

sealed interface LlmResult {
    data class Ok(val content: String) : LlmResult
    data class Fatal(val message: String, val httpCode: Int = 0) : LlmResult
    data class Retryable(val message: String) : LlmResult
}

object LlmClient {

    suspend fun test(cfg: ApiConfig): LlmResult = LlmResult.Fatal(
        "demo 不含网络实现 —— 接入你的 OpenAI 兼容接口后此按钮才会真正生效",
    )

    suspend fun testVision(cfg: ApiConfig): LlmResult = LlmResult.Fatal(
        "demo 不含网络实现 —— 接入你的 OpenAI 兼容接口后此按钮才会真正生效",
    )
}
