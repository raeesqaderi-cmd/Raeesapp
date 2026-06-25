package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private val TAG = "NotificationListener"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (!MessageInterceptorManager.isNotificationReaderEnabled) {
            return
        }

        try {
            val notification = sbn?.notification ?: return
            val extras = notification.extras ?: return

            // Extract title (usually sender) and message text
            val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""

            // Skip empty notifications or self notifications if package is ours
            val packageName = sbn.packageName ?: ""
            if (packageName == this.packageName) {
                return
            }

            if (title.isNotEmpty() && text.isNotEmpty()) {
                Log.d(TAG, "Notification received from [$packageName] - Title: $title, Text: $text")
                
                // Initialize background TTS and read aloud
                MessageInterceptorManager.init(applicationContext)
                MessageInterceptorManager.handleInterceptedMessage(title, text)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error intercepting statusBarNotification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }
}
