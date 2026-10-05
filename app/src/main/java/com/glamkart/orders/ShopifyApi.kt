package com.glamkart.orders

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ShopifyApi {

    fun fetchOrders(domain: String, token: String, limit: Int = 20): List<Order> {
        val clean = domain.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
        require(clean.isNotBlank()) { "Store domain is empty" }
        require(token.isNotBlank()) { "API token is empty" }

        val url = URL("https://$clean/admin/api/2024-10/orders.json?status=any&limit=$limit")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("X-Shopify-Access-Token", token)
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 20000
            readTimeout = 20000
        }
        try {
            val code = conn.responseCode
            if (code == 401 || code == 403) {
                throw Exception("Unauthorized (HTTP $code). Check your Admin API token.")
            }
            if (code != 200) throw Exception("Shopify API error: HTTP $code")
            val body = conn.inputStream.bufferedReader().readText()
            val arr = JSONObject(body).optJSONArray("orders") ?: return emptyList()
            return (0 until arr.length()).map { Order.fromJson(arr.getJSONObject(it)) }
        } finally {
            conn.disconnect()
        }
    }
}
