package com.denisu.homesorter.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.denisu.homesorter.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.denisu.homesorter.model.Containers
import com.denisu.homesorter.sharedPreferences
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.math.log


class OptionsView : AppCompatActivity() {

    private var context = this
    private lateinit var signInButton: SignInButton
    private lateinit var clearButton: Button
    private lateinit var syncButton: Button
    private lateinit var receiveButton: Button
    private lateinit var logoutButton: Button
    private lateinit var textView2: TextView

    private val TAG = "DriveSync"
    private val APPLICATION_NAME = "HomeSorter"
    private val FOLDER_NAME = "HomeSorter"
    private val DATABASE_NAME = "database.db"
    private val SHARED_PREF_KEY = "novoId"
    private lateinit var token: GoogleIdTokenCredential
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        title = "Opções"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        clearButton = findViewById<Button>(R.id.buttonClear)
        syncButton = findViewById<Button>(R.id.syncButton)
        logoutButton = findViewById<Button>(R.id.logoutButton)
        textView2 = findViewById<TextView>(R.id.textView2)
        receiveButton = findViewById<Button>(R.id.receiveButton)

        findViewById<TextView>(R.id.textViewId).setText("(DEBUG) ID Atual: "+ Containers.novoid)
        setSupportActionBar(toolbar)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val credentialManager = CredentialManager.create(this)

        updateUIWithSignedOutUser()
        lifecycleScope.launch {
            checkIfSignedInWithGoogleAsync(credentialManager)
        }

