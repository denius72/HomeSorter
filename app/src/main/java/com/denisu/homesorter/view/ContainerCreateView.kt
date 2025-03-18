package com.denisu.homesorter.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.denisu.homesorter.R
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.model.CallbackManager
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.google.gson.Gson
import java.io.File

class ContainerCreateView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_create)

        title = "Criar novo"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val containers = intent.getSerializableExtra("rootContainer") as Container
        val containerId = intent.getIntExtra("containerId", 0)
        val gson = Gson()

        val editTextNome = findViewById<EditText>(R.id.editTextNome)
        val buttonCriar = findViewById<Button>(R.id.buttonCriar)
        val toggle = findViewById<Switch>(R.id.objectSwitch)
        val cadastroController = CadastroController(Containers.containers)
        val callbackListener = CallbackManager.getCallbackListener()

        val buttonFoto = findViewById<Button>(R.id.buttonAtualizarFoto)
        val foto = findViewById<ImageView>(R.id.imageViewBackground)


        foto.setImageResource(R.drawable.placeholder)
        /*
        val imageName = "img-"+Containers.novoid.toString()+".jpg"
        val file = File(foto.context.filesDir, imageName)
        try {
            if (file.exists()) {
                foto.setImageURI(Uri.fromFile(file))
            } else {
                foto.setImageResource(R.drawable.placeholder)
            }
        } catch (e: Resources.NotFoundException) {}*/

        buttonCriar.setOnClickListener {
            val nome = editTextNome.text.toString()
            //Log.d("TAG", "containers antes: "+gson.toJson(Containers.containers))
            //todo Tá gerando um novo ID aqui depois da foto.
            cadastroController.cadastrarContainer(nome, containerId, toggle.isChecked)
            //Log.d("TAG", "containers depois: "+gson.toJson(Containers.containers))
/*
            val intent = Intent(this, CameraActivity::class.java)
            Containers.containerCameraId = Containers.novoid
            startActivity(intent)
*/
            callbackListener?.onDataReceived("Container Criado")
            finish() // Fecha a atividade após criar o novo container

        }

        buttonFoto.setOnClickListener {

            val nome = editTextNome.text.toString()
            cadastroController.cadastrarContainer(nome, containerId, toggle.isChecked)
            val intent = Intent(this, CameraActivity::class.java)
            Containers.containerCameraId = Containers.novoid
            startActivityForResult(intent,121)

            callbackListener?.onDataReceived("Container Criado")
            finish()
        }
    }
}
