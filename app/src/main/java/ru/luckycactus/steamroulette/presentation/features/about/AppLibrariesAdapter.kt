package ru.luckycactus.steamroulette.presentation.features.about

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.databinding.ItemAppLibraryBinding
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater

class AppLibrariesAdapter(
    private val items: List<AppLibrary>,
    private val onItemClick: (AppLibrary) -> Unit
) : RecyclerView.Adapter<AppLibrariesAdapter.LibraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder =
        LibraryViewHolder(ItemAppLibraryBinding.inflate(parent.layoutInflater, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class LibraryViewHolder(
        private val binding: ItemAppLibraryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var lib: AppLibrary

        init {
            itemView.setOnClickListener {
                onItemClick(lib)
            }
        }

        fun bind(lib: AppLibrary) {
            binding.apply {
                tvLibraryName.text =
                    itemView.context.getString(R.string.app_library_template, lib.author, lib.name)
                tvLibraryLicense.text = lib.licenseType.title
            }
            this.lib = lib
        }
    }
}
