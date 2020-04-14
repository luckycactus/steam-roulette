package ru.luckycactus.steamroulette.presentation.features.about

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_app_library.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.domain.about.entity.AppLibrary
import ru.luckycactus.steamroulette.domain.about.entity.LicenseType
import ru.luckycactus.steamroulette.presentation.utils.inflate

class AppLibrariesAdapter(
    private val items: List<AppLibrary>,
    private val onItemClick: (AppLibrary) -> Unit
) : RecyclerView.Adapter<AppLibrariesAdapter.LibraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder =
        LibraryViewHolder(parent.inflate(R.layout.item_app_library))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class LibraryViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private lateinit var lib: AppLibrary

        init {
            itemView.setOnClickListener {
                onItemClick(lib)
            }
        }

        fun bind(lib: AppLibrary) {
            tvLibraryName.text =
                itemView.context.getString(R.string.app_library_template, lib.author, lib.name)
            tvLibraryLicense.text = when (lib.licenseType) {
                LicenseType.MIT -> "MIT"
                LicenseType.Apache2 -> "Apache 2.0"
                LicenseType.Custom -> "Custom license"
            }
            this.lib = lib
        }
    }
}
