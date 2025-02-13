package com.example.testrickmorty.feature.locations.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.feature.locations.models.Location
import com.example.testrickmorty.databinding.ItemLocationBinding

class LocationsAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<Location, LocationsAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.bind(location)
        holder.itemView.setOnClickListener { onItemClick(location.id) }
    }

    class LocationViewHolder(private val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
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
