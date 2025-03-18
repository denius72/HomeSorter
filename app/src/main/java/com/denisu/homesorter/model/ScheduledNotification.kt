package com.denisu.homesorter.model

data class ScheduledNotification(
    val containerId: Long,
    val otherContainerId: Long,
    val notificationTime: Long,
    val message: String
)