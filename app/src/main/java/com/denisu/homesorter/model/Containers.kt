package com.denisu.homesorter.model

object Containers{
    var containers = mutableListOf<Container>() //precisa persistir
    var novoid = 0 //precisa persistir
    lateinit var database: DBHelper
    var containerFoundId = 0
    var containerCameraId = 0
}