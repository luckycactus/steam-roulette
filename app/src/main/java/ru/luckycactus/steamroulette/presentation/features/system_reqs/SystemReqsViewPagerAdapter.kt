package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import ru.luckycactus.steamroulette.databinding.ItemSystemReqsBinding
import ru.luckycactus.steamroulette.domain.games.entity.SystemRequirements
import ru.luckycactus.steamroulette.presentation.utils.extensions.layoutInflater
import ru.luckycactus.steamroulette.presentation.utils.extensions.visibility

class SystemReqsViewPagerAdapter(
    private val items: List<SystemRequirements>
) : RecyclerView.Adapter<SystemReqsViewPagerAdapter.SystemReqsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemReqsViewHolder =
        SystemReqsViewHolder(ItemSystemReqsBinding.inflate(parent.layoutInflater, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SystemReqsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun getPageTitle(position: Int): String = items[position].platform.name

    class SystemReqsViewHolder(
        val binding: ItemSystemReqsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SystemRequirements): Unit = with(binding) {
            if (item.minimal != null) {
                tvMinimumReqs.text = HtmlCompat.fromHtml(item.minimal, 0)
                tvMinimumReqs.visibility(true)
            } else {
                tvMinimumReqs.visibility(false)
            }
            if (item.recommended != null) {
                tvRecommendedReqs.text = HtmlCompat.fromHtml(item.recommended, 0)
                tvRecommendedReqs.visibility(true)
            } else {
                tvRecommendedReqs.visibility(false)
            }
        }

    }
}