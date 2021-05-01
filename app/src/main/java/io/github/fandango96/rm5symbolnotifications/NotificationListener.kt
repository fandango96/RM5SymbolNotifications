package io.github.fandango96.rm5symbolnotifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private lateinit var notifications: MutableSet<String>
    private lateinit var excludedApps: Set<String>
    private lateinit var excludedCategories: Set<String>

    private var screenOn = true

    private lateinit var screenStateReceiver: BroadcastReceiver

    override fun onListenerConnected() {
        Log.i(TAG, "Listener connected")
    }

    override fun onCreate() {
        super.onCreate()

        notifications = hashSetOf()
        resources.run {
            excludedApps = getStringArray(R.array.excludedApps).toSet()
            excludedCategories = getStringArray(R.array.excludedCategories).toSet()
        }

        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        Log.i(TAG, "Screen on")

                        screenOn = true

                        if (notifications.isNotEmpty()) {
                            stopNotification()
                        }
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        Log.i(TAG, "Screen off")

                        screenOn = false

                        if (notifications.isNotEmpty()) {
                            startNotification()
                        }
                    }
                }
            }
        }

        val screenStateReceiverFilter = IntentFilter("screenStateChanged").apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        registerReceiver(screenStateReceiver, screenStateReceiverFilter)

        Log.i(TAG, "Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
        unregisterReceiver(screenStateReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sbn.run {
            val category = sbn.notification.category
            Log.i(TAG, "Notification posted: $key $isClearable $category")

            if (category != null && !excludedCategories.contains(category) &&
                    !excludedApps.contains(packageName)) {
                if (notifications.add(key) && !screenOn) {
                    startNotification()
                }

                Log.i(TAG, "Active notification count: ${notifications.size}")
            } else {
                Log.i(TAG, "Excluded app/category: $packageName, $category")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        sbn.key.let { key ->
            Log.i(TAG, "Notification dismissed: $key")

            if (notifications.contains(key)) {
                notifications.remove(key)

                Log.i(TAG, "Active notification count: ${notifications.size}")

                if (notifications.isEmpty()) {
                    stopNotification()
                }
            }
        }
    }

    private fun startNotification() {
        Log.i(TAG, "Starting symbol notification")

        setSymbolLampEnable(1)
    }

    private fun stopNotification() {
        Log.i(TAG, "Stopping symbol notification")

        setSymbolLampEnable(0)
    }

    private fun setSymbolLampEnable(value: Int) {
        Settings.Global.putInt(contentResolver, "switch_symbol_lamp_enable", value)
    }

    companion object {
        private const val TAG = "rm5symbolnotifications"
    }
}