package com.example.chargingmonitor

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.*
import android.view.Gravity
import android.widget.*
import kotlin.math.abs

class MainActivity : Activity() {
    private lateinit var tvWatt: TextView
    private lateinit var tvDetails: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val updater = object : Runnable {
        override fun run() {
            updateBatteryInfo()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(48, 48, 48, 48)
        }
        val title = TextView(this).apply {
            text = "CHARGING MONITOR LITE"
            textSize = 14f
            setTextColor(Color.parseColor("#888888"))
            gravity = Gravity.CENTER
        }
        tvWatt = TextView(this).apply {
            text = "0.00 W"
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#00E676"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }
        tvDetails = TextView(this).apply {
            text = "Memuat sensor..."
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setLineSpacing(12f, 1f)
        }
        layout.addView(title)
        layout.addView(tvWatt)
        layout.addView(tvDetails)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updater)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updater)
    }

    private fun updateBatteryInfo() {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val voltMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

        val currMicro = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val volt = voltMv / 1000.0
        val amp = abs(currMicro) / 1_000_000.0
        val watt = volt * amp
        val temp = tempTenths / 10.0

        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            else -> "Not Charging"
        }
        val sourceStr = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Baterai"
        }

        tvWatt.text = String.format("%.2f W", watt)
        tvWatt.setTextColor(if (status == BatteryManager.BATTERY_STATUS_CHARGING) Color.parseColor("#00E676") else Color.parseColor("#FFAB40"))
        tvDetails.text = """
            Tegangan : ${String.format("%.2f", volt)} V
            Arus     : ${String.format("%.2f", amp)} A
            Suhu     : ${String.format("%.1f", temp)} °C
            Baterai  : $level% ($statusStr)
            Sumber   : $sourceStr
        """.trimIndent()
    }
}
