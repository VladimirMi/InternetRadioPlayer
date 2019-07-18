package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.net.CoverArtService
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 25.03.2019.
 */

class CoverArtRepository
@Inject constructor(private val service: CoverArtService) {

    fun getCoverArtUri(query: String): Single<String> {
        return service.searchRecordings(query)
                .map { CoverArtService.getCoverArtUri(it.getReleaseGroupId(), CoverArtService.QUALITY_MID) }
                .subscribeOn(Schedulers.io())
    }

}