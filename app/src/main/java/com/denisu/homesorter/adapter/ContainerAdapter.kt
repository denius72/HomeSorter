package com.denisu.homesorter.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import java.io.File


// ContainerAdapter.kt
class ContainerAdapter(
    private val containers: List<Container>,
    private val onItemClickListener: (Container) -> Unit,
    private val onEditClickListener: (Container) -> Unit,
    private val onDeleteClickListener: (Container) -> Unit,
    private val onMoveClickListener: (Container) -> Unit
) : RecyclerView.Adapter<ContainerAdapter.ViewHolder>() {

    var isContainer: Boolean = true

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val containerNameTextView: TextView = view.findViewById(R.id.containerNameTextView)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
        val btnMoveContainer: ImageButton = view.findViewById(R.id.btnMoveContainer)
        val imageBackground: ImageView = view.findViewById(R.id.imageViewBackground)
        val badgeTextView: TextView = view.findViewById(R.id.badgeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (isContainer)
        {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container, parent, false)
            return ViewHolder(view)
        }
        else
        {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_item, parent, false)
            return ViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val container = containers[position]

        holder.containerNameTextView.text = container.nome

        holder.badgeTextView.setText(container.contarSubContainers().toString())

        Log.d("TAG3", container.id.toString())

        val imageName = "img-"+container.id.toString()+".jpg"
        val file = File(holder.itemView.context.filesDir, imageName)
        try {
            val resourceId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)

            if (file.exists()) {
                holder.imageBackground.setImageURI(Uri.fromFile(file))
            } else {
                holder.imageBackground.setImageResource(R.drawable.placeholder)
            }
        } catch (e: Resources.NotFoundException) {}

        holder.editButton.setOnClickListener { onEditClickListener(container) }
        holder.deleteButton.setOnClickListener { onDeleteClickListener(container) }
        holder.btnMoveContainer.setOnClickListener { onMoveClickListener(container) }
        holder.itemView.setOnClickListener { onItemClickListener(container) }
    }

    override fun getItemCount(): Int {
        return containers.size
    }

    fun updateContainers(newContainers: List<Container>) {
        //containers.clear()
        //containers.addAll(newContainers)
        notifyDataSetChanged()
    }
}

