package com.example.myanmarkeymap

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.content.res.Configuration
import android.view.Gravity
import android.view.InputDevice
import android.hardware.input.InputManager
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethod
import android.os.Build

class MyanmarIme : InputMethodService() {
    private val engine = UnicodeEngine()
    private var myanmar = true
    private var permitted = true
    private val inputManager by lazy { getSystemService(InputManager::class.java) }
    private var connected = false
    private var editing = false
    private fun hasHardware() = inputManager.inputDeviceIds.any { id ->
        inputManager.getInputDevice(id)?.let { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC } == true
    }
    private val listener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(id: Int) = refreshHardware()
        override fun onInputDeviceRemoved(id: Int) = refreshHardware()
        override fun onInputDeviceChanged(id: Int) = refreshHardware()
    }
    override fun onCreate() {
        super.onCreate()
        connected = hasHardware()
        inputManager.registerInputDeviceListener(listener, Handler(Looper.getMainLooper()))
    }
    override fun onDestroy() {
        inputManager.unregisterInputDeviceListener(listener)
        super.onDestroy()
    }
    private fun refreshHardware() {
        val now = hasHardware()
        if (now == connected) return
        connected = now
        finishComposition()
        if (!now && editing && prefs.getBoolean("return_gboard", true)) switchToGboard()
    }
    private fun switchToGboard() {
        finishComposition()
        val imm = getSystemService(InputMethodManager::class.java)
        val gboard = imm.enabledInputMethodList.firstOrNull { it.packageName == "com.google.android.inputmethod.latin" }
        if (gboard == null) {
            Toast.makeText(this, "Enable Gboard in keyboard settings", Toast.LENGTH_SHORT).show()
            imm.showInputMethodPicker()
            return
        }
        try { switchInputMethod(gboard.id) }
        catch (e: SecurityException) { imm.showInputMethodPicker() }
        catch (e: IllegalArgumentException) { imm.showInputMethodPicker() }
    }
    private val consumed = mutableSetOf<Pair<Int,Int>>()
    private fun finishComposition() { currentInputConnection?.finishComposingText(); engine.clear() }
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        editing = true
        connected = hasHardware()
        engine.clear()
        val type = attribute?.inputType ?: 0
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        permitted = cls == InputType.TYPE_CLASS_TEXT && variation !in setOf(InputType.TYPE_TEXT_VARIATION_PASSWORD, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, InputType.TYPE_TEXT_VARIATION_URI)
        if (!connected && prefs.getBoolean("return_gboard", true)) {
            Handler(Looper.getMainLooper()).post { if (editing && !hasHardware()) switchToGboard() }
        }
    }
    override fun onFinishInput() { editing = false; finishComposition(); consumed.clear(); super.onFinishInput() }
    private val prefs get() = getSharedPreferences("keyboard", MODE_PRIVATE)
    override fun onEvaluateInputViewShown(): Boolean = false
    private fun toggleLanguage() { finishComposition(); myanmar = !myanmar }
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
        if (!connected || event.device?.let { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC } != true) return super.onKeyDown(keyCode,event)
        val id = event.deviceId to keyCode
        fun handled(): Boolean { consumed.add(id); return true }
        if (keyCode == KeyEvent.KEYCODE_SPACE && event.isCtrlPressed && event.isShiftPressed && !event.isAltPressed && !event.isMetaPressed) {
            if (event.repeatCount == 0) switchToGboard()
            return handled()
        }
        if (keyCode == KeyEvent.KEYCODE_SPACE && event.isCtrlPressed && !event.isAltPressed && !event.isMetaPressed) {
            if (event.repeatCount == 0) { toggleLanguage(); Toast.makeText(this, if (myanmar) "မြန်မာ" else "English", Toast.LENGTH_SHORT).show() }
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
