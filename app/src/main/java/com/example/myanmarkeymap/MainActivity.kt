package com.example.myanmarkeymap

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,64,32,32) }
        layout.addView(TextView(this).apply { text = "Myanmar KeyMap • 0.1.0\nPhysical Keyboard / Myanmar3\nCtrl + Space: Myanmar / English"; textSize = 20f })
        layout.addView(Button(this).apply { text = "1. Enable keyboard"; setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) } })
        layout.addView(Button(this).apply { text = "2. Select Myanmar KeyMap"; setOnClickListener { getSystemService(InputMethodManager::class.java).showInputMethodPicker() } })
        layout.addView(EditText(this).apply { hint = "3. Test: a u s m i f ; → ကျောင်း"; minLines = 3 })
        setContentView(layout)
    }
}
