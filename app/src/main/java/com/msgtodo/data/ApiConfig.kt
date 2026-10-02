package com.msgtodo.data

import android.content.Context

data class ApiConfig(
    val enabled: Boolean,

    val baseUrl: String,
    val apiKey: String,
    val model: String,

    val dailyLimit: Int,

    val visionBaseUrl: String = "",
    val visionApiKey: String = "",
    val visionModel: String = "",
) {

    val ready: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()

    val visionReady: Boolean
        get() = visionBaseUrl.isNotBlank() &&
            visionApiKey.isNotBlank() &&
            visionModel.isNotBlank()
}

data class ApiPreset(
    val name: String,
    val baseUrl: String,
    val model: String,

    val note: String? = null,
)

object VisionPresets {

    const val DASHSCOPE_BASE_URL: String =
        "https://dashscope.aliyuncs.com/compatible-mode/v1"

    val VISION_MODEL_PRESETS: List<ApiPreset> = listOf(
        ApiPreset(
            name = "qwen-vl-plus",
            baseUrl = DASHSCOPE_BASE_URL,
            model = "qwen-vl-plus",
            note = "便宜、够用。识别作业题目推荐从这个开始",
        ),
        ApiPreset(
            name = "qwen-vl-max",
            baseUrl = DASHSCOPE_BASE_URL,
            model = "qwen-vl-max",
            note = "更准，贵一些。图面复杂 / 字迹潦草时用它",
        ),
    )

    val BASE_URL_EXAMPLES: List<ApiPreset> = listOf(
        ApiPreset(
            name = "阿里云百炼",
            baseUrl = DASHSCOPE_BASE_URL,
            model = "",
            note = "读图模型可填 qwen-vl-plus 或 qwen-vl-max",
        ),
        ApiPreset(
            name = "硅基流动",
            baseUrl = "https://api.siliconflow.cn/v1",
            model = "",
            note = "一个 key 调多家模型；模型名去官网抄",
        ),
        ApiPreset(
            name = "自定义",
            baseUrl = "",
            model = "",
            note = "填任意 OpenAI 兼容地址（含各类中转站）",
        ),
    )
}

data class DailyLimitOption(val value: Int, val label: String)

object ApiConfigStore {

    private const val NAME = "msgtodo_api"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MODEL = "model"
    private const val KEY_DAILY_LIMIT = "daily_limit"
        private const val KEY_VISION_BASE_URL = "vision_base_url"
    private const val KEY_VISION_API_KEY = "vision_api_key"
    private const val KEY_VISION_MODEL = "vision_model"

    val PRESETS: List<ApiPreset> = listOf(
        ApiPreset(
            name = "DeepSeek 官方",
            baseUrl = "https://api.deepseek.com",
            model = "deepseek-flash",
                    ),
        ApiPreset(
            name = "阿里云百炼",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            model = "qwen-plus",
                    ),
        ApiPreset(
            name = "硅基流动",
            baseUrl = "https://api.siliconflow.cn/v1",
            model = "deepseek-ai/DeepSeek-V3",
            note = "一个 key 调多家模型；据称注册送赠金，请以官网为准",
        ),
        ApiPreset(
            name = "自定义",
            baseUrl = "",
            model = "",
            note = "填任意 OpenAI 兼容地址（含各类中转站）",
        ),
    )

    const val RELAY_WARNING: String =
        "额度、稳定性和售后都没有保障。\n\n" +
            "我们的用量一个月才几块钱，不值得为省这点钱冒这个险。"

    val DAILY_LIMIT_OPTIONS: List<DailyLimitOption> = listOf(
        DailyLimitOption(50, "50 次"),
        DailyLimitOption(100, "100 次"),
        DailyLimitOption(200, "200 次"),
        DailyLimitOption(500, "500 次"),
        DailyLimitOption(1000, "1000 次"),
        DailyLimitOption(0, "不限制"),
    )

    private fun sp(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun load(ctx: Context): ApiConfig {
        val p = sp(ctx)
        return ApiConfig(
            enabled = p.getBoolean(KEY_ENABLED, false),
            baseUrl = p.getString(KEY_BASE_URL, null) ?: PRESETS[0].baseUrl,
            apiKey = p.getString(KEY_API_KEY, null).orEmpty(),
            model = p.getString(KEY_MODEL, null) ?: PRESETS[0].model,
            dailyLimit = p.getInt(KEY_DAILY_LIMIT, 200),
                                                visionBaseUrl = p.getString(KEY_VISION_BASE_URL, null).orEmpty(),
            visionApiKey = p.getString(KEY_VISION_API_KEY, null).orEmpty(),
            visionModel = p.getString(KEY_VISION_MODEL, null).orEmpty(),
        )
    }

    fun save(ctx: Context, cfg: ApiConfig) {
        sp(ctx).edit()
            .putBoolean(KEY_ENABLED, cfg.enabled)
            .putString(KEY_BASE_URL, cfg.baseUrl.trim())
            .putString(KEY_API_KEY, cfg.apiKey.trim())
            .putString(KEY_MODEL, cfg.model.trim())
            .putInt(KEY_DAILY_LIMIT, cfg.dailyLimit)
            .putString(KEY_VISION_BASE_URL, cfg.visionBaseUrl.trim())
            .putString(KEY_VISION_API_KEY, cfg.visionApiKey.trim())
            .putString(KEY_VISION_MODEL, cfg.visionModel.trim())
            .apply()
    }

    fun maskKey(key: String): String = when {
        key.isBlank() -> "（未填写）"
        key.length <= 12 -> "•".repeat(key.length)
        else -> key.take(6) + "•".repeat(8) + key.takeLast(4)
    }
}
