package com.denisu.homesorter

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.controller.ContainerAlarmManager
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.denisu.homesorter.model.DBHelper
import com.denisu.homesorter.view.ContainerDetailsActivity
import com.google.gson.Gson
import java.util.Calendar

lateinit var sharedPreferences: SharedPreferences
class HomeSorter : Application() {

    private lateinit var containerRecyclerView: RecyclerView

    override fun onCreate() {
        super.onCreate()

        Containers.database = DBHelper(this) // Instanciando o DBHelper
        val gson = Gson()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        Containers.novoid = sharedPreferences.getInt("novoId", 0)


        if(Containers.database.getContainers() != null)
            Containers.containers = Containers.database.getContainers()!!

        if(Containers.containers.isEmpty() || Containers.database.getContainers() == null)
        {
            val root = Container("Início",0, true) //cria o primeiro container raiz
            Containers.containers.add(root)

            val cadastroController = CadastroController(Containers.containers)
            //exemplo de cadastro por nome
            cadastroController.cadastrarContainer("casa 1",0, true) //será cadastrado com Id 1
            cadastroController.cadastrarContainer("casa 2",0, true) //será cadastrado com Id 2
            cadastroController.cadastrarContainer("cômodo 1", 1, true)
            cadastroController.cadastrarContainer("cômodo 2", 1, true)
            cadastroController.cadastrarContainer("cômodo 1", 2, true)
            cadastroController.cadastrarContainer("Móvel 1", 3, true)
            cadastroController.cadastrarContainer("Móvel 2", 3, true)
            cadastroController.cadastrarContainer("Móvel 3", 3, true)
            cadastroController.cadastrarContainer("Item 1", 6, false)

            Containers.database.saveContainers(Containers.containers)

            val containerId = 1 // ID do contêiner

        }
/*
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            handleUncaughtException(e)
        }*/

        val rootContainer = Containers.containers.first() // Obtém o único container da lista
        val intent = Intent(this, ContainerDetailsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("rootContainer", rootContainer)
        startActivity(intent)
    }
    private fun handleUncaughtException(e: Throwable) {

        val packageManager = this.packageManager
        val intent = packageManager.getLaunchIntentForPackage(this.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        this.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}