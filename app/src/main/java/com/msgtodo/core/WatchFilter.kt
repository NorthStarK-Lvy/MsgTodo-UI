package com.msgtodo.core

object WatchFilter {

    data class Rule(

        val filterEnabled: Boolean = false,

        val conversations: Set<String> = emptySet(),

        val senders: Set<String> = emptySet(),

        val alwaysMentionAll: Boolean = true,

        val displayNames: Map<String, String> = emptyMap(),
    ) {
        val isFiltering: Boolean get() = filterEnabled

        val activeList: Set<String> get() = conversations + senders

        val isListEmpty: Boolean get() = conversations.isEmpty() && senders.isEmpty()
    }

    data class Decision(val send: Boolean, val reason: String)

    private val MENTION_ALL_MARKERS = listOf("@所有人", "@全体成员", "[有全体消息]")

    fun mentionsAll(text: String): Boolean {
        if (text.isBlank()) return false
        val t = normalize(text)
        return MENTION_ALL_MARKERS.any { t.contains(it) }
    }

    fun decide(
        rule: Rule,
        conversation: String,
        sender: String?,
        text: String = "",
        isGroup: Boolean = false,
    ): Decision {
                if (!rule.filterEnabled) {
            return Decision(true, "未启用范围限制，全部总结")
        }

                                                                                                                                                                if (rule.alwaysMentionAll && mentionsAll(text)) {
            return Decision(true, "含「@所有人」，一律总结")
        }

        val conv = normalize(conversation)

                if (conv.isNotEmpty() && conv in rule.conversations) {
            return Decision(true, "会话「$conversation」在总结范围内")
        }

        val who = normalize(sender.orEmpty())

                if (who.isNotEmpty() && who in rule.senders) {
            return Decision(true, "发言人「$sender」在总结范围内")
        }

                                        if (who.isEmpty() && conv.isNotEmpty() && conv in rule.senders) {
            return Decision(true, "「$conversation」在总结范围内（单聊）")
        }

                return if (rule.isListEmpty) {
            Decision(false, "总结范围是空的，去「总结范围」里勾选")
        } else {
            Decision(false, "不在总结范围内")
        }
    }

    private val INVISIBLE = setOf(
        0x200B,          0x200C,          0x200D,          0xFEFF,          0x00A0,          0x3000,      )

    fun normalize(s: String): String = s
        .filterNot { it.code in INVISIBLE || it.isWhitespace() }
        .lowercase()

    fun parseInput(raw: String): Set<String> =
        raw.split(',', '，', '、', '\n', ';', '；')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    fun normalizeSet(raw: Set<String>): Set<String> =
        raw.map { normalize(it) }.filter { it.isNotBlank() }.toSet()

    fun displayNameOf(
        picked: Set<String>,
        candidates: List<String>,
        stored: Map<String, String> = emptyMap(),
    ): Map<String, String> {
        if (picked.isEmpty()) return emptyMap()
        val out = LinkedHashMap<String, String>()
                candidates.forEach { raw ->
            val k = normalize(raw)
            if (k in picked) out.putIfAbsent(k, raw)
        }
                picked.forEach { k -> out.putIfAbsent(k, k) }
                        stored.forEach { (k, name) ->
            if (k in picked && name.isNotBlank()) out[k] = name
        }
        return out
    }

    fun unresolvedCount(
        picked: Set<String>,
        candidates: List<String>,
        stored: Map<String, String> = emptyMap(),
    ): Int {
        if (picked.isEmpty()) return 0
        val known = candidates.map { normalize(it) }.toSet()
        return picked.count { it !in known && stored[it].isNullOrBlank() }
    }

    fun mergeNames(
        keys: Set<String>,
        raws: Iterable<String>,
        stored: Map<String, String>,
    ): Map<String, String> {
        val out = LinkedHashMap<String, String>()
                stored.forEach { (k, v) -> if (k in keys && v.isNotBlank()) out[k] = v }
                raws.forEach { raw ->
            val k = normalize(raw)
            if (k.isNotBlank() && k in keys && raw.isNotBlank()) out[k] = raw
        }
        return out
    }
}
