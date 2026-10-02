package com.msgtodo.data

import android.content.Context
import com.msgtodo.core.WatchFilter

object Prefs {

    private const val NAME = "msgtodo_prefs"
    private const val KEY_WHITELIST = "whitelist"
    private const val KEY_PROBE = "probe_log"

    private const val KEY_FLOATING_NAV = "floating_nav"

        private const val KEY_WATCH_MODE = "watch_mode"
    private const val KEY_WATCH_CONV = "watch_conversations"
    private const val KEY_WATCH_SENDER = "watch_senders"

    private const val KEY_WATCH_MENTION_ALL = "watch_mention_all"

    private const val KEY_WATCH_NAMES = "watch_names"

    val DEFAULT_WHITELIST: Set<String> = setOf("com.tencent.mm")

    data class MonitoredApp(val pkg: String, val name: String, val note: String)

    val MONITORED_APPS: List<MonitoredApp> = listOf(
        MonitoredApp(
            pkg = "com.tencent.mm",
            name = "微信",
            note = "含群聊与单聊",
        ),
        MonitoredApp(
            pkg = "com.tencent.mobileqq",
            name = "QQ",
            note = "含群聊与单聊",
        ),
    )

    private fun sp(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun whitelist(ctx: Context): Set<String> =
        sp(ctx).getStringSet(KEY_WHITELIST, null)?.toSet() ?: DEFAULT_WHITELIST

    fun setWhitelist(ctx: Context, v: Set<String>) {
        sp(ctx).edit().putStringSet(KEY_WHITELIST, v.toSet()).apply()
    }

    fun watchRule(ctx: Context): WatchFilter.Rule {
        val p = sp(ctx)
        val modeName = p.getString(KEY_WATCH_MODE, null)

                        val filterEnabled = modeName != null && modeName != LEGACY_MODE_ALL

        return WatchFilter.Rule(
            filterEnabled = filterEnabled,
            conversations = p.getStringSet(KEY_WATCH_CONV, null)?.toSet() ?: emptySet(),
            senders = p.getStringSet(KEY_WATCH_SENDER, null)?.toSet() ?: emptySet(),
            alwaysMentionAll = p.getBoolean(KEY_WATCH_MENTION_ALL, true),
            displayNames = readNames(p.getString(KEY_WATCH_NAMES, null)),
        )
    }

    private fun readNames(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        val out = LinkedHashMap<String, String>()
        raw.lineSequence().forEach { line ->
            val i = line.indexOf('\t')
            if (i > 0) {
                val k = line.substring(0, i)
                val v = line.substring(i + 1)
                if (k.isNotBlank() && v.isNotBlank()) out[k] = v
            }
        }
        return out
    }

    private fun writeNames(m: Map<String, String>): String =
        m.entries.joinToString("\n") { "${it.key}\t${it.value}" }

    fun setWatchNames(ctx: Context, m: Map<String, String>) {
        sp(ctx).edit().putString(KEY_WATCH_NAMES, writeNames(m)).apply()
    }

    fun setWatchFilterEnabled(ctx: Context, enabled: Boolean) {
                sp(ctx).edit()
            .putString(KEY_WATCH_MODE, if (enabled) LEGACY_MODE_PICKED else LEGACY_MODE_ALL)
            .apply()
    }

    fun setWatchConversations(ctx: Context, v: Set<String>) {
        sp(ctx).edit().putStringSet(KEY_WATCH_CONV, v.toSet()).apply()
    }

    fun setWatchSenders(ctx: Context, v: Set<String>) {
        sp(ctx).edit().putStringSet(KEY_WATCH_SENDER, v.toSet()).apply()
    }

    fun setWatchMentionAll(ctx: Context, v: Boolean) {
        sp(ctx).edit().putBoolean(KEY_WATCH_MENTION_ALL, v).apply()
    }

    private const val LEGACY_MODE_ALL = "ALL"
    private const val LEGACY_MODE_PICKED = "PICKED"

    fun probeLog(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_PROBE, true)

    fun setProbeLog(ctx: Context, v: Boolean) {
        sp(ctx).edit().putBoolean(KEY_PROBE, v).apply()
    }

    fun floatingNav(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_FLOATING_NAV, false)

    fun setFloatingNav(ctx: Context, v: Boolean) {
        sp(ctx).edit().putBoolean(KEY_FLOATING_NAV, v).apply()
    }

        private const val KEY_THEME_MODE = "theme_mode"

    enum class ThemeMode { SYSTEM, LIGHT, DARK }

    fun themeMode(ctx: Context): ThemeMode =
        runCatching { ThemeMode.valueOf(sp(ctx).getString(KEY_THEME_MODE, null) ?: "") }
            .getOrNull() ?: ThemeMode.SYSTEM

    fun setThemeMode(ctx: Context, v: ThemeMode) {
        sp(ctx).edit().putString(KEY_THEME_MODE, v.name).apply()
    }

    data class Snapshot(
        val whitelist: Set<String>,
        val probeLog: Boolean,
        val watch: WatchFilter.Rule,
    )

    fun snapshot(ctx: Context) = Snapshot(
        whitelist = whitelist(ctx),
        probeLog = probeLog(ctx),
        watch = watchRule(ctx),
    )
}
