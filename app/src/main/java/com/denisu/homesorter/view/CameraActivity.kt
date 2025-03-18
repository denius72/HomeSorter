package com.denisu.homesorter.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_REQUEST_CODE = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //verificação de permissão
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, abre a câmera
                openCamera()
            } else {
                // Permissão negada, exibe uma mensagem de erro
                Toast.makeText(this, "Permissão para acessar a câmera negada", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) { // A foto foi capturada com sucesso
            val imageBitmap = data?.extras?.get("data") as Bitmap
            saveImageToFile(imageBitmap)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun saveImageToFile(imageBitmap: Bitmap) {
        //val file = File(filesDir, "img-"+Containers.novoid.toString()+".jpg")
        val file = File(filesDir, "img-"+Containers.containerCameraId.toString()+".jpg")
        val fos = FileOutputStream(file)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
    }
}
