package com.denisu.homesorter.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.adapter.ContainerMoveAdapter
import com.denisu.homesorter.adapter.SearchAdapter
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.google.gson.Gson
import kotlin.math.log

class LastParentsView : AppCompatActivity() {

    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_parents)

        title = "Histórico de Localização"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Obtém o ID do container da Intent
        val containerId = intent.getIntExtra("containerId", 0)

        val container = CadastroController(Containers.containers).buscarSubcontainerPorId(containerId)?.ultimosParentes
        Log.d("TAG4","container: " + Gson().toJson(CadastroController(Containers.containers).buscarSubcontainerPorId(containerId)))

        Log.d("TAG4","Ultimos parentes: " + Gson().toJson(CadastroController(Containers.containers).buscarSubcontainerPorId(containerId)?.ultimosParentes))
        val resultContainer: MutableList<Pair<String, Container>> = mutableListOf()

        container?.forEach { value ->
            val parentContainer = CadastroController(Containers.containers).buscarSubcontainerPorId(value)
            Log.d("TAG4","Value: " + value)
            Log.d("TAG4","Parent Container: " + parentContainer)
            parentContainer?.let {
                val path = CadastroController(Containers.containers).searchContainerPathById(it.id)
                if(path != null)
                    resultContainer.add(path to it)
            }
        }

        Log.d("TAG4","Result Container: "+Gson().toJson(resultContainer))
        //todo agora eu tenho todos os containers de parentes, alimentar na recyclerview

        // Configura o RecyclerView
        val parentesRecyclerView: RecyclerView = findViewById(R.id.parentsRecyclerView)
        parentesRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SearchAdapter(resultContainer) { container ->
            val intent = Intent(this@LastParentsView, ContainerDetailsActivity::class.java)
            intent.putExtra("rootContainer", container)
            intent.putExtra("containerId", container.id)
            startActivity(intent)
        }
        parentesRecyclerView.adapter = adapter
    }
}
