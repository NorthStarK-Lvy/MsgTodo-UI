package com.msgtodo.data

import android.content.Context
import com.msgtodo.core.Io
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class TodoItem(
    val id: Long,

    val source: String,
    val sender: String?,

    val rawText: String,

    val title: String,

    val subItems: List<String> = emptyList(),

    val dueAt: Long?,
    val createdAt: Long,
    val done: Boolean,

    val doneAt: Long? = null,
)

object TodoStore {

    private const val FILE_NAME = "todos.jsonl"

    private const val MAX_KEEP = 500

    private const val DONE_TTL_MS = 24L * 60 * 60 * 1000

    private val _items = MutableStateFlow<List<TodoItem>>(emptyList())
    val items: StateFlow<List<TodoItem>> = _items.asStateFlow()

    private var appCtx: Context? = null
    private var seq = 0L

    @Synchronized
    fun init(context: Context) {
        if (appCtx != null) return
        appCtx = context.applicationContext
        load()
                        pruneStaleDone()
    }

    @Synchronized
    fun add(item: TodoItem) {
        _items.value = (listOf(item) + _items.value).take(MAX_KEEP)
        val snapshot = item
        Io.run { appendToFile(snapshot) }
    }

    @Synchronized
    fun save(item: TodoItem) {
        if (item.id == 0L) {
            add(item.copy(id = nextId()))
        } else {
            _items.value = _items.value.map { if (it.id == item.id) item else it }
            rewriteAll()
        }
    }

    @Synchronized
    fun setDone(id: Long, done: Boolean) {
        val now = System.currentTimeMillis()
        _items.value = _items.value.map {
            if (it.id == id) it.copy(done = done, doneAt = if (done) now else null) else it
        }
        pruneStaleDone()
        rewriteAll()
    }

    @Synchronized
    fun pruneStaleDone(now: Long = System.currentTimeMillis()) {
        val cutoff = now - DONE_TTL_MS
        val kept = _items.value.filterNot { it.done && (it.doneAt ?: it.createdAt) < cutoff }
        if (kept.size != _items.value.size) _items.value = kept
    }

    @Synchronized
    fun remove(id: Long) {
        _items.value = _items.value.filterNot { it.id == id }
        rewriteAll()
    }

    @Synchronized
    fun clearDone() {
        _items.value = _items.value.filterNot { it.done }
        rewriteAll()
    }

    @Synchronized
    fun clearAll() {
        _items.value = emptyList()
        seq = 0
        val ctx = appCtx ?: return
        Io.run { runCatching { file(ctx).delete() } }
    }

    @Synchronized
    fun nextId(): Long = ++seq

    private fun file(ctx: Context) = File(ctx.filesDir, FILE_NAME)

    private fun toJson(t: TodoItem): JSONObject = JSONObject().apply {
        put("id", t.id)
        put("source", t.source)
        put("sender", t.sender ?: JSONObject.NULL)
        put("rawText", t.rawText)
        put("title", t.title)
                        put("subItems", JSONArray(t.subItems))
        put("dueAt", t.dueAt ?: JSONObject.NULL)
        put("createdAt", t.createdAt)
        put("done", t.done)
        put("doneAt", t.doneAt ?: JSONObject.NULL)
    }

    private fun appendToFile(t: TodoItem) {
        val ctx = appCtx ?: return
        file(ctx).appendText(toJson(t).toString() + "\n")
    }

    private fun rewriteAll() {
        val ctx = appCtx ?: return
        val lines = _items.value.reversed().map { toJson(it).toString() }
        Io.run { runCatching { file(ctx).writeText(lines.joinToString("") { "$it\n" }) } }
    }

    private fun load() {
        val ctx = appCtx ?: return
        runCatching {
            val f = file(ctx)
            if (!f.exists()) return
            val parsed = ArrayList<TodoItem>()
            f.forEachLine { line ->
                val t = line.trim()
                if (t.isEmpty()) return@forEachLine
                val o = runCatching { JSONObject(t) }.getOrNull() ?: return@forEachLine
                parsed.add(
                    TodoItem(
                        id = o.optLong("id"),
                        source = o.optString("source"),
                        sender = if (o.isNull("sender")) null else o.optString("sender"),
                        rawText = o.optString("rawText"),
                        title = o.optString("title"),
                                                                        subItems = readSubItems(o.optJSONArray("subItems")),
                        dueAt = if (o.isNull("dueAt")) null else o.optLong("dueAt"),
                        createdAt = o.optLong("createdAt"),
                        done = o.optBoolean("done"),
                        doneAt = if (o.isNull("doneAt")) null else o.optLong("doneAt"),
                    ),
                )
            }
            _items.value = parsed.asReversed().take(MAX_KEEP)
            seq = parsed.maxOfOrNull { it.id } ?: 0L
        }
    }

    private fun readSubItems(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val out = ArrayList<String>(arr.length())
        for (k in 0 until arr.length()) {
            val s = arr.optString(k).trim()
            if (s.isNotEmpty()) out += s
        }
        return out
    }
}
