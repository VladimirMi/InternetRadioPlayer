package io.github.vladimirmi.internetradioplayer.presentation.history

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_history.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryFragment : BaseFragment<HistoryPresenter, HistoryView>(), HistoryView {

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
        historyAdapter.onAddToFavListener = { presenter.switchFavorite(it) }
    }

    override fun setHistory(list: List<Pair<Station, Boolean>>) {
        historyAdapter.stations = list
    }

    override fun selectStation(station: Station) {
        historyAdapter.selectStation(station)
    }
}