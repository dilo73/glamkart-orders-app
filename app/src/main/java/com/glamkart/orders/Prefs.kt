package com.glamkart.orders
import android.content.Context
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("glamkart_orders", Context.MODE_PRIVATE)
    var storeDomain: String
        get() = sp.getString("store_domain", DEFAULT_DOMAIN) ?: DEFAULT_DOMAIN
        set(v) = sp.edit().putString("store_domain", v).apply()
    var apiToken: String
        get() = sp.getString("api_token", "") ?: ""
        set(v) = sp.edit().putString("api_token", v).apply()
    var notificationsEnabled: Boolean
        get() = sp.getBoolean("notifications_enabled", true)
        set(v) = sp.edit().putBoolean("notifications_enabled", v).apply()
    var pollMinutes: Int
        get() = sp.getInt("poll_minutes", 15)
        set(v) = sp.edit().putInt("poll_minutes", v).apply()
    var lastSeenOrderId: Long
        get() = sp.getLong("last_seen_order_id", 0L)
        set(v) = sp.edit().putLong("last_seen_order_id", v).apply()
    val isConfigured: Boolean get() = apiToken.isNotBlank()
    companion object {
        const val DEFAULT_DOMAIN = "ihw8jr-t1.myshopify.com"
    }
}
