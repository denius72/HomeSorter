package com.denisu.homesorter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.denisu.homesorter.R

class EmptyContainerAdapter : RecyclerView.Adapter<EmptyContainerAdapter.EmptyContainerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyContainerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_empty, parent, false)
        return EmptyContainerViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmptyContainerViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class EmptyContainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }
}