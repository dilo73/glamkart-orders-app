package com.glamkart.orders

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class GlamKartApp : Application() {

    companion object {
        const val CHANNEL_ID = "order_alerts"
        const val WORK_NAME = "glamkart_order_poll"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        schedulePolling()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Order Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts for new Shopify orders" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun schedulePolling() {
        val prefs = Prefs(this)
        val minutes = prefs.pollMinutes.coerceAtLeast(15).toLong()
        val req = PeriodicWorkRequestBuilder<OrderPollWorker>(minutes, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, req
        )
    }
}
