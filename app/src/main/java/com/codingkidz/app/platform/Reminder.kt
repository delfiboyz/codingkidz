package com.codingkidz.app.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.codingkidz.app.CodingKidzApp
import com.codingkidz.app.MainActivity
import com.codingkidz.app.R
import com.codingkidz.app.data.today
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Pengingat latihan harian yang lembut, hanya muncul jika anak belum bermain hari itu. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as CodingKidzApp
        val data = app.container.userRepository.current()
        if (data.lastActiveDay == today()) return Result.success()
        val streak = data.currentStreak(today())
        val text = if (streak > 0) "Runtutanmu sudah $streak hari. Yuk jaga apinya tetap menyala! 🔥"
        else "Bito kangen! Yuk selesaikan satu teka-teki coding hari ini 🤖"
        Reminders.show(applicationContext, text)
        return Result.success()
    }
}

object Reminders {
    private const val CHANNEL = "daily_reminder"
    private const val WORK = "daily_reminder"

    fun schedule(context: Context, hour: Int) {
        val now = LocalDateTime.now()
        var next = now.withHour(hour).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next).toMinutes()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK)
    }

    fun show(context: Context, text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Pengingat latihan", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Waktunya latihan coding!")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, notification)
    }
}
