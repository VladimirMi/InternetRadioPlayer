package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.records

import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

interface RecordsView : BaseView {

    fun setRecords(records: List<Record>)
    fun selectRecord(id: String)
}