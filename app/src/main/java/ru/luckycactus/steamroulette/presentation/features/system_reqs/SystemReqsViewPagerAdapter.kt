package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.view.ViewGroup
import android.widget.TextView
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
            bindReq(item.minimal, tvMinimumReqs)
            bindReq(item.recommended, tvRecommendedReqs)
        }

        private fun bindReq(req: String?, textView: TextView) {
            if (req != null) {
                textView.text = HtmlCompat.fromHtml(req, 0)
                textView.visibility(true)
            } else {
                textView.visibility(false)
            }
        }

    }
}