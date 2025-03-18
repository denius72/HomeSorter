package com.denisu.homesorter.model

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "database.db"
        private const val DATABASE_VERSION = 1

        // Tabela 'novoID'
        private const val TABLE_NOVO_ID = "novoID"
        private const val COLUMN_NOVO_ID = "id"

        // Tabela 'containers'
        private const val TABLE_CONTAINERS = "containers"
        private const val COLUMN_CONTAINER_DATA = "data"

        // Tabela para notificações agendadas
        private const val TABLE_SCHEDULED_NOTIFICATIONS = "scheduled_notifications"
        private const val COLUMN_NOTIFICATION_ID = "notification_id"
        private const val COLUMN_CONTAINER_ID = "container_id"
        private const val COLUMN_NOTIFICATION_TIME = "notification_time"
        private const val COLUMN_NOTIFICATION_MESSAGE = "notification_message"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE_NOVO_ID = (
                "CREATE TABLE $TABLE_NOVO_ID (" +
                        "$COLUMN_NOVO_ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                        ")")
        db.execSQL(CREATE_TABLE_NOVO_ID)

        val CREATE_TABLE_CONTAINERS = (
                "CREATE TABLE $TABLE_CONTAINERS (" +
                        "$COLUMN_CONTAINER_DATA TEXT" +
                        ")")
        db.execSQL(CREATE_TABLE_CONTAINERS)

        val CREATE_TABLE_SCHEDULED_NOTIFICATIONS = (
                "CREATE TABLE $TABLE_SCHEDULED_NOTIFICATIONS (" +
                        "$COLUMN_NOTIFICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "$COLUMN_CONTAINER_ID INTEGER," +
                        "$COLUMN_NOTIFICATION_TIME INTEGER," +
                        "$COLUMN_NOTIFICATION_MESSAGE TEXT" +
                        ")")
        db.execSQL(CREATE_TABLE_SCHEDULED_NOTIFICATIONS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Atualizações de banco de dados, se necessário
    }

    fun saveContainers(containers: MutableList<Container>) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CONTAINER_DATA, Gson().toJson(containers))
        }
        db.delete(TABLE_CONTAINERS, null, null) // Limpa a tabela antes de inserir
        db.insert(TABLE_CONTAINERS, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getContainers(): MutableList<Container>? {
        val db = readableDatabase
        val cursor = db.query(TABLE_CONTAINERS, null, null, null, null, null, null)
        var containers: MutableList<Container>? = null
        if (cursor != null && cursor.moveToFirst()) {
            val containerData = cursor.getString(cursor.getColumnIndex(COLUMN_CONTAINER_DATA))
            val listType = object : TypeToken<MutableList<Container>>() {}.type
            containers = Gson().fromJson(containerData, listType)
        }
        cursor?.close()
        db.close()
        return containers
    }

    fun saveScheduledNotification(notificationTime: Long, containerId: Long, message: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CONTAINER_ID, containerId)
            put(COLUMN_NOTIFICATION_TIME, notificationTime)
            put(COLUMN_NOTIFICATION_MESSAGE, message)
        }
        val id = db.insert(TABLE_SCHEDULED_NOTIFICATIONS, null, values)
        db.close()
        return id
    }

    fun removeScheduledNotification(containerId: Int) {
        val db = writableDatabase
        val whereClause = "$COLUMN_NOTIFICATION_ID = ?"
        val whereArgs = arrayOf(containerId.toString())
        db.delete(TABLE_SCHEDULED_NOTIFICATIONS, whereClause, whereArgs)
        db.close()
    }

    fun removeScheduledNotificationByContainerId(containerId: Int) {
        val db = writableDatabase
        val whereClause = "$COLUMN_CONTAINER_ID = ?"
        val whereArgs = arrayOf(containerId.toString())
        db.delete(TABLE_SCHEDULED_NOTIFICATIONS, whereClause, whereArgs)
        db.close()
    }
    
    @SuppressLint("Range")
    fun getScheduledNotifications(): List<ScheduledNotification> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCHEDULED_NOTIFICATIONS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_NOTIFICATION_TIME ASC"
        )
        val notifications = mutableListOf<ScheduledNotification>()
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(COLUMN_NOTIFICATION_ID))
                val containerId = it.getLong(it.getColumnIndex(COLUMN_CONTAINER_ID))
                val notificationTime = it.getLong(it.getColumnIndex(COLUMN_NOTIFICATION_TIME))
                val message = it.getString(it.getColumnIndex(COLUMN_NOTIFICATION_MESSAGE))
                notifications.add(ScheduledNotification(id, containerId,notificationTime, message))
            }
        }
        cursor?.close()
        db.close()
        return notifications
    }

    @SuppressLint("Range")
    fun getScheduledNotificationByContainerId(containerId: Long): ScheduledNotification? {
        val db = readableDatabase
        val selection = "$COLUMN_CONTAINER_ID = ?"
        val selectionArgs = arrayOf(containerId.toString())
        val cursor = db.query(
            TABLE_SCHEDULED_NOTIFICATIONS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        var scheduledNotification: ScheduledNotification? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndex(COLUMN_NOTIFICATION_ID))
                val notificationContainerId = it.getLong(it.getColumnIndex(COLUMN_CONTAINER_ID))
                val notificationTime = it.getLong(it.getColumnIndex(COLUMN_NOTIFICATION_TIME))
                val message = it.getString(it.getColumnIndex(COLUMN_NOTIFICATION_MESSAGE))
                scheduledNotification = ScheduledNotification(id, notificationContainerId, notificationTime, message)
            }
        }
        cursor?.close()
        db.close()
        return scheduledNotification
    }


}
