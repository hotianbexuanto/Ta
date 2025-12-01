package com.hotian.ta

import android.app.Application
import android.util.Log
import java.io.PrintStream

class TaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Filter out MediaTek GED (Graphics Extension Driver) spam logs
        suppressGedLogs()
    }

    /**
     * Suppresses MediaTek GED ioctl error logs that spam logcat.
     * These errors are harmless system-level GPU driver logs that don't affect app functionality.
     */
    private fun suppressGedLogs() {
        try {
            // Create a custom error stream that filters GED logs
            val originalErr = System.err
            val filteredErr = object : PrintStream(originalErr) {
                override fun println(x: String?) {
                    if (x != null && !shouldFilterLog(x)) {
                        super.println(x)
                    }
                }

                override fun print(s: String?) {
                    if (s != null && !shouldFilterLog(s)) {
                        super.print(s)
                    }
                }
            }

            System.setErr(filteredErr)

            // Also set a default uncaught exception handler filter
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                if (!shouldFilterException(throwable)) {
                    defaultHandler?.uncaughtException(thread, throwable)
                }
            }

            Log.d("TaApplication", "GED log filtering initialized")
        } catch (e: Exception) {
            // Silently fail - log filtering is not critical
            e.printStackTrace()
        }
    }

    /**
     * Determines if a log message should be filtered out.
     */
    private fun shouldFilterLog(message: String): Boolean {
        return message.contains("GED") &&
               (message.contains("ged_swd_ioctl_fence_info") ||
                message.contains("Failed to execute ioctl") ||
                message.contains("BridgeID"))
    }

    /**
     * Determines if an exception should be filtered out.
     */
    private fun shouldFilterException(throwable: Throwable): Boolean {
        val message = throwable.message ?: ""
        val stackTrace = throwable.stackTraceToString()
        return shouldFilterLog(message) || shouldFilterLog(stackTrace)
    }
}
