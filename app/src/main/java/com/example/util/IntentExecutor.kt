package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import android.widget.Toast

object IntentExecutor {
    private const val TAG = "IntentExecutor"

    fun execute(context: Context, action: String, parameter: String): Boolean {
        Log.d(TAG, "Executing Intent action: $action with param: $parameter")
        return try {
            when (action.uppercase()) {
                "CALL" -> {
                    // Try to extract numbers from the parameter, or dial whatever was parsed
                    val cleanParam = parameter.filter { it.isDigit() || it == '+' }
                    val telUri = if (cleanParam.isNotEmpty()) {
                        Uri.parse("tel:$cleanParam")
                    } else {
                        Uri.parse("tel:")
                    }
                    val intent = Intent(Intent.ACTION_DIAL, telUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                "ALARM" -> {
                    // Try to parse hour and minute
                    var hour = 7
                    var minute = 0
                    
                    // Simple heuristic parsing of time (e.g. "7:30", "19:00", "8")
                    try {
                        val timeString = parameter.replace(" ", "")
                        if (timeString.contains(":")) {
                            val parts = timeString.split(":")
                            hour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: 7
                            minute = parts[1].filter { it.isDigit() }.toIntOrNull() ?: 0
                        } else {
                            val numeric = timeString.filter { it.isDigit() }.toIntOrNull()
                            if (numeric != null && numeric in 0..23) {
                                hour = numeric
                            }
                        }
                    } catch (pe: Exception) {
                        Log.e(TAG, "Failed to parse time details", pe)
                    }

                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "د رئیس ږغ RAEESTALK Alarm")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                "MAP" -> {
                    val encodedLocation = Uri.encode(parameter)
                    val mapUri = Uri.parse("geo:0,0?q=$encodedLocation")
                    val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                "SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    true
                }
                else -> {
                    Log.d(TAG, "No action executed for action: $action")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch intent for action: $action", e)
            Toast.makeText(context, "کړنه ترسره نشوه. اړوند اپلیکیشن ونه موندل شو.\nAction failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }
}
