package com.denisu.homesorter.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.adapter.NotificationsAdapter
import com.denisu.homesorter.model.Containers
import com.denisu.homesorter.model.ScheduledNotification
import com.google.gson.Gson

class NotificationViewActivity : AppCompatActivity(){
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        title = "Notificações"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationsAdapter = NotificationsAdapter(getScheduledNotifications(),this)

        Log.d("TAG3",Gson().toJson(getScheduledNotifications()))
        Log.d("TAG3","Número de itens:"+notificationsAdapter.getItemCount())
        notificationsAdapter.notifyDataSetChanged()

        recyclerView.adapter = notificationsAdapter
    }

    private fun getScheduledNotifications(): MutableList<ScheduledNotification> {
        val notifications = Containers.database.getScheduledNotifications()
        return notifications.sortedBy { it.notificationTime }.toMutableList()
    }
}