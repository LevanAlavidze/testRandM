package com.example.testrickmorty

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.databinding.ItemLocationBinding

class LocationsAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    private val locations = mutableListOf<Location>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
        holder.itemView.setOnClickListener { onItemClick(location.id) }
    }

    override fun getItemCount(): Int = locations.size

    fun submitList(newLocations: List<Location>) {
        locations.clear()
        locations.addAll(newLocations)
        notifyDataSetChanged()
    }


    inner class LocationViewHolder(private val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(location: Location) {
            binding.location = location
            binding.executePendingBindings()
        }
    }
}

class LocationDiffCallback : DiffUtil.ItemCallback<Location>() {
    override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem == newItem
    }
}