        signInButton.setOnClickListener {
            lifecycleScope.launch {
                signInWithGoogleAsync(credentialManager)
            }
        }

        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                googleLogOut(credentialManager)
            }
            updateUIWithSignedOutUser()
        }

        clearButton.setOnClickListener {
            showClearDataConfirmationDialog()
        }

        receiveButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
            } else {
                downloadDriveFiles(context)
            }
        }

        syncButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
            } else {
                requestQueue = Volley.newRequestQueue(this)
                driveSync(context)
            }
        }

    }

    private suspend fun signInWithGoogleAsync(credentialManager: CredentialManager) {

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id))
            .build()


        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()


        try {
            val result = credentialManager.getCredential(this, request)

            val credential = result.credential


            when (credential) {

                // GoogleIdToken credential
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            updateUIWithSignedInUser(googleIdTokenCredential.displayName)
                            token = googleIdTokenCredential
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(TAG, "Received an invalid google id token response", e)
                        }
                    } else {
                        Log.e(TAG, "Unexpected type of credential")
                    }
                }

                else -> {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }
        } catch (e: GetCredentialException) {
            Log.d(TAG, "Exception :", e)
        }
    }

    private suspend fun checkIfSignedInWithGoogleAsync(credentialManager: CredentialManager) {

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(this, request)

            val credential = result.credential

            when (credential) {

                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            updateUIWithSignedInUser(googleIdTokenCredential.displayName)
                            token = googleIdTokenCredential
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(TAG, "Received an invalid google id token response", e)
                        }
                    } else {
                        Log.e(TAG, "Unexpected type of credential")
                    }
                }

                else -> {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }
        } catch (e: GetCredentialException) {
            Log.d(TAG, "Exception :", e)
        }
    }

    suspend fun googleLogOut(credentialManager: CredentialManager)
    {
        val request = ClearCredentialStateRequest()

        try {
            credentialManager.clearCredentialState(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credential state", e)
        }
    }

    fun driveSync(context: Context) {
        val filesDir = context.filesDir
        val databasePath = context.getDatabasePath(DATABASE_NAME).absolutePath
        val databaseFile = File(databasePath)
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val novoId = sharedPreferences.getInt("novoId", 0)
        val sharedPrefFile = File(context.filesDir, "novoId.txt")
        sharedPrefFile.writeText(novoId.toString())

        val photoFiles = filesDir.listFiles { _, name -> name.startsWith("img-") }

        uploadFile(context, databaseFile,
            onSuccess = {
                Log.d(TAG, "Sucesso")
            },
            onFailure = { errorMessage ->
                Log.d(TAG, "Falha")
            }
        )

        uploadFile(context, sharedPrefFile,
            onSuccess = {
                Log.d(TAG, "Sucesso")
            },
            onFailure = { errorMessage ->
                Log.d(TAG, "Falha")
            }
        )

        photoFiles?.forEach { file ->
            uploadFile(context, file,
                onSuccess = {
                    Log.d(TAG, "Sucesso ao enviar ${file.name} para o Google Drive")
                },
                onFailure = { errorMessage ->
                    Log.d(TAG, "Falha ao enviar ${file.name} para o Google Drive: $errorMessage")
                }
            )
        }

    }

    fun uploadFile(context: Context, file: File, onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        val authorizationRequest =
            AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                .build()

        Identity.getAuthorizationClient(context)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    // Access needs to be granted by the user
                    val pendingIntent = authorizationResult.pendingIntent
                    val intentSender = pendingIntent?.getIntentSender()

                    try {
                        if (intentSender != null) {
                            ActivityCompat.startIntentSenderForResult(
                                context as Activity, // Cast context to Activity
                                intentSender,
                                999,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("DriveUploader", "Couldn't start Authorization UI", e)
                        onFailure("Failed to authorize")
                    }
                } else {
                    // Access already granted, proceed with upload
                    val accessToken = authorizationResult.accessToken

                    // Implement upload logic using access token and file
                    if (accessToken != null) {
                        uploadToDrive(context, file, accessToken, onSuccess, onFailure)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DriveUploader", "Failed to authorize", e)
                onFailure("Failed to authorize")
            }
    }

    private fun uploadToDrive(context: Context, file: File, accessToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
        val fileName = file.name

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("metadata", null, RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{\"name\":\"homesorter-$fileName\"}"))
            .addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), file))
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Upload bem-sucedido!", Toast.LENGTH_SHORT).show()
                    }
                    onSuccess()
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Falha ao fazer o upload: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                    onFailure("Falha ao fazer o upload: ${response.message()}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Falha ao fazer o upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                onFailure("Falha ao fazer o upload: ${e.message}")
            }
        })
    }

    fun downloadDriveFiles(context: Context) {
        val authorizationRequest =
            AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                .build()

        Identity.getAuthorizationClient(context)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    // Access needs to be granted by the user
                    val pendingIntent = authorizationResult.pendingIntent
                    val intentSender = pendingIntent?.getIntentSender()

                    try {
                        if (intentSender != null) {
                            ActivityCompat.startIntentSenderForResult(
                                context as Activity, // Cast context to Activity
                                intentSender,
                                998,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("DriveDownloader", "Couldn't start Authorization UI", e)
                    }
                } else {
                    val accessToken = authorizationResult.accessToken

                    if (accessToken != null) {
                        val url = "https://www.googleapis.com/drive/v3/files?q=name%20contains%20'homesorter-'&fields=files(id,name)" // Combined query and fields

                        val request = Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer $accessToken")
                            .get()
                            .build()

                        val client = OkHttpClient()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                if (response.isSuccessful) {
                                    val responseBody = response.body()?.string()
                                    val gson = Gson()
                                    val jsonElement = JsonParser.parseString(responseBody)
                                    val jsonObject = jsonElement.asJsonObject
                                    val filesArray = jsonObject.getAsJsonArray("files")

                                    if (filesArray != null) {
                                        for (jsonFile in filesArray) {
                                            val fileId = jsonFile.asJsonObject["id"].asString
                                            //Log.d("TAG", "ARQUIVO: "+jsonFile.asJsonObject)
                                            val fileName = jsonFile.asJsonObject["name"].asString
                                            downloadAndSaveFile(context, fileId, fileName, accessToken)
                                        }
                                    } else {
                                        Log.e("DriveDownloader", "No files found")
                                    }
                                } else {
                                    Log.e("DriveDownloader", "Failed to list files: ${response.code()}")
                                }
                            }

                            override fun onFailure(call: Call, e: IOException) {
                                Log.e("DriveDownloader", "Failed to list files", e)
                            }
                        })
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DriveDownloader", "Failed to authorize", e)
            }
    }

    fun downloadAndSaveFile(context: Context, driveFileId: String, drivefileName: String, accessToken: String) {
        val url = "https://www.googleapis.com/drive/v3/files/$driveFileId?alt=media"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    //Log.d("TAG", "Headers: "+response.headers())
                    //val fileName = response.header("Content-Disposition")?.split("filename=")?.get(1)?.trim() ?: ""
                    val fileName = drivefileName
                    val inputStream = response.body()!!.byteStream()

                    if (fileName.startsWith("homesorter-")) {
                        val targetFile = getDestinationFile(context, fileName.substringAfter("homesorter-"))
                        val outputStream = FileOutputStream(targetFile)
                        val buffer = ByteArray(1024)
                        var readBytes: Int


                        while (inputStream.read(buffer).also { readBytes = it } != -1) {
                            outputStream.write(buffer, 0, readBytes)
                        }

                        inputStream.close()
                        outputStream.close()

                        Log.d("DriveDownloader", "Downloaded file: $fileName")

                        if (fileName.contains("novoId")) {
                            val fileContent = targetFile.readText()

                            try {
                                val intValue = fileContent.toInt()
                                Log.d("DriveDownloader", "novoId content converted to int: $intValue")
                                sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit() //da classe principal
                                editor.putInt("novoId", intValue)
                                editor.apply()
                            } catch (e: NumberFormatException) {
                                Log.e("DriveDownloader", "Failed to convert novoId content to int: $fileContent", e)
                            }
                        }
                    } else {
                        Log.w("DriveDownloader", "Downloaded file with unexpected name: $fileName")
                    }
                } else {
                    Log.e("DriveDownloader", "Failed to download file: ${response.message()}")
                    // Handle download error (e.g., notify user)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DriveDownloader", "Failed to download file", e)
                // Handle download error (e.g., notify user)
            }
        })
    }

    private fun getDestinationFile(context: Context, fileName: String): File {
        val fileExtension = fileName.substringAfterLast('.')
        val targetDir: File
        //Log.d("TAG", "Database Path Parent:  "+context.getDatabasePath(DATABASE_NAME).parent)
        //Log.d("TAG", "Database Path:         "+context.getDatabasePath(DATABASE_NAME))
        //Log.d("TAG", "Database Path Absolute:"+context.getDatabasePath(DATABASE_NAME).absolutePath)

        when (fileExtension) {
            "db" -> targetDir = context.getDatabasePath(DATABASE_NAME).parent?.let { File(it) }!!
            "jpg", "jpeg", "png" -> targetDir = context.filesDir // Assuming images go to Pictures directory (if available)
            "txt" -> {
                if (fileName.startsWith("myapp-novoid")) {
                    // Handle "myapp-novoid.txt" file
                    val novoIdText = extractNovoIdFromFileName(fileName)
                    val novoId = novoIdText.toIntOrNull()

                    if (novoId != null) {
                        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putInt("novoId", novoId).apply()

                        Log.d("DriveDownloader", "Updated novoId in shared preferences: $novoId")
                    } else {
                        Log.w("DriveDownloader", "Failed to extract valid novoId from filename: $fileName")
                    }

                    targetDir = context.filesDir // Assuming "myapp-novoid.txt" goes to files directory
                } else {
                    targetDir = context.filesDir // Default location for other ".txt" files
                }
            }
            else -> targetDir = context.filesDir // Default location for other file types
        }

        return File(targetDir, fileName)
    }

    private fun extractNovoIdFromFileName(fileName: String): String {
        return fileName.substringAfter("myapp-novoid").replace(".txt", "")
    }




    fun updateUIWithSignedInUser(email: String?) {
        if (email != null) {
            textView2.text = "Logado como: $email"
            signInButton.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
            syncButton.visibility = View.VISIBLE
            receiveButton.visibility = View.VISIBLE

        } else {
            textView2.text = "Não conectado"
            signInButton.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
            syncButton.visibility = View.GONE
            receiveButton.visibility = View.GONE
        }
    }

    fun updateUIWithSignedOutUser() {
        textView2.text = "Não conectado"
        signInButton.visibility = View.VISIBLE
        logoutButton.visibility = View.GONE
        syncButton.visibility = View.GONE
        receiveButton.visibility = View.GONE
    }

    fun showClearDataConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Limpar dados")
        builder.setMessage("Tem certeza que deseja apagar todos os dados do HomeSorter?")
        builder.setPositiveButton("Sim") { _, _ ->
            restartApplication()
        }
        builder.setNegativeButton("Não") { _, _ ->
            // Do nothing
        }
        builder.show()
    }

    //todo isso não ta apagando certo, alguma coisa tá faltando
    fun restartApplication() {

        //limpar a shared preferences
        val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()

        //dropar essa database
        val context: Context = applicationContext
        context.deleteDatabase("database.db")

        //apagar todas as fotos
        val filesDir = applicationContext.filesDir
        val files = filesDir.listFiles()
        if (files != null) {
            for (file in files) {
                file.delete()
            }
        }

        finishAffinity()
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}