package com.example.myanmarkeymap

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.view.InputDevice
import android.hardware.input.InputManager
import android.os.Handler
import android.os.Looper

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val inputs by lazy { getSystemService(InputManager::class.java) }
    private fun refreshStatus() {
        val names = inputs.inputDeviceIds.toList().mapNotNull { inputs.getInputDevice(it) }.filter { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC }.map { it.name }.distinct()
        status.text = if (names.isEmpty()) "Physical keyboard: Not connected" else "Connected: " + names.joinToString(", ")
    }
    private val listener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(id:Int) = refreshStatus()
        override fun onInputDeviceRemoved(id:Int) = refreshStatus()
        override fun onInputDeviceChanged(id:Int) = refreshStatus()
    }
    override fun onResume() { super.onResume(); refreshStatus(); inputs.registerInputDeviceListener(listener, Handler(Looper.getMainLooper())) }
    override fun onPause() { inputs.unregisterInputDeviceListener(listener); super.onPause() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("keyboard", MODE_PRIVATE)
        fun dp(n:Int) = (n*resources.displayMetrics.density+.5f).toInt()
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20),dp(32),dp(20),dp(24)); setBackgroundColor(Color.parseColor("#F4F6FA")) }
        fun heading(t:String, size:Float=18f) { layout.addView(TextView(this).apply { text=t; textSize=size; setTextColor(Color.parseColor("#192338")); setTypeface(null,Typeface.BOLD); setPadding(0,dp(18),0,dp(8)) }) }
        fun note(t:String) { layout.addView(TextView(this).apply { text=t; textSize=15f; setTextColor(Color.parseColor("#46536B")); setPadding(0,0,0,dp(8)) }) }
        fun button(t:String, action:()->Unit) { layout.addView(Button(this).apply { text=t; isAllCaps=false; setOnClickListener { action() } }) }
        heading("Myanmar KeyMap",28f); note("Version 0.3.1  •  Myanmar3\nPhysical keyboard only · Offline")
        heading("01  Connection & keyboard setup")
        status = TextView(this).apply { textSize=16f; setTextColor(Color.parseColor("#245A45")); setPadding(0,dp(8),0,dp(8)) }; layout.addView(status)
        layout.addView(Switch(this).apply { text="Return to Gboard on disconnect"; setTextColor(Color.parseColor("#192338")); isChecked=prefs.getBoolean("return_gboard",true); setOnCheckedChangeListener { _,v -> prefs.edit().putBoolean("return_gboard",v).apply() } })
        fun option(label: String, key: String) { layout.addView(Switch(this).apply { text=label; setTextColor(Color.parseColor("#192338")); isChecked=prefs.getBoolean(key,true); setOnCheckedChangeListener { _,v -> prefs.edit().putBoolean(key,v).apply() } }) }
        option("Global two-way shortcut", "global_shortcut")
        option("Auto activate on physical keyboard connection", "auto_activate")
        note("Android 11+ required for global switching. Enable both keyboards and the Shortcut Service. The service checks the shortcut only; no typed text is saved or sent.")
        button("Enable global shortcut service") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        note("Enable Gboard and Android’s Use on-screen keyboard option.")
        button("Android physical keyboard settings") { startActivity(Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS)) }
        button("Enable keyboard") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        button("Select input method") { getSystemService(InputMethodManager::class.java).showInputMethodPicker() }
        heading("02  Shortcuts & test")
        note("Ctrl + Space: Myanmar / English. Ctrl + Shift + Space: toggle Gboard / Myanmar KeyMap with Shortcut Service enabled. If unavailable, use the system keyboard picker. Gboard controls its own on-screen visibility.")
        layout.addView(EditText(this).apply { hint="Try typing: ကေ / ကျောင်း"; minLines=3; gravity=android.view.Gravity.TOP; setTextColor(Color.parseColor("#192338")) })
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
