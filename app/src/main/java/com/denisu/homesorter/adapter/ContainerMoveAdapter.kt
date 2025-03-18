package com.denisu.homesorter.adapter

import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R
import com.denisu.homesorter.model.Container
import java.io.File

class ContainerMoveAdapter(
    private val results: List<Pair<String, Container>>,
    private val onItemClick: (Container) -> Unit
) : RecyclerView.Adapter<ContainerMoveAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val containerNameTextView: TextView = view.findViewById(R.id.containerNameTextView)
        val containerPathTextView: TextView = view.findViewById(R.id.containerPathTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (path, container) = results[position]

        holder.containerNameTextView.text = container.nome
        holder.containerPathTextView.text = path

        val imageName = "img-"+container.id.toString()+".jpg"
        val file = File(holder.itemView.context.filesDir, imageName)
        try {
            if (file.exists()) {
                holder.imageView.setImageURI(Uri.fromFile(file))
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder)
            }
        } catch (e: Resources.NotFoundException) {}

        holder.itemView.setOnClickListener {
            onItemClick(container)
        }
    }

    override fun getItemCount(): Int = results.size
}