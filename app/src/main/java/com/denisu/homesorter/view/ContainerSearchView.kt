package com.denisu.homesorter.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.adapter.SearchAdapter
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.model.Containers

class ContainerSearchView : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter
    private val cadastroController = CadastroController(Containers.containers)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_search)

        title = "Pesquisar"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        searchEditText = findViewById(R.id.searchEditText)
        searchRecyclerView = findViewById(R.id.searchRecyclerView)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotBlank()) {
                    val searchResults = cadastroController.searchContainers(query)
                    adapter = SearchAdapter(searchResults) { container ->
                        if(container.containerFlag)
                        {
                            val intent = Intent(this@ContainerSearchView, ContainerDetailsActivity::class.java)
                            intent.putExtra("rootContainer", container)
                            intent.putExtra("containerId", container.id)
                            startActivity(intent)
                        }
                        else
                        {
                            val intent = Intent(this@ContainerSearchView, ItemView::class.java)
                            intent.putExtra("rootContainer", container)
                            intent.putExtra("containerId", container.id)
                            startActivity(intent)
                        }
                    }
                    searchRecyclerView.adapter = adapter
                } else {
                    searchRecyclerView.adapter = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}
