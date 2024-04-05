package id.anantyan.tfliteaudio.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.anantyan.tfliteaudio.R

/**
 * Created by Arya Rezza Anantya on 15/03/2024.
 */
fun Context.showNotification(title: String?, message: String?, intent: Intent? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Noise Notifications"
        val description = "Shows notifications whenever starts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("NOISE_NOTIFICATION", name, importance).apply { setDescription(description) }
        (this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.let { manager: NotificationManager ->
            manager.createNotificationChannel(channel)
        }
    }

    var pendingIntent: PendingIntent? = null
    if (intent != null) {
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
    }

    val buider = NotificationCompat.Builder(this, "NOISE_NOTIFICATION")
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setVibrate(longArrayOf(1000, 1000, 1000))
        .setOnlyAlertOnce(true)
        .setAutoCancel(true)
        .setGroup("NoiseDetectionNotification")
        .setGroupSummary(true)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    NotificationManagerCompat.from(this).notify(1, buider.build())
}