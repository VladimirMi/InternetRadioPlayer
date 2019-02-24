package io.github.vladimirmi.internetradioplayer.presentation.history

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.dp
import io.github.vladimirmi.internetradioplayer.extensions.visible
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import io.github.vladimirmi.internetradioplayer.presentation.main.SimpleControlsView
import kotlinx.android.synthetic.main.fragment_history.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryFragment : BaseFragment<HistoryPresenter, HistoryView>(), HistoryView, SimpleControlsView {

    private val historyAdapter = HistoryAdapter()

    override val layout = R.layout.fragment_history

    override fun providePresenter(): HistoryPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(HistoryPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        historyRv.layoutManager = LinearLayoutManager(context)
        historyRv.adapter = historyAdapter
        historyAdapter.onItemClickListener = { presenter.selectStation(it) }
        historyAdapter.onAddToFavListener = { presenter.switchFavorite() }
    }

    override fun setHistory(list: List<Pair<Station, Boolean>>) {
        historyAdapter.stations = list
    }

    override fun selectStation(id: String) {
        val position = historyAdapter.selectStation(id)
        historyRv.scrollToPosition(position)
    }

    override fun showControls(visibility: Float) {
        val pb = ((48 * (1 - visibility) + 16) * context!!.dp).toInt()
        historyRv.setPadding(0, historyRv.paddingTop, 0, pb)
    }

    override fun showPlaceholder(show: Boolean) {
        historyRv.visible(!show)
        placeholderView.visible(show)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.context_menu_history && item.itemId == R.id.context_menu_action_delete) {
            historyAdapter.longClickedItem?.let { presenter.deleteHistory(it) }
            return true
        }
        return super.onContextItemSelected(item)
    }
}