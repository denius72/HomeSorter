package com.denisu.homesorter.controller

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.denisu.homesorter.R
import com.denisu.homesorter.model.DBHelper


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val message = intent.getStringExtra("message")
        val time = intent.getLongExtra("time", 0)
        val containerId = intent.getIntExtra("containerId", 0)


        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "HomeSorter_Notification"
        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(0,builder.build())
        //deletar do banco aqui
        val dbHelper = DBHelper(context)
        //só remove do banco aqui se não for recorrente
        if(time.toInt() == 0)
            dbHelper.removeScheduledNotificationByContainerId(containerId)
    }
}
