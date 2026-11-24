package com.example.myanmarkeymap

import android.accessibilityservice.AccessibilityService
import android.hardware.input.InputManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.InputDevice
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

class ShortcutService : AccessibilityService() {
    private val inputs by lazy { getSystemService(InputManager::class.java) }
    private val imm by lazy { getSystemService(InputMethodManager::class.java) }
    private val prefs get() = getSharedPreferences("keyboard", MODE_PRIVATE)
    private var connected = false
    private var registered = false
    private val held = mutableSetOf<Int>()
    private fun hardware() = inputs.inputDeviceIds.any { id ->
        inputs.getInputDevice(id)?.let { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC } == true
    }
    private fun current() = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    private fun ownId() = imm.enabledInputMethodList.firstOrNull { it.packageName == packageName }?.id
    private fun gboardId() = imm.enabledInputMethodList.firstOrNull { it.packageName == "com.google.android.inputmethod.latin" }?.id
    private fun change(toOwn: Boolean) {
        if (Build.VERSION.SDK_INT < 30) return
        val target = if (toOwn) ownId() else gboardId()
        if (target == null) {
            Toast.makeText(this, if (toOwn) "Enable Myanmar KeyMap first" else "Enable Gboard first", Toast.LENGTH_SHORT).show()
            return
        }
        if (current() == target) return
        if (!softKeyboardController.switchToInputMethod(target)) {
            Toast.makeText(this, "Switch failed; use keyboard picker", Toast.LENGTH_SHORT).show()
        } else if (!toOwn) {
            softKeyboardController.setShowMode(SHOW_MODE_IGNORE_HARD_KEYBOARD)
        }
    }
    private fun refresh() {
        val now = hardware()
        if (now == connected) return
        connected = now
        held.clear()
        if (now && prefs.getBoolean("auto_activate", true)) change(true)
        if (!now && prefs.getBoolean("return_gboard", true) && current() == ownId()) change(false)
    }
    private val listener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(id: Int) = refresh()
        override fun onInputDeviceRemoved(id: Int) = refresh()
        override fun onInputDeviceChanged(id: Int) = refresh()
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        connected = hardware()
        if (!registered) { inputs.registerInputDeviceListener(listener, Handler(Looper.getMainLooper())); registered = true }
        if (connected && prefs.getBoolean("auto_activate", true)) change(true)
    }
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Never read characters or editor content. Only consume the selected shortcut pair.
        if (event.keyCode != KeyEvent.KEYCODE_SPACE) return false
        if (event.action == KeyEvent.ACTION_UP && held.remove(event.deviceId)) return true
        if (event.action != KeyEvent.ACTION_DOWN) return false
        if (held.contains(event.deviceId)) return true
        if (Build.VERSION.SDK_INT < 30 || !prefs.getBoolean("global_shortcut", true) || !connected) return false
        val device = event.device ?: return false
        if (device.isVirtual || device.keyboardType != InputDevice.KEYBOARD_TYPE_ALPHABETIC) return false
        if (!event.isCtrlPressed || !event.isShiftPressed || event.isAltPressed || event.isMetaPressed || event.repeatCount != 0) return false
        val own = ownId() ?: return false
        held.add(event.deviceId)
        change(current() != own)
        return true
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { }
    override fun onInterrupt() { }
    override fun onDestroy() {
        if (registered) inputs.unregisterInputDeviceListener(listener)
        held.clear()
        super.onDestroy()
    }
}
