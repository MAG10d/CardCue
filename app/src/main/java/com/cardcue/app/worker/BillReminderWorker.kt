package com.cardcue.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cardcue.app.CardCueApplication
import com.cardcue.app.data.BillStatus
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class BillReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as CardCueApplication
        val repository = application.container.billsRepository

        try {
            val bills = repository.getAllBillsStream().first()
            val unpaidBills = bills.filter { it.status == BillStatus.UNPAID }

            val today = Calendar.getInstance()
            // Reset time to start of day for accurate comparison
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            for (bill in unpaidBills) {
                // Check if bill is overdue
                if (bill.dueDate < System.currentTimeMillis()) {
                    // Already overdue logic handled by UI, but maybe notify once?
                    // For now, focusing on reminders and due day.
                }

                // Check Due Date match
                val dueDate = Calendar.getInstance().apply { timeInMillis = bill.dueDate }
                dueDate.set(Calendar.HOUR_OF_DAY, 0)
                dueDate.set(Calendar.MINUTE, 0)
                dueDate.set(Calendar.SECOND, 0)
                dueDate.set(Calendar.MILLISECOND, 0)

                val diffInMillis = dueDate.timeInMillis - today.timeInMillis
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

                // Check Reminder Offsets (e.g., offsets = [1, 3] means remind 1 day before and 3 days before)
                // If diffDays is in the list, OR if diffDays is 0 (Due Today)

                val shouldNotify = diffDays == 0 || bill.reminderOffsets.contains(diffDays)

                if (shouldNotify) {
                    showNotification(
                        applicationContext,
                        bill.id,
                        "Bill Due Reminder",
                        "${bill.bankName} bill of ₹${bill.totalDue} is due ${if (diffDays == 0) "TODAY" else "in $diffDays days"}."
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun showNotification(context: Context, id: Int, title: String, message: String) {
        // Permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMIS) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val channelId = "bill_reminders"
        val channelName = "Bill Reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for upcoming credit card bills"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon if available
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
}
