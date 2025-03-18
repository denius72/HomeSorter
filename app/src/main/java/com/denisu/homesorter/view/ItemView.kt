package com.denisu.homesorter.view

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.DataCallback
import com.denisu.homesorter.R
import com.denisu.homesorter.adapter.NotificationsAdapter
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.model.CallbackManager
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.denisu.homesorter.model.ScheduledNotification
import com.google.gson.Gson
import java.io.File

class ItemView : AppCompatActivity(), DataCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_view)

        val container = intent.getSerializableExtra("rootContainer") as Container
        val containerId = intent.getIntExtra("containerId", 0)
        val gson = Gson()

        val editTextNome = findViewById<EditText>(R.id.editTextNome)
        val editTextDescription = findViewById<EditText>(R.id.editTextDescription)
        val buttonCriar = findViewById<Button>(R.id.buttonCriar)
        val buttonFoto = findViewById<Button>(R.id.buttonAtualizarFoto)
        val foto = findViewById<ImageView>(R.id.imageViewBackground)
        val cadastroController = CadastroController(Containers.containers)
        val buttonHistory = findViewById<Button>(R.id.buttonHistory)
        val novaNotification = findViewById<Button>(R.id.buttonAdicionarNotification)
        val miniRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val callbackListener = CallbackManager.getCallbackListener()

        val imageName = "img-"+container.id.toString()+".jpg"
        val file = File(foto.context.filesDir, imageName)
        try {
            if (file.exists()) {
                foto.setImageURI(Uri.fromFile(file))
            } else {
                foto.setImageResource(R.drawable.placeholder)
            }
        } catch (e: Resources.NotFoundException) {}
        title = "Visualizando "+container.nome
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        editTextNome.setText(container.nome)
        editTextDescription.setText(container.description)

        val scheduledNotification = Containers.database.getScheduledNotificationByContainerId(container.id.toLong())
        if (scheduledNotification != null) {
            // Há uma notificação agendada, carregue a mini RecyclerView
            novaNotification.visibility = View.GONE
            miniRecyclerView.visibility = View.VISIBLE

            // Configurar a mini RecyclerView
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            miniRecyclerView.layoutManager = layoutManager
            val list: MutableList<ScheduledNotification> = mutableListOf()
            list.add(scheduledNotification)
            val adapter = NotificationsAdapter(list,this)
            callbackListener?.onDataReceived("Nova notificação")

            miniRecyclerView.adapter = adapter

        } else {
            novaNotification.visibility = View.VISIBLE
            miniRecyclerView.visibility = View.GONE
        }


        buttonCriar.setOnClickListener {
            val nome = editTextNome.text.toString()
            val description = editTextDescription.text.toString()
            //Log.d("TAG", "containers antes: "+gson.toJson(Containers.containers))
            cadastroController.editarContainer(nome,description,containerId)

            //Log.d("TAG", "Container Editado: "+gson.toJson(Containers.containers))
            //callbackListener?.onDataReceived("Container Editado")
            finish() // Fecha a atividade após criar o novo container

        }

        novaNotification.setOnClickListener {
            val intent = Intent(this, NotificationAddActivity::class.java)
            intent.putExtra("containerId", container.id)
            intent.putExtra("containerName", container.nome)
            startActivityForResult(intent, 1)
            //callbackListener?.onDataReceived("Nova notificação")
        }

        buttonFoto.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            Containers.containerCameraId = containerId
            startActivityForResult(intent,121)
            //callbackListener?.onDataReceived("Container Editado")
        }

        buttonHistory.setOnClickListener {
            val intent = Intent(this, LastParentsView::class.java)
            intent.putExtra("containerId", container.id)
            startActivity(intent)
            //callbackListener?.onDataReceived("Container Editado")
        }
        CallbackManager.setCallbackListenerItem(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        recreate()
    }

    override fun onDataReceived(data: String) {
        val novaNotification = findViewById<Button>(R.id.buttonAdicionarNotification)
        val miniRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        novaNotification.visibility = View.GONE
        miniRecyclerView.visibility = View.VISIBLE
        //recreate()
    }

}