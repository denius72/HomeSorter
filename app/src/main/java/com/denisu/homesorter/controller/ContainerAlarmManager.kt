package com.denisu.homesorter.controller

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.denisu.homesorter.model.Containers

class ContainerAlarmManager(private val context: Context) {
    private val dbHelper = Containers.database

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(containerId: Int, notificationTime: Long, title: String, message: String, repeatingInterval: Long) {

        val channelId = "HomeSorter_Notification"
        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .setContentTitle("teste")
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("title", title)
        intent.putExtra("message", message)
        intent.putExtra("time", repeatingInterval)
        intent.putExtra("containerId",containerId)

        var pendingIntent = PendingIntent.getActivity(context,121,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            var notificationChannel = notificationManager.getNotificationChannel(channelId)
            if(notificationChannel == null)
            {
                val importance = NotificationManager.IMPORTANCE_HIGH
                notificationChannel = NotificationChannel(channelId,"desc",importance)
                notificationChannel.setLightColor(Color.GREEN)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        //pra disparar a notificação agora
        //notificationManager.notify(0,builder.build())

        pendingIntent = PendingIntent.getBroadcast(context,containerId, intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if(repeatingInterval == 0L)
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
        else
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, notificationTime, repeatingInterval, pendingIntent)

        // Salva a notificação agendada no banco de dados
        dbHelper.saveScheduledNotification(notificationTime, containerId.toLong(), message)
    }

    fun cancelNotification(containerId: Int) {
        // Remove a notificação agendada do banco de dados
        dbHelper.removeScheduledNotification(containerId)

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, containerId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}

