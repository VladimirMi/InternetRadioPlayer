package io.github.vladimirmi.internetradioplayer.presentation.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.model.RecentSuggestion
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import kotlinx.android.synthetic.main.item_suggestion.view.*

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

private const val RECENT_SUGGESTION = 0
private const val REGULAR_SUGGESTION = 1

class SearchSuggestionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var suggestions: List<Suggestion> = emptyList()

    fun setData(list: List<Suggestion>) {
        suggestions = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (suggestions[position] is RecentSuggestion) RECENT_SUGGESTION
        else REGULAR_SUGGESTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            RECENT_SUGGESTION -> RecentSuggestionVH(inflater.inflate(R.layout.item_suggestion, parent, false))
            REGULAR_SUGGESTION -> RegularSuggestionVH(inflater.inflate(R.layout.item_suggestion, parent, false))
            else -> throw IllegalStateException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecentSuggestionVH -> setupRecentVH(holder, position)
            is RegularSuggestionVH -> setupRegularVH(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    private fun setupRegularVH(holder: RegularSuggestionVH, position: Int) {
        holder.bind(suggestions[position])
    }

    private fun setupRecentVH(holder: RecentSuggestionVH, position: Int) {
        holder.bind(suggestions[position])
    }
}

class RecentSuggestionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(suggestion: Suggestion) {
        itemView.suggestionTv.text = suggestion.value
        itemView.iconIv.setImageResource(R.drawable.ic_history)
    }
}

class RegularSuggestionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(suggestion: Suggestion) {
        itemView.suggestionTv.text = suggestion.value
        itemView.iconIv.setImageResource(R.drawable.ic_search)
    }
}
