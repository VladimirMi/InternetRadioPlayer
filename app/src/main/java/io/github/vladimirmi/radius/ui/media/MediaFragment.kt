package io.github.vladimirmi.radius.ui.media

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.presentation.media.MediaPresenter
import io.github.vladimirmi.radius.presentation.media.MediaView
import io.github.vladimirmi.radius.ui.base.BaseFragment
import io.github.vladimirmi.radius.ui.dialogs.NewStationDialog
import kotlinx.android.synthetic.main.fragment_media.*
import toothpick.Toothpick

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaFragment : BaseFragment(), MediaView, MediaItemCallback {

    override val layoutRes = R.layout.fragment_media
    private val adapter = MediaListAdapter(this)

    private val addAction: (Station) -> Unit = { presenter.addStation(it) }
    private val addMediaDialog: NewStationDialog by lazy { NewStationDialog(view as ViewGroup, addAction) }

    @InjectPresenter lateinit var presenter: MediaPresenter

    @ProvidePresenter
    fun providePresenter(): MediaPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MediaPresenter::class.java).also {
            Toothpick.closeScope(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        media_recycler.layoutManager = LinearLayoutManager(context)
        media_recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        activity?.intent?.data?.let { presenter.addStation(it) }
        activity?.intent = null
    }

    override fun setMediaList(stationList: GroupedList<Station>) {
        adapter.setData(stationList)
    }

    override fun onGroupSelected(group: String) {
        presenter.selectGroup(group)
    }

    override fun onItemSelected(station: Station) {
        presenter.select(station)
    }

    override fun select(station: Station, playing: Boolean) {
        adapter.select(station, playing)
    }

    override fun notifyList() {
        adapter.notifyDataSetChanged()
    }

    override fun openAddDialog(station: Station) {
        addMediaDialog.setupDialog(station)
        addMediaDialog.open()
    }

    override fun closeAddDialog() = addMediaDialog.close()

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
}