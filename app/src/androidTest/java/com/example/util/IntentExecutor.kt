package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.util.Log

object IntentExecutor {
    private const val TAG = "IntentExecutor"

    fun execute(context: Context, action: String, parameter: String): Boolean {
        if (action == "NONE" || action.isEmpty()) return false

        return try {
            when (action.uppercase()) {
                "CALL_PHONE" -> {
                    // برقراری تماس صوتی مستقیم
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:$parameter")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                "SET_ALARM" -> {
                    // تنظیم آلارم ساعت
                    val intent = Intent(Intent.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_MESSAGE, "د رئیس ږغ Assistant")
                        putExtra(AlarmClock.EXTRA_HOUR, 8) // مقدار پیش‌فرض یا تحلیل شده
                        putExtra(AlarmClock.EXTRA_MINUTES, 0)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                "NAVIGATE" -> {
                    // باز کردن مسیریاب و نقشه گوگل
                    val gmmIntentUri = Uri.parse("geo:0,0?q=$parameter")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(mapIntent)
                    true
                }
                else -> {
                    Log.d(TAG, "Unknown action: $action")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute intent action: $action", e)
            false // کرش نمیکند، فقط مقدار واژگون برمی‌گرداند
        }
    }
}
