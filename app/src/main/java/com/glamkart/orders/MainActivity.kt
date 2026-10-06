package com.glamkart.orders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var adapter: OrderAdapter
    private lateinit var setupView: LinearLayout
    private lateinit var contentView: LinearLayout
    private lateinit var progress: ProgressBar
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvRevenue: TextView
    private lateinit var tvPending: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)

        setupView = findViewById(R.id.setup_view)
        contentView = findViewById(R.id.content_view)
        progress = findViewById(R.id.progress)
        tvTotalOrders = findViewById(R.id.tv_total_orders)
        tvRevenue = findViewById(R.id.tv_revenue)
        tvPending = findViewById(R.id.tv_pending)

        val rv = findViewById<RecyclerView>(R.id.rv_orders)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter { order ->
            startActivity(Intent(this, OrderDetailActivity::class.java).apply {
                putExtra("order_data", encodeOrder(order))
            })
        }
        rv.adapter = adapter

        findViewById<Button>(R.id.btn_setup).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btn_refresh).setOnClickListener { loadOrders() }

        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        if (!prefs.isConfigured) {
            setupView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
        } else {
            setupView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
            loadOrders()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }
    }

    private fun loadOrders() {
        progress.visibility = View.VISIBLE
        Thread {
            try {
                val orders = ShopifyApi.fetchOrders(prefs.storeDomain, prefs.apiToken, 30)
                runOnUiThread {
                    progress.visibility = View.GONE
                    adapter.submit(orders)
                    val revenue = orders.sumOf { it.totalPrice.toDoubleOrNull() ?: 0.0 }
                    val pending = orders.count {
                        val f = it.fulfillmentStatus
                        f == null || f == "unfulfilled"
                    }
                    tvTotalOrders.text = orders.size.toString()
                    tvRevenue.text = "Rs ${"%,.0f".format(revenue)}"
                    tvPending.text = pending.toString()
                    if (orders.isNotEmpty() && prefs.lastSeenOrderId == 0L) {
                        prefs.lastSeenOrderId = orders.maxOf { it.id }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun encodeOrder(order: Order): String {
        val items = order.items.joinToString("|") { "${it.title}~${it.quantity}~${it.price}" }
        return listOf(
            order.id.toString(),
            order.name,
            order.createdAt,
            order.totalPrice,
            order.currency,
            order.financialStatus,
            order.fulfillmentStatus ?: "",
            order.customerName,
            order.customerPhone,
            order.customerEmail,
            order.address,
            items
        ).joinToString("§")
    }

    inner class OrderAdapter(private val onClick: (Order) -> Unit) :
        RecyclerView.Adapter<OrderAdapter.VH>() {

        private var items: List<Order> = emptyList()

        fun submit(list: List<Order>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        override fun getItemCount() = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            fun bind(o: Order) {
                itemView.findViewById<TextView>(R.id.tv_order_name).text = o.name
                itemView.findViewById<TextView>(R.id.tv_order_customer).text = o.customerName
                itemView.findViewById<TextView>(R.id.tv_order_amount).text = "Rs ${o.totalPrice}"
                val status = itemView.findViewById<TextView>(R.id.tv_order_status)
                status.text = if (o.fulfillmentStatus == "fulfilled") "Shipped" else "Pending"
                itemView.setOnClickListener { onClick(o) }
            }
        }
    }
}
