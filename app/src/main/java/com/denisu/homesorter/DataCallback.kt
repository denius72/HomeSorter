package com.denisu.homesorter

import java.io.Serializable

interface DataCallback : Serializable {
    fun onDataReceived(data: String)
}