package io.github.vladimirmi.internetradioplayer.presentation.player.coverart

import android.support.v4.media.MediaMetadataCompat
import io.github.vladimirmi.internetradioplayer.data.service.extensions.artist
import io.github.vladimirmi.internetradioplayer.data.service.extensions.isEmpty
import io.github.vladimirmi.internetradioplayer.data.service.extensions.isNotSupported
import io.github.vladimirmi.internetradioplayer.data.service.extensions.title
import io.github.vladimirmi.internetradioplayer.domain.interactor.CoverArtInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 28.03.2019.
 */

class CoverArtPresenter
@Inject constructor(private val coverArtInteractor: CoverArtInteractor,
                    private val playerInteractor: PlayerInteractor)
    : BasePresenter<CoverArtView>() {

    private var coverArtLoad: Disposable? = null

    override fun onAttach(view: CoverArtView) {
        playerInteractor.metadataObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { loadCoverArt(it) })
                .addTo(viewSubs)
    }

    private fun loadCoverArt(metadata: MediaMetadataCompat) {
        coverArtLoad?.dispose()
        if (metadata.isEmpty() || metadata.isNotSupported()) return
        coverArtLoad = coverArtInteractor.getCoverArtUri(metadata.artist, metadata.title)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.setCoverArt(it) })
    }
}