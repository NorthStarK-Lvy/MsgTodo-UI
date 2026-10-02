package com.msgtodo.data

import android.content.Context
import com.msgtodo.core.Io
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class MsgRecord(

    val id: Long,
    val pkg: String,

    val conversation: String,

    val sender: String?,

    val isGroup: Boolean,
    val text: String,
    val postedAt: Long,

    val context: List<String>,

    val rawText: String = "",
) {

    val hasBody: Boolean get() = text.isNotBlank()

    val hasRawText: Boolean get() = rawText.isNotBlank() && rawText != text

    fun promptText(): String = buildString {
        append("【会话】").append(conversation)
        if (isGroup) append("（群聊）")
        append('\n')
        if (!sender.isNullOrBlank()) {
            append("【发送者】").append(sender).append('\n')
        }
        if (context.isNotEmpty()) {
            append("【上文】\n")
            context.forEach { append("  ").append(it).append('\n') }
        }
        append("【本条】\n  ").append(text.ifBlank { "（无正文）" }).append('\n')
    }
}

object MsgStore {

    private const val FILE_NAME = "messages.json"

    const val MAX_KEEP = 122

    const val CONTEXT_WINDOW = 10

    private val _messages = MutableStateFlow<List<MsgRecord>>(emptyList())
    val messages: StateFlow<List<MsgRecord>> = _messages.asStateFlow()

    private val recent = HashMap<String, MutableList<String>>()

    private var appCtx: Context? = null

    private var seq = 0L

    @Synchronized
    fun init(context: Context) {
        if (appCtx != null) return
        appCtx = context.applicationContext
        load()
    }

    @Synchronized
    fun record(
        pkg: String,
        conversation: String,
        sender: String?,
        isGroup: Boolean,
        text: String,
        at: Long,
        rawText: String = "",
    ): MsgRecord {
        val key = "$pkg|$conversation"
        val buf = recent.getOrPut(key) { mutableListOf() }

                val ctx = buf.toList()
        if (text.isNotBlank()) {
            buf.add(text)
            while (buf.size > CONTEXT_WINDOW) buf.removeAt(0)
        }

        val rec = MsgRecord(
            id = ++seq,
            pkg = pkg,
            conversation = conversation,
            sender = sender,
            isGroup = isGroup,
            text = text,
            postedAt = at,
            context = ctx,
            rawText = rawText,
        )
        _messages.value = (listOf(rec) + _messages.value).take(MAX_KEEP)
                Io.run { appendToFile(rec) }
        return rec
    }

    @Synchronized
    fun remove(id: Long) {
        _messages.value = _messages.value.filterNot { it.id == id }
        rewriteAll()
    }

    @Synchronized
    fun clearAll() {
        _messages.value = emptyList()
        recent.clear()
        val ctx = appCtx ?: return
        Io.run { runCatching { file(ctx).delete() } }
    }

    fun exportText(): String = buildString {
        append("消息待办 · 已捕获消息\n")
        append("共 ").append(_messages.value.size).append(" 条\n")
        append("=".repeat(48)).append("\n\n")
        _messages.value.asReversed().forEach { m ->
            append(m.promptText()).append('\n')
        }
    }

    private fun file(ctx: Context) = File(ctx.filesDir, FILE_NAME)

    private fun toJson(m: MsgRecord): JSONObject = JSONObject().apply {
        put("id", m.id)
        put("pkg", m.pkg)
        put("conversation", m.conversation)
        put("sender", m.sender ?: JSONObject.NULL)
        put("isGroup", m.isGroup)
        put("text", m.text)
        put("postedAt", m.postedAt)
        put("context", JSONArray(m.context))
                put("rawText", if (m.hasRawText) m.rawText else "")
    }

    private fun appendToFile(m: MsgRecord) {
        val ctx = appCtx ?: return
        file(ctx).appendText(toJson(m).toString() + "\n")
    }

    private fun rewriteAll() {
        val ctx = appCtx ?: return
                val lines = _messages.value.reversed().map { toJson(it).toString() }
        Io.run { runCatching { file(ctx).writeText(lines.joinToString("") { "$it\n" }) } }
    }

    private fun load() {
        val ctx = appCtx ?: return
        runCatching {
            val f = file(ctx)
            if (!f.exists()) return
                        val parsed = ArrayList<MsgRecord>()
            f.forEachLine { line ->
                val t = line.trim()
                if (t.isEmpty()) return@forEachLine
                val o = runCatching { JSONObject(t) }.getOrNull() ?: return@forEachLine
                val c = o.optJSONArray("context")
                val ctxList = ArrayList<String>()
                if (c != null) for (j in 0 until c.length()) ctxList.add(c.optString(j))
                parsed.add(
                    MsgRecord(
                        id = o.optLong("id"),
                        pkg = o.optString("pkg"),
                        conversation = o.optString("conversation"),
                        sender = if (o.isNull("sender")) null else o.optString("sender"),
                        isGroup = o.optBoolean("isGroup"),
                        text = o.optString("text"),
                        postedAt = o.optLong("postedAt"),
                        context = ctxList,
                                                rawText = o.optString("rawText"),
                    ),
                )
            }

                        val recentFirst = parsed.asReversed()
            _messages.value = recentFirst.take(MAX_KEEP)
                        seq = parsed.maxOfOrNull { it.id } ?: 0L

                        parsed.forEach { m ->
                if (m.text.isBlank()) return@forEach
                val key = "${m.pkg}|${m.conversation}"
                val buf = recent.getOrPut(key) { mutableListOf() }
                buf.add(m.text)
                while (buf.size > CONTEXT_WINDOW) buf.removeAt(0)
            }
        }
    }
}
