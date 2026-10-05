package com.glamkart.orders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OrderPollWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val prefs = Prefs(applicationContext)
        if (!prefs.isConfigured || !prefs.notificationsEnabled) return Result.success()
        return try {
            val orders = ShopifyApi.fetchOrders(prefs.storeDomain, prefs.apiToken)
            if (orders.isEmpty()) return Result.success()
            val newestId = orders.maxOf { it.id }
            val lastSeen = prefs.lastSeenOrderId
            if (lastSeen == 0L) {
                // First run: record newest, don't notify for old orders
                prefs.lastSeenOrderId = newestId
                return Result.success()
            }
            val fresh = orders.filter { it.id > lastSeen }.sortedBy { it.id }
            for (order in fresh) showNotification(order)
            if (fresh.isNotEmpty()) prefs.lastSeenOrderId = newestId
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(order: Order) {
        val ctx = applicationContext
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            ctx, order.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val itemsText = order.items.joinToString(", ") { "${it.title} x${it.quantity}" }
        val notif = NotificationCompat.Builder(ctx, GlamKartApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New order ${order.name} - Rs ${order.totalPrice}")
            .setContentText("${order.customerName} - ${order.customerPhone}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(itemsText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        NotificationManagerCompat.from(ctx).notify(order.id.toInt(), notif)
    }
}
