package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseViewGroup
import kotlinx.android.synthetic.main.view_records.view.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class RecordsViewImpl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseViewGroup<RecordsPresenter, RecordsView>(context, attrs, defStyleAttr), RecordsView {

    private val recordsAdapter = RecordsAdapter()

    override fun providePresenter(): RecordsPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(RecordsPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView() {
        recordsRv.layoutManager = LinearLayoutManager(context)
        recordsRv.adapter = recordsAdapter
        recordsAdapter.onItemClickListener = { presenter.selectRecord(it) }
    }

    override fun setRecords(records: List<Record>) {
        recordsAdapter.records = records
    }

    override fun selectRecord(id: String) {
        val position = recordsAdapter.selectRecord(id)
        recordsRv.scrollToPosition(position)
    }

    override fun getContextSelectedItem()  = recordsAdapter.longClickedItem
}