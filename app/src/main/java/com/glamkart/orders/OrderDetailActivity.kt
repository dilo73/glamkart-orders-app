package com.glamkart.orders

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Order Details"

        val parts = (intent.getStringExtra("order_data") ?: "").split("§")
        if (parts.size < 12) {
            finish()
            return
        }

        setText(R.id.tv_d_name, parts[1])
        setText(R.id.tv_d_total, "Rs ${parts[3]} ${parts[4]}")
        setText(R.id.tv_d_payment, parts[5].replaceFirstChar { it.uppercase() })
        setText(
            R.id.tv_d_fulfill,
            parts[6].ifBlank { "unfulfilled" }.replaceFirstChar { it.uppercase() }
        )
        setText(R.id.tv_d_customer, parts[7])
        setText(R.id.tv_d_phone, parts[8].ifBlank { "-" })
        setText(R.id.tv_d_email, parts[9].ifBlank { "-" })
        setText(R.id.tv_d_address, parts[10].ifBlank { "-" })
        setText(R.id.tv_d_date, parts[2].take(10))
        val itemsText = parts[11].split("|")
            .filter { it.isNotBlank() }
            .joinToString("\n") {
                val p = it.split("~")
                "- ${p.getOrElse(0) { "?" }}  x${p.getOrElse(1) { "1" }}"
            }
        setText(R.id.tv_d_items, itemsText.ifBlank { "-" })
    }

    private fun setText(id: Int, text: String) {
        findViewById<TextView>(id).text = text
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
