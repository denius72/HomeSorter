package com.denisu.homesorter.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.adapter.ContainerMoveAdapter
import com.denisu.homesorter.adapter.SearchAdapter
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.model.CallbackManager
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers

class ContainerMoveView : AppCompatActivity() {

    private lateinit var editTextNome: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var adapter: ContainerMoveAdapter
    private val cadastroController = CadastroController(Containers.containers)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_move)
        val callbackListener = CallbackManager.getCallbackListener()

        editTextNome = findViewById(R.id.textNome)
        searchEditText = findViewById(R.id.searchEditText)
        searchEditText = findViewById(R.id.searchEditText)
        searchRecyclerView = findViewById(R.id.searchRecyclerView)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        val containers = intent.getSerializableExtra("rootContainer") as Container
        val containerId = intent.getIntExtra("containerId", 0)
        val containerPai = intent.getIntExtra("containerPai", 0)
        val containerNome = intent.getStringExtra("containerNome")

        title = "Movendo "+containerNome
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        editTextNome.text = "Mover $containerNome para:"
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotBlank()) {
                    val searchResults = cadastroController.searchContainersToMove(containerId, query)
                    adapter = ContainerMoveAdapter(searchResults) { container ->
                        cadastroController.moverContainer(containers, container, containerPai)
                        callbackListener?.onDataReceived("Container Editado")
                        finish()
                    }
                    searchRecyclerView.adapter = adapter
                } else {
                    searchRecyclerView.adapter = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}