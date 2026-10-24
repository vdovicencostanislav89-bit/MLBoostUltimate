package com.mlboost.ultimate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Debug
import android.os.StatFs
import android.os.Environment
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.net.InetAddress
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val bg = Color.rgb(10, 14, 28)
    private val panel = Color.rgb(22, 29, 51)
    private val cyan = Color.rgb(75, 220, 255)
    private val white = Color.rgb(242, 246, 255)
    private val muted = Color.rgb(170, 184, 210)
    private lateinit var statusText: TextView
    private lateinit var ramText: TextView
    private lateinit var batteryText: TextView
    private lateinit var storageText: TextView
    private lateinit var pingText: TextView
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refreshStats()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(24))
        }
        scroll.addView(root)
        setContentView(scroll)

        val title = TextView(this).apply {
            text = "ML BOOST"
            textSize = 30f
            setTextColor(cyan)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWrap())
        root.addView(TextView(this).apply {
            text = "ULTIMATE  •  MOBILE LEGENDS PROFILE"
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(18))
        }, matchWrap())

        statusText = addPanel(root, "GAME STATUS", "Ready to prepare your phone")
        addButton(root, "⚡  QUICK CHECK", "Updates available device stats") { refreshStats() }
        addButton(root, "🎮  LAUNCH MOBILE LEGENDS", "Opens the installed game if available") { launchGame() }
        addButton(root, "📶  PING TEST", "Tests basic network response to a public host") { runPingTest() }

        ramText = addPanel(root, "MEMORY", "Reading…")
        batteryText = addPanel(root, "BATTERY", "Reading…")
        storageText = addPanel(root, "STORAGE", "Reading…")
        pingText = addPanel(root, "NETWORK TEST", "Tap PING TEST to run")

        addPanel(root, "PERFORMANCE PROFILE",
            "Recommended: set the highest in-game frame rate available, use medium graphics if FPS drops, disable Battery Saver during play, and keep the phone cool.")
        addButton(root, "⚙️  OPEN DISPLAY SETTINGS", "Set the highest refresh rate available") {
            openSettings("android.settings.DISPLAY_SETTINGS")
        }
        addButton(root, "🔋  OPEN BATTERY SETTINGS", "Check battery and power mode") {
            openSettings("android.settings.BATTERY_SAVER_SETTINGS")
        }
        addButton(root, "🛡️  OPEN APP SETTINGS", "Manage permissions and battery options") {
            openSettings("android.settings.APPLICATION_DETAILS_SETTINGS", "package:$packageName")
        }

        addPanel(root, "IMPORTANT",
            "Android does not let ordinary apps force the CPU/GPU to maximum speed, unlock game FPS, or safely clear all RAM. This app only reads basic stats, runs a simple network test, opens settings, and launches the game.")
        addPanel(root, "MOBILE LEGENDS TIPS",
            "• FPS: choose the highest option shown in the game.\n• Graphics: use medium/low if team fights stutter.\n• Heat: avoid playing while charging if the phone gets hot.\n• Network: use a stable Wi‑Fi connection or strong mobile signal.")
    }

    private fun addPanel(root: LinearLayout, heading: String, body: String): TextView {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(panel)
        }
        val h = TextView(this).apply {
            text = heading
            textSize = 12f
            setTextColor(cyan)
            typeface = Typeface.DEFAULT_BOLD
        }
        val b = TextView(this).apply {
            text = body
            textSize = 15f
            setTextColor(white)
            setPadding(0, dp(6), 0, 0)
        }
        container.addView(h, matchWrap())
        container.addView(b, matchWrap())
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        root.addView(container, lp)
        when (heading) {
            "GAME STATUS" -> statusText = b
            "MEMORY" -> ramText = b
            "BATTERY" -> batteryText = b
            "STORAGE" -> storageText = b
            "NETWORK TEST" -> pingText = b
        }
        return b
    }

    private fun addButton(root: LinearLayout, label: String, subtitle: String, action: () -> Unit) {
        val b = Button(this).apply {
            text = "$label\n$subtitle"
            textSize = 13f
            isAllCaps = false
            setTextColor(white)
            setBackgroundColor(Color.rgb(34, 49, 82))
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setOnClickListener { action() }
        }
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        root.addView(b, lp)
    }

    private fun refreshStats() {
        try {
            val runtime = Runtime.getRuntime()
            val used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            val max = runtime.maxMemory() / (1024 * 1024)
            ramText.text = "App memory used: ${used} MB • app heap limit: ${max} MB\nAndroid-wide free RAM is not fully exposed to ordinary apps."
        } catch (_: Exception) {
            ramText.text = "Memory details unavailable."
        }

        try {
            val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val intent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val temp = if (tempTenths > 0) String.format("%.1f°C", tempTenths / 10.0) else "not reported"
            batteryText.text = "Charge: ${if (level >= 0) "$level%" else "unavailable"} • Battery temperature: $temp"
        } catch (_: Exception) {
            batteryText.text = "Battery details unavailable."
        }

        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val free = stat.availableBytes / (1024 * 1024 * 1024)
            val total = stat.totalBytes / (1024 * 1024 * 1024)
            storageText.text = "Free: about ${free} GB • Total: about ${total} GB"
        } catch (_: Exception) {
            storageText.text = "Storage details unavailable."
        }
        statusText.text = "Ready • Stats refreshed"
    }

    private fun launchGame() {
        val candidates = listOf("com.mobile.legends", "com.mobile.legends.hwag")
        for (pkg in candidates) {
            val launch = packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                startActivity(launch)
                statusText.text = "Launching Mobile Legends"
                return
            }
        }
        Toast.makeText(this, "Mobile Legends wasn't found. Open it manually once, or check your game region/version.", Toast.LENGTH_LONG).show()
        statusText.text = "Game not detected"
    }

    private fun runPingTest() {
        pingText.text = "Testing…"
        executor.execute {
            val start = System.nanoTime()
            val result = try {
                val reachable = InetAddress.getByName("1.1.1.1").isReachable(2500)
                val ms = (System.nanoTime() - start) / 1_000_000
                if (reachable) "Basic response: ${ms} ms" else "No ICMP reply. This doesn't necessarily mean the internet is down."
            } catch (e: Exception) {
                "Test failed: check internet connection"
            }
            runOnUiThread {
                pingText.text = result + "\nThis is not the in-game server ping."
            }
        }
    }

    private fun openSettings(action: String, data: String? = null) {
        try {
            val i = Intent(action)
            if (data != null) i.data = Uri.parse(data)
            startActivity(i)
        } catch (_: Exception) {
            Toast.makeText(this, "This settings page isn't available on this Android version.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }
}
