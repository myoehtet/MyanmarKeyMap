package com.example.myanmarkeymap

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.Toast

class MyanmarIme : InputMethodService() {
    private val engine = UnicodeEngine()
    private var myanmar = true
    private var permitted = true
    private val consumed = mutableSetOf<Pair<Int,Int>>()
    private fun finishComposition() { currentInputConnection?.finishComposingText(); engine.clear() }
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        engine.clear()
        val type = attribute?.inputType ?: 0
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        permitted = cls == InputType.TYPE_CLASS_TEXT && variation !in setOf(InputType.TYPE_TEXT_VARIATION_PASSWORD, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, InputType.TYPE_TEXT_VARIATION_URI)
    }
    override fun onFinishInput() { finishComposition(); consumed.clear(); super.onFinishInput() }
    override fun onEvaluateInputViewShown(): Boolean = false
    override fun onEvaluateFullscreenMode(): Boolean = false
    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart,oldSelEnd,newSelStart,newSelEnd,candidatesStart,candidatesEnd)
        if (!engine.isEmpty && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) finishComposition()
    }
    private fun physicalKey(code: Int): Char? = when (code) {
        in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z -> 'a' + (code-KeyEvent.KEYCODE_A)
        in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> '0' + (code-KeyEvent.KEYCODE_0)
        KeyEvent.KEYCODE_GRAVE -> '`'; KeyEvent.KEYCODE_MINUS -> '-'; KeyEvent.KEYCODE_EQUALS -> '='
        KeyEvent.KEYCODE_LEFT_BRACKET -> '['; KeyEvent.KEYCODE_RIGHT_BRACKET -> ']'; KeyEvent.KEYCODE_BACKSLASH -> '\\'
        KeyEvent.KEYCODE_SEMICOLON -> ';'; KeyEvent.KEYCODE_APOSTROPHE -> '\''; KeyEvent.KEYCODE_COMMA -> ','
        KeyEvent.KEYCODE_PERIOD -> '.'; KeyEvent.KEYCODE_SLASH -> '/'; else -> null
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val id = event.deviceId to keyCode
        fun handled(): Boolean { consumed.add(id); return true }
        if (keyCode == KeyEvent.KEYCODE_SPACE && event.isCtrlPressed && !event.isAltPressed && !event.isMetaPressed) {
            if (event.repeatCount == 0) { finishComposition(); myanmar = !myanmar; Toast.makeText(this, if (myanmar) "မြန်မာ" else "English", Toast.LENGTH_SHORT).show() }
            return handled()
        }
        val ic = currentInputConnection ?: return super.onKeyDown(keyCode,event)
        if (!myanmar || !permitted || event.isCtrlPressed || event.isAltPressed || event.isMetaPressed) { finishComposition(); return super.onKeyDown(keyCode,event) }
        if (keyCode == KeyEvent.KEYCODE_DEL && engine.backspace()) { ic.setComposingText(engine.text(),1); if (engine.isEmpty) ic.finishComposingText(); return handled() }
        val key = physicalKey(keyCode)
        val mapped = key?.let { MyanmarLayout.map(it,event.isShiftPressed) }
        if (mapped != null) {
            if (engine.accepts(mapped)) {
                if (engine.needsBoundary(mapped)) finishComposition()
                engine.add(mapped)
                ic.setComposingText(engine.text(),1)
            } else { finishComposition(); ic.commitText(mapped.toString(),1) }
            return handled()
        }
        if (!KeyEvent.isModifierKey(keyCode)) finishComposition()
        return super.onKeyDown(keyCode,event)
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (consumed.remove(event.deviceId to keyCode)) return true
        return super.onKeyUp(keyCode,event)
    }
}
