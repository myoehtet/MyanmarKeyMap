package com.example.myanmarkeymap

/** Small visual-order composer. Not a complete Myanmar orthography validator. */
class UnicodeEngine {
    private val raw = mutableListOf<Char>()
    val isEmpty get() = raw.isEmpty()
    private fun base(c: Char) = c in '\u1000'..'\u102A' || c == '\u103F'
    fun accepts(c: Char): Boolean = base(c) || c in '\u102B'..'\u103E'
    fun clear() = raw.clear()
    fun backspace(): Boolean { if (raw.isEmpty()) return false; raw.removeAt(raw.lastIndex); return true }
    // A fresh pre-vowel starts a new syllable unless after a stack marker.
    fun needsBoundary(c: Char): Boolean {
        if (raw.isEmpty()) return false
        if (c == 'ေ') return raw.any(::base) && raw.last() != '္'
        if (base(c)) return raw.any(::base) && raw.last() != '္' && !(raw.last() == 'ေ' && raw.getOrNull(raw.lastIndex-1) == '္')
        return false
    }
    fun add(c: Char) { raw.add(c) }
    fun text(): String {
        if (raw.isEmpty()) return ""
        val out = mutableListOf<Char>()
        var pendingE = false
        for (c in raw) {
            if (c == 'ေ') { pendingE = true; continue }
            out.add(c)
        }
        if (pendingE) {
            val lastBase = out.indexOfLast(::base)
            if (lastBase < 0 || out.lastOrNull() == '္') return out.joinToString("") + "ေ"
            var i = lastBase + 1
            while (i < out.size && out[i] in 'ျ'..'ှ') i++
            out.add(i, 'ေ')
        }
        // Reorder adjacent common marks only; do not reorder across consonants or virama.
        val ranks = "ျြွှေိီဲုူါာံ့း"
        var i = 0
        while (i < out.size) {
            if (out[i] !in ranks) { i++; continue }
            var j = i + 1
            while (j < out.size && out[j] in ranks) j++
            val sorted = out.subList(i,j).sortedBy { ranks.indexOf(it) }
            for (k in sorted.indices) out[i+k] = sorted[k]
            i = j
        }
        return out.joinToString("")
    }
}
