package com.msgtodo.core

object SecretGuard {

    data class Result(val blocked: Boolean, val reason: String) {
        companion object {
            val CLEAR = Result(false, "")
        }
    }

    private val CREDENTIAL = Regex("默认[\\s\u3000]*[账帐][号户]|默认[\\s\u3000]*密码")

    private val ACCOUNT_WITH_PASSWORD = Regex(
        "[账帐帳][号戶號户][\\s\u3000:：,，、;；=＝]*密码" +
            "|[账帐帳][号戶號户][\\s\u3000:：,，、;；=＝]*" +
            "[^\\n，。；、！？\u4e00-\u9fff]{1,12}\\s*密码",
    )

    private val SCHEME_URL = Regex(
        "(?i)\\b(?:https?|ftps?|ws|wss)://(\\[[^\\]]+\\]|[^\\s/?#:]+)" +
            "|\\bdata:([^,\\s]+)",
    )

    private val HOST_LIKE = Regex(
        "(?i)(?<![\\w@./-])((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.){1,4}([a-z]{2,24}))(?![\\w-])",
    )

    private val PUBLIC_TLDS = setOf(
                "com", "cn", "net", "org", "edu", "gov", "mil", "int",
                "io", "me", "cc", "tv", "co", "ai", "app", "dev", "pro", "vip",
        "top", "xyz", "info", "biz", "site", "online", "shop", "store",
        "club", "live", "fun", "link", "tech", "art", "wiki", "news",
        "blog", "space", "website", "group", "work", "ren", "wang", "xin",
                "uk", "us", "jp", "kr", "de", "fr", "ru", "hk", "tw", "mo",
        "sg", "au", "ca", "in", "br", "it", "es", "nl", "se", "ch",
    )

    fun check(text: String): Result {
        if (text.isBlank()) return Result.CLEAR

                CREDENTIAL.find(text)?.let { m ->
            val v = m.value.replace("\u3000", " ")
            return Result(true, "含「$v」字样 —— 疑似账号口令")
        }

                ACCOUNT_WITH_PASSWORD.find(text)?.let { m ->
            return Result(true, "含「${m.value}」—— 疑似账号口令")
        }

        urlHit(text)?.let { host ->
            return Result(true, "含可打开的网址（$host）")
        }

        return Result.CLEAR
    }

    private fun urlHit(text: String): String? {
                SCHEME_URL.findAll(text).forEach { m ->
                                    val host = m.groupValues.getOrNull(1).orEmpty()
                .ifBlank { m.groupValues.getOrNull(2).orEmpty() }
                                    val isData = m.value.lowercase().startsWith("data:")
            if (isData || !isLocalHost(host)) return shorten(host)
        }
                HOST_LIKE.findAll(text).forEach { m ->
            val host = m.groupValues[1]
            val tld = m.groupValues[2].lowercase()
            if (tld in PUBLIC_TLDS && !isLocalHost(host)) return shorten(host)
        }
        return null
    }

    private fun shorten(host: String): String =
        if (host.length <= 48) host else host.take(48) + "…"

    fun isLocalHost(raw: String): Boolean {
        var h = raw.lowercase().trim()

                if (h.startsWith("[")) {
            val end = h.indexOf(']')
            val v6 = if (end > 0) h.substring(1, end) else h.removePrefix("[")
            return isLocalV6(v6)
        }

                        if (h.contains(':')) {
                        val head = h.substringBefore(':')
            return if (head.contains(':') || head.isEmpty()) {
                isLocalV6(h)
            } else {
                isLocalV4OrName(head)
            }
        }

        return isLocalV4OrName(h)
    }

    private fun isLocalV4OrName(h0: String): Boolean {
        val h = h0.trim()
        if (h.isEmpty()) return false

        if (h == "localhost" || h.endsWith(".localhost")) return true
        if (h.endsWith(".local") || h.endsWith(".lan") || h.endsWith(".internal")) return true

                val parts = h.split('.')
        if (parts.size != 4) return false
        val n = parts.map { it.toIntOrNull() ?: return false }
        if (n.any { it !in 0..255 }) return false

        return n[0] == 10 ||
            n[0] == 127 ||
            (n[0] == 172 && n[1] in 16..31) ||
            (n[0] == 192 && n[1] == 168) ||
            (n[0] == 169 && n[1] == 254)
    }

    private fun isLocalV6(v6: String): Boolean {
        val h = v6.trim().lowercase()
        if (h.isEmpty()) return false
        return h == "::1" || h == "0:0:0:0:0:0:0:1" ||
            h.startsWith("fe80") || h.startsWith("fc") || h.startsWith("fd")
    }
}
