package com.denisu.homesorter.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.denisu.homesorter.DataCallback
import com.denisu.homesorter.adapter.ContainerAdapter
import com.denisu.homesorter.adapter.EmptyContainerAdapter
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.controller.GestureListener
import com.denisu.homesorter.model.CallbackManager
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.io.File

class ContainerDetailsActivity : AppCompatActivity(), GestureListener.OnGestureListener {

    private lateinit var containerRecyclerView: RecyclerView
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var adapter: ContainerAdapter
    private lateinit var adapterObj: ContainerAdapter
    private var containers = mutableListOf<Container>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gestureListener: GestureListener

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_details)
        val container = intent.getSerializableExtra("rootContainer") as Container

        title = container.nome

        val contId = container.id

        CadastroController(Containers.containers).buscarContainerPorId(container.id)?.subContainers.also {
            if (it != null) {
                containers = it
            }
            else if (containers.isEmpty())
            {
                CadastroController(Containers.containers).buscarSubcontainerPorId(container.id)?.subContainers.also {
                    if (it != null) {
                        containers = it
                    }
                }
            }
        }

        val gson = Gson()
        Log.d("TAG", "Container.subcontainers: "+gson.toJson(container.subContainers))
        Log.d("TAG", "CadastroController: "+gson.toJson(containers))
        //Log.d("TAG", "Container atual: "+container.nome+" id: "+container.id)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        containerRecyclerView = findViewById(R.id.containerRecyclerView)
        itemRecyclerView = findViewById(R.id.itemRecyclerView)
        containerRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        itemRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        gestureListener = GestureListener(this, this)

        toolbar.setOnTouchListener { _, event ->
            gestureListener.onTouchEvent(event)
        }

        if (containers.filter { it.containerFlag }.isEmpty()) {
            containerRecyclerView.adapter = EmptyContainerAdapter()
        } else {

            adapter = ContainerAdapter(containers.filter { it.containerFlag },
                onItemClickListener = { container ->
                    val intent = Intent(this, ContainerDetailsActivity::class.java)
                    intent.putExtra("rootContainer", container)
                    intent.putExtra("containerId", container.id)
                    startActivity(intent)
                },
                onEditClickListener = { container ->
                    val intent = Intent(this, ContainerEditView::class.java)
                    intent.putExtra("rootContainer", container)
                    intent.putExtra("containerId", container.id)
                    CallbackManager.setCallbackListener(callbackListener)
                    startActivity(intent)
                    //Toast.makeText(this, "Editar: ${container.nome}", Toast.LENGTH_SHORT).show()
                },
                onDeleteClickListener = { container ->
                    AlertDialog.Builder(this)
                        .setTitle("Confirmar exclusão")
                        .setMessage("Tem certeza de que deseja excluir: ${container.nome}?")
                        .setPositiveButton("Sim") { dialogInterface, _ ->
                            CadastroController(Containers.containers).excluirSubcontainerPorId(container.id, intent.getIntExtra("containerId", 0))
                            val filePath = File(filesDir, "img-${container.id.toString()}.jpg")
                            if (filePath.exists())
                                filePath.delete()
                            callbackListener.onDataReceived("Container Deletado")
                        }
                        .setNegativeButton("Não") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        .show()
                },
                onMoveClickListener = { container ->
                    val intent = Intent(this, ContainerMoveView::class.java)
                    intent.putExtra("rootContainer", container)
                    intent.putExtra("containerId", container.id)
                    intent.putExtra("containerNome", container.nome)
                    intent.putExtra("containerPai", contId)
                    startActivity(intent)
                    //Toast.makeText(this, "Extra toasty", Toast.LENGTH_SHORT).show()
                }
            )

            containerRecyclerView.adapter = adapter
        }
        //todo significa que é um objeto comum, abra uma view diferente pra ele
        adapterObj = ContainerAdapter(containers.filter { !it.containerFlag },
            onItemClickListener = { container ->

                val intent = Intent(this, ItemView::class.java)
                intent.putExtra("rootContainer", container)
                intent.putExtra("containerId", container.id)
                CallbackManager.setCallbackListener(callbackListener)
                startActivity(intent)
                //Toast.makeText(this, "Editar: ${container.nome}", Toast.LENGTH_SHORT).show()

            },
            onEditClickListener = { container ->
                /*
                val intent = Intent(this, ContainerEditView::class.java)
                intent.putExtra("rootContainer", container)
                intent.putExtra("containerId", container.id)
                CallbackManager.setCallbackListener(callbackListener)
                startActivity(intent)
                //Toast.makeText(this, "Editar: ${container.nome}", Toast.LENGTH_SHORT).show()
                */
            },
            onDeleteClickListener = { container ->
                AlertDialog.Builder(this)
                    .setTitle("Confirmar exclusão")
                    .setMessage("Tem certeza de que deseja excluir: ${container.nome}?")
                    .setPositiveButton("Sim") { dialogInterface, _ ->
                        CadastroController(Containers.containers).excluirSubcontainerPorId(container.id, intent.getIntExtra("containerId", 0))
                        val filePath = File(filesDir, "img-${container.id.toString()}.jpg")
                        if (filePath.exists())
                            filePath.delete()
                        callbackListener.onDataReceived("Container Deletado")
                    }
                    .setNegativeButton("Não") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .show()
            },
            onMoveClickListener = { container ->
                // Abre a tela para mover o contêiner
                val intent = Intent(this, ContainerMoveView::class.java)
                intent.putExtra("rootContainer", container)
                intent.putExtra("containerId", container.id)
                intent.putExtra("containerNome", container.nome)
                intent.putExtra("containerPai", contId)
                startActivity(intent)
                //Toast.makeText(this, "Extra toasty", Toast.LENGTH_SHORT).show()
            }
        )
        adapterObj.isContainer = false
        itemRecyclerView.adapter = adapterObj
        CallbackManager.setCallbackListener(callbackListener)

        //itemRecyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, ContainerCreateView::class.java)
            intent.putExtra("rootContainer", container)
            intent.putExtra("containerId", container.id)
            CallbackManager.setCallbackListener(callbackListener)
            startActivity(intent)

        }

        val searchButton = findViewById<FloatingActionButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            val intent = Intent(this, ContainerSearchView::class.java)
            intent.putExtra("rootContainer", container)
            intent.putExtra("containerId", container.id)
            CallbackManager.setCallbackListener(callbackListener)
            startActivity(intent)
        }

        val notificationButton = findViewById<FloatingActionButton>(R.id.notificationButton)
        notificationButton.setOnClickListener {
            val intent = Intent(this, NotificationViewActivity::class.java)
            //intent.putExtra("rootContainer", container)
            //intent.putExtra("containerId", container.id)
            CallbackManager.setCallbackListener(callbackListener)
            startActivity(intent)
        }

        val optionsButton = findViewById<FloatingActionButton>(R.id.optionsButton)
        optionsButton.setOnClickListener {
            val intent = Intent(this, OptionsView::class.java)
            CallbackManager.setCallbackListener(callbackListener)
            startActivity(intent)
        }

        val homeButton = findViewById<FloatingActionButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            restartApplication(this)
        }

        val textView3 = findViewById<TextView>(R.id.textView3)
        textView3.setOnClickListener {
            val intent = Intent(this, ContainerCreateView::class.java)
            intent.putExtra("rootContainer", container)
            intent.putExtra("containerId", container.id)
            CallbackManager.setCallbackListener(callbackListener)
            startActivity(intent)
        }

        val textView = findViewById<TextView>(R.id.textView)
        textView.setOnClickListener {
            val intent = Intent(this, ContainerCreateView::class.java)
            intent.putExtra("rootContainer", container)
            intent.putExtra("containerId", container.id)
            CallbackManager.setCallbackListener(callbackListener)
            startActivity(intent)
        }

    }

    private val callbackListener = object : DataCallback {
        override fun onDataReceived(data: String) {
            //Log.d("TAG", "Dados recebidos: $data")
            val gson = Gson()

            //todo SUBSTITUIR POR SQLITE
            Containers.database.saveContainers(Containers.containers)
            /*
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("container", gson.toJson(Containers.containers))
            Log.d("TAGBACK", "CONTAINER CALLBACK 1: "+gson.toJson(containers))
            Log.d("TAGBACK", "CONTAINER CALLBACK 2: "+gson.toJson(Containers.containers))
            editor.apply()
            */
            //Log.d("TAG", "Dados do Container: ${gson.toJson(containers)}")
            Log.d("TAGBACK", "Recebeu os seguintes dados para callback: $data")
            containerRecyclerView.invalidate()
            //adapter.updateContainers(containers.filter { it.containerFlag })
            itemRecyclerView.invalidate()
            //adapterObj.updateContainers(containers.filter { !it.containerFlag })
            recreate()
        }
    }

    override fun onSwipeUp() {
        //recreate()
    }

    override fun onSwipeDown() {
        recreate()
    }

    //TODO
    private fun updateToolbarTitle(container: Container) {
        var currentContainer: Container? = container
        var hierarchy = ""

        // Constrói a hierarquia de views concatenando os nomes dos containers
       // while (currentContainer != null) {
            //hierarchy = "${currentContainer.nome} -> $hierarchy"
            //currentContainer = currentContainer.containerPai // Supondo que Container tenha uma referência para o container pai
       // }

        // Define o título na Toolbar ou TextView
        if (currentContainer != null) {
            hierarchy = "${currentContainer.nome} -> $hierarchy"
        }
        supportActionBar?.title = hierarchy
    }

    fun restartApplication(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}
