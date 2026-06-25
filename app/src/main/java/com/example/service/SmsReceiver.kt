package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (!MessageInterceptorManager.isSmsReaderEnabled) {
            Log.d(TAG, "SMS Interceptor is disabled in settings")
            return
        }

        try {
            if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in messages) {
                    val sender = sms.displayOriginatingAddress ?: "Unknown Sender"
                    val messageBody = sms.messageBody ?: ""
                    
                    // Route to Speaker Manager
                    MessageInterceptorManager.init(context)
                    MessageInterceptorManager.handleInterceptedMessage(sender, messageBody)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving SMS broadcast", e)
        }
    }
}
