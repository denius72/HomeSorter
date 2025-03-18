package com.denisu.homesorter.view

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.denisu.homesorter.DataCallback
import com.denisu.homesorter.R
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.model.CallbackManager
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.google.gson.Gson
import java.io.File

class ContainerEditView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_edit)

        val container = intent.getSerializableExtra("rootContainer") as Container
        val containerId = intent.getIntExtra("containerId", 0)
        val gson = Gson()

        val editTextNome = findViewById<EditText>(R.id.editTextNome)
        val buttonCriar = findViewById<Button>(R.id.buttonCriar)
        val buttonFoto = findViewById<Button>(R.id.buttonAtualizarFoto)
        val foto = findViewById<ImageView>(R.id.imageViewBackground)
        val cadastroController = CadastroController(Containers.containers)
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

        title = "Editando "+container.nome
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        editTextNome.setText(container.nome)
        buttonCriar.setOnClickListener {
            val nome = editTextNome.text.toString()
            //Log.d("TAG", "containers antes: "+gson.toJson(Containers.containers))
            cadastroController.editarContainer(nome,"",containerId)

            //Log.d("TAG", "Container Editado: "+gson.toJson(Containers.containers))
            callbackListener?.onDataReceived("Container Editado")
            finish() // Fecha a atividade após criar o novo container

        }

        buttonFoto.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            Containers.containerCameraId = containerId
            startActivityForResult(intent,121)
            callbackListener?.onDataReceived("Container Editado")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        recreate()
    }

}
