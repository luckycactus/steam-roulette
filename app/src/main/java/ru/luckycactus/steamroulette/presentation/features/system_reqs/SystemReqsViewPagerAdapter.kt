package ru.luckycactus.steamroulette.presentation.features.system_reqs

import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_system_reqs.*
import ru.luckycactus.steamroulette.R
import ru.luckycactus.steamroulette.presentation.features.system_reqs.model.SystemReqsUiModel
import ru.luckycactus.steamroulette.presentation.utils.inflate
import ru.luckycactus.steamroulette.presentation.utils.visibility

class SystemReqsViewPagerAdapter(
    private val items: List<SystemReqsUiModel>
) : RecyclerView.Adapter<SystemReqsViewPagerAdapter.SystemReqsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemReqsViewHolder =
        SystemReqsViewHolder(parent.inflate(R.layout.item_system_reqs))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SystemReqsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun getPageTitle(position: Int): String = items[position].platform

    class SystemReqsViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: SystemReqsUiModel) {
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