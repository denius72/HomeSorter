package com.denisu.homesorter.adapter

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.controller.CadastroController
import com.denisu.homesorter.controller.ContainerAlarmManager
import com.denisu.homesorter.model.CallbackManager
import com.denisu.homesorter.model.Containers
import com.denisu.homesorter.model.ScheduledNotification
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsAdapter(private val notifications: MutableList<ScheduledNotification>, private val context: Context) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val containerIdTextView: TextView = itemView.findViewById(R.id.containerIdTextView)
        val notificationTimeTextView: TextView = itemView.findViewById(R.id.notificationTimeTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(position)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }

        private fun showDeleteConfirmationDialog(position: Int) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Deletar Notificação")
            alertDialog.setMessage("Tem certeza que deseja cancelar esta notificação?")
            alertDialog.setPositiveButton("Sim") { _, _ ->
                deleteNotification(position)
                val callbackListener = CallbackManager.getCallbackListener()
                callbackListener?.onDataReceived("data")
            }
            alertDialog.setNegativeButton("Não", null)
            alertDialog.show()
        }

        private fun deleteNotification(position: Int) {
            val notification = notifications[position]
            ContainerAlarmManager(context).cancelNotification(notification.containerId.toInt())
            notifications.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        val timeInMillis = notification.notificationTime // Tempo em milissegundos
        val date = Date(timeInMillis)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        val nome = CadastroController(Containers.containers).buscarSubcontainerPorId(notification.otherContainerId.toInt())?.nome

        val imageName = "img-"+notification.otherContainerId.toString()+".jpg"
        val file = File(holder.itemView.context.filesDir, imageName)
        try {
            if (file.exists()) {
                holder.imageView.setImageURI(Uri.fromFile(file))
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder)
            }
        } catch (e: Resources.NotFoundException) {}

        holder.containerIdTextView.text = "${nome}"
        holder.notificationTimeTextView.text = "Data: $formattedDate"
        holder.messageTextView.text = "Mensagem: ${notification.message}"
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}
