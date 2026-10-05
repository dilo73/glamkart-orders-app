package com.glamkart.orders

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private val intervals = listOf(15, 30, 60)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        prefs = Prefs(this)

        val etDomain = findViewById<EditText>(R.id.et_domain)
        val etToken = findViewById<EditText>(R.id.et_token)
        val swNotif = findViewById<Switch>(R.id.sw_notifications)
        val spInterval = findViewById<Spinner>(R.id.sp_interval)

        etDomain.setText(prefs.storeDomain)
        etToken.setText(prefs.apiToken)
        swNotif.isChecked = prefs.notificationsEnabled

        spInterval.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            intervals.map { "$it min" }
        )
        spInterval.setSelection(intervals.indexOf(prefs.pollMinutes).coerceAtLeast(0))

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            val domain = etDomain.text.toString().trim()
            val token = etToken.text.toString().trim()
            if (token.isBlank()) {
                Toast.makeText(this, "Enter API token first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Testing connection...", Toast.LENGTH_SHORT).show()
            Thread {
                try {
                    val orders = ShopifyApi.fetchOrders(domain, token, 1)
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Connected! Found ${orders.size} recent order(s).",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            prefs.storeDomain = etDomain.text.toString().trim()
            prefs.apiToken = etToken.text.toString().trim()
            prefs.notificationsEnabled = swNotif.isChecked
            prefs.pollMinutes = intervals[spInterval.selectedItemPosition]
            (application as GlamKartApp).schedulePolling()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
