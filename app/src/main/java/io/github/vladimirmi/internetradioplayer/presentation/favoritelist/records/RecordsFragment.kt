package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_records.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class RecordsFragment : BaseFragment<RecordsPresenter, RecordsView>(), RecordsView {

    override val layout = R.layout.fragment_records

    private val recordsAdapter = RecordsAdapter()

    override fun providePresenter(): RecordsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(RecordsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        recordsRv.layoutManager = LinearLayoutManager(context)
        recordsRv.adapter = recordsAdapter
        recordsAdapter.onItemClickListener = { presenter.selectRecord(it) }
    }

    //region =============== RecordsView ==============

    override fun setRecords(records: List<Record>) {
        recordsAdapter.records = records
    }

    override fun selectRecord(id: String) {
        val position = recordsAdapter.selectRecord(id)
        recordsRv.scrollToPosition(position)
    }

    //endregion

    override fun getContextSelectedItem() = recordsAdapter.longClickedItem
}