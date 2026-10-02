package com.msgtodo.core

import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object TimeParse {

    private val WEEK_CN = mapOf(
        "一" to Calendar.MONDAY,
        "二" to Calendar.TUESDAY,
        "三" to Calendar.WEDNESDAY,
        "四" to Calendar.THURSDAY,
        "五" to Calendar.FRIDAY,
        "六" to Calendar.SATURDAY,
        "日" to Calendar.SUNDAY,
        "天" to Calendar.SUNDAY,
    )

    private val CN_HOUR = mapOf(
        "一" to 1, "两" to 2, "二" to 2, "三" to 3, "四" to 4, "五" to 5, "六" to 6,
        "七" to 7, "八" to 8, "九" to 9, "十" to 10, "十一" to 11, "十二" to 12,
    )

    private val RE_WEEK = Pattern.compile("(?:周|星期|礼拜)([一二三四五六日天])")

    private val RE_DATE = Pattern.compile(
        "(?:(\\d{4})\\s*年)?\\s*(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*[日号]",
    )
    private val RE_HHMM = Pattern.compile("(\\d{1,2})\\s*[:：]\\s*(\\d{1,2})")
    private val RE_HOUR = Pattern.compile("(\\d{1,2})\\s*[点時时]")
    private val RE_HOUR_CN = Pattern.compile("([一二两三四五六七八九十]{1,3})\\s*[点時时]")

    private val RE_DEADLINE_MARKER = Regex("(?i)截止|截至|deadline")

    private val RE_DEADLINE_SUFFIX = Regex(
        "\\d{1,2}\\s*月\\s*\\d{1,2}\\s*[日号]\\s*(?:之前|前)" +
            "|(?:周|星期|礼拜)[一二三四五六日天]\\s*(?:之前|前)" +
            "|(?:大后天|后天|明天|今天|今晚|明日|今日)\\s*(?:之前|前)",
    )

    private const val WINDOW = 14

    private data class Hit(
        val at: Long,

        val explicitDate: Boolean,

        val explicitYear: Boolean,
    )

    fun extract(text: String, now: Long = System.currentTimeMillis()): Long? =
        parseHit(text, now, rollToFuture = false)?.at

    fun extractDeadline(text: String, now: Long = System.currentTimeMillis()): Long? {
        if (text.isBlank()) return null

                                RE_DEADLINE_MARKER.findAll(text).forEach { m ->
            val from = (m.range.first - WINDOW).coerceAtLeast(0)
            val to = (m.range.last + 1 + WINDOW).coerceAtMost(text.length)
            parseHit(text.substring(from, to), now, rollToFuture = true)?.let { return it.at }
        }

                RE_DEADLINE_SUFFIX.find(text)?.let { m ->
            parseHit(m.value, now, rollToFuture = true)?.let { return it.at }
        }

        return null
    }

    private fun parseHit(text: String, now: Long, rollToFuture: Boolean): Hit? {
        val cal = Calendar.getInstance(Locale.CHINA).apply { timeInMillis = now }

        var dayOffset: Int? = null
        var weekday: Int? = null
        var hour: Int? = null
        var minute = 0
        var explicitMonth: Int? = null
        var explicitDay: Int? = null
        var explicitYear: Int? = null

                when {
            text.contains("大后天") -> dayOffset = 3
            text.contains("后天") -> dayOffset = 2
            text.contains("明天") || text.contains("明日") -> dayOffset = 1
            text.contains("今天") || text.contains("今日") || text.contains("今晚") -> dayOffset = 0
        }

                RE_WEEK.matcher(text).let { m ->
            if (m.find()) m.group(1)?.let { weekday = WEEK_CN[it] }
        }

                RE_DATE.matcher(text).let { m ->
            if (m.find()) {
                explicitYear = m.group(1)?.toIntOrNull()
                explicitMonth = m.group(2)?.toIntOrNull()
                explicitDay = m.group(3)?.toIntOrNull()
            }
        }

                RE_HHMM.matcher(text).let { m ->
            if (m.find()) {
                hour = m.group(1)?.toIntOrNull()
                minute = m.group(2)?.toIntOrNull() ?: 0
            }
        }
        if (hour == null) {
            RE_HOUR.matcher(text).let { m ->
                if (m.find()) hour = m.group(1)?.toIntOrNull()
            }
        }
        if (hour == null) {
            RE_HOUR_CN.matcher(text).let { m ->
                if (m.find()) m.group(1)?.let { hour = CN_HOUR[it] }
            }
        }

                val h = hour
        if (h != null) {
            val isPm = text.contains("下午") || text.contains("晚上") ||
                text.contains("傍晚") || text.contains("夜里") || text.contains("今晚")
            val isAm = text.contains("上午") || text.contains("早上") || text.contains("早晨")
            hour = when {
                isPm && h < 12 -> h + 12
                isAm && h == 12 -> 0
                else -> h
            }
        }

                if (dayOffset == null && weekday == null && explicitMonth == null && hour == null) return null

                        val em = explicitMonth
        val ed = explicitDay
        val ey = explicitYear
        val wd = weekday
        val off = dayOffset

        when {
            em != null && ed != null -> {
                                if (ey != null) cal.set(Calendar.YEAR, ey)
                cal.set(Calendar.MONTH, (em - 1).coerceIn(0, 11))
                cal.set(Calendar.DAY_OF_MONTH, ed.coerceIn(1, 31))
            }

            wd != null -> {
                val cur = cal.get(Calendar.DAY_OF_WEEK)
                var diff = wd - cur
                if (diff <= 0) diff += 7
                cal.add(Calendar.DAY_OF_MONTH, diff)
            }

            off != null -> cal.add(Calendar.DAY_OF_MONTH, off)

            else -> {
                                val target = hour ?: 0
                if (target <= cal.get(Calendar.HOUR_OF_DAY)) cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

                cal.set(Calendar.HOUR_OF_DAY, hour ?: 9)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

                val explicitDate = em != null && ed != null
        if (rollToFuture && explicitDate && ey == null) {
            if (startOfDay(cal.timeInMillis) < startOfDay(now)) {
                cal.add(Calendar.YEAR, 1)
            }
        }

        return Hit(at = cal.timeInMillis, explicitDate = explicitDate, explicitYear = ey != null)
    }

    private fun startOfDay(ts: Long): Long = Calendar.getInstance(Locale.CHINA).apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
