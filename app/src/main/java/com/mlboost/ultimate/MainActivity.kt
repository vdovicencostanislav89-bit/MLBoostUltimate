
package com.mlboost.ultimate

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 36)
            setBackgroundColor(Color.rgb(15, 20, 32))
        }

        fun text(value: String, size: Float, color: Int) {
            layout.addView(TextView(this).apply {
                text = value
                textSize = size
                setTextColor(color)
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 12)
            })
        }

        fun button(label: String, action: () -> Unit) {
            layout.addView(Button(this).apply {
                text = label
                isAllCaps = false
                setOnClickListener { action() }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }
            })
        }

        text("ML BOOST ULTIMATE", 26f, Color.WHITE)
        text("Помощник для Mobile Legends", 16f, Color.LTGRAY)

        info = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }
        layout.addView(info)

        button("Обновить информацию") { updateInfo() }

        button("Открыть Mobile Legends") {
            val intent = packageManager.getLaunchIntentForPackage(
                "com.mobile.legends"
            )
            if (intent != null) startActivity(intent)
            else info.text = "Не удалось найти игру. Открой её вручную."
        }

        button("Открыть настройки телефона") {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }

        text(
            "Совет: закрой лишние приложения, отключи энергосбережение " +
                "и выбери подходящую графику в настройках игры.",
            14f, Color.LTGRAY
        )

        scroll.addView(layout)
        setContentView(scroll)
        updateInfo()
    }

    private fun updateInfo() {
        val battery = registerReceiver(
            null,
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = battery?.getIntExtra("level", -1) ?: -1
        val scale = battery?.getIntExtra("scale", 100) ?: 100
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1

        info.text = "Заряд батареи: " +
            if (percent >= 0) "$percent%" else "нет данных"
    }
}
