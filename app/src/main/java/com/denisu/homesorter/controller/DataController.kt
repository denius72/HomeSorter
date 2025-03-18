package com.denisu.homesorter.controller

import com.denisu.homesorter.DataCallback

class DataController {
    fun fetchData(callback: DataCallback) {
        val data = "Novos dados"
        callback.onDataReceived(data)
    }
}