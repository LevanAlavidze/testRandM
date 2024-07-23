package com.example.testrickmorty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testrickmorty.databinding.ItemEpisodeBinding

class EpisodeAdapter(
    private val onItemClick: (Int) -> Unit, // Added parameter
    private val onLoadMore: () -> Unit
) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeViewHolder(binding, onItemClick) // Pass onItemClick to ViewHolder
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = getItem(position)
        holder.bind(episode)

        // Load more episodes when the user reaches the end of the list
        if (position == itemCount - 1) {
            onLoadMore()
        }
    }

    class EpisodeViewHolder(
        private val binding: ItemEpisodeBinding,
        private val onItemClick: (Int) -> Unit // Receive onItemClick
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode) {
            binding.episode = episode
            binding.root.setOnClickListener {
                onItemClick(episode.id) // Call onItemClick with episode ID
            }
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }
}
