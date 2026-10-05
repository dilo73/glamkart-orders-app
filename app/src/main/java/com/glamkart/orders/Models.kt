package com.glamkart.orders

import org.json.JSONObject

data class OrderItem(val title: String, val quantity: Int, val price: String)

data class Order(
    val id: Long,
    val name: String,
    val createdAt: String,
    val totalPrice: String,
    val currency: String,
    val financialStatus: String,
    val fulfillmentStatus: String?,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val address: String,
    val items: List<OrderItem>
) {
    companion object {
        fun fromJson(o: JSONObject): Order {
            val customer = o.optJSONObject("customer")
            val ship = o.optJSONObject("shipping_address")
            val items = mutableListOf<OrderItem>()
            val arr = o.optJSONArray("line_items")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val li = arr.getJSONObject(i)
                    items.add(
                        OrderItem(
                            title = li.optString("title"),
                            quantity = li.optInt("quantity"),
                            price = li.optString("price")
                        )
                    )
                }
            }
            val addrParts = listOf(
                ship?.optString("address1").orEmpty(),
                ship?.optString("city").orEmpty(),
                ship?.optString("province").orEmpty(),
                ship?.optString("country").orEmpty()
            ).filter { it.isNotBlank() }
            val custName = listOf(
                customer?.optString("first_name").orEmpty(),
                customer?.optString("last_name").orEmpty()
            ).filter { it.isNotBlank() }.joinToString(" ")
            val phone = customer?.optString("phone").orEmpty()
                .ifBlank { ship?.optString("phone").orEmpty() }
            return Order(
                id = o.optLong("id"),
                name = o.optString("name"),
                createdAt = o.optString("created_at"),
                totalPrice = o.optString("total_price"),
                currency = o.optString("currency"),
                financialStatus = o.optString("financial_status"),
                fulfillmentStatus = o.optString("fulfillment_status")
                    .takeIf { it.isNotBlank() && it != "null" },
                customerName = custName.ifBlank { "Guest" },
                customerPhone = phone,
                customerEmail = customer?.optString("email").orEmpty(),
                address = addrParts.joinToString(", "),
                items = items
            )
        }
    }
}
