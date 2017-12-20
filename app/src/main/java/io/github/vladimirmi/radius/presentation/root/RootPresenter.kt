package io.github.vladimirmi.radius.presentation.root

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

@InjectViewState
class RootPresenter
@Inject constructor(private val router: Router,
                    private val mediaController: MediaController,
                    private val repository: StationRepository)
    : BasePresenter<RootView>() {

    companion object {
        const val REQUEST_WRITE = 100
    }

    override fun onFirstViewAttach() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (checkAndRequestPermissions(permissions, REQUEST_WRITE)) {
            nextScreen()
        }
        mediaController.connect()
    }

    override fun onDestroy() {
        mediaController.disconnect()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val requestCanceled = grantResults.contains(PackageManager.PERMISSION_DENIED) ||
                grantResults.isEmpty() || permissions.isEmpty()
        if (requestCanceled) {
            //todo implement
//            showPermissionSnackBar()
        } else if (requestCode == REQUEST_WRITE) {
            nextScreen()
        }
    }

    private fun checkAndRequestPermissions(permissions: Array<String>, requestCode: Int): Boolean {
        val rootActivity = attachedViews.first() as RootActivity
        val allGranted = permissions.none {
            ContextCompat.checkSelfPermission(rootActivity, it) == PackageManager.PERMISSION_DENIED
        }
        if (!allGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                rootActivity.requestPermissions(permissions, requestCode)
            } else {
                ActivityCompat.requestPermissions(rootActivity, permissions, requestCode)
            }
        }
        return allGranted
    }

    private fun nextScreen() {
        repository.initStations()
        router.newRootScreen(Router.MEDIA_LIST_SCREEN)
    }

    fun addStation(uri: Uri) {
        Timber.e("addStation: $uri")
        repository.parseStation(uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = { router.navigateTo(Router.STATION_SCREEN) },
                        onError = { viewState.showToast(R.string.toast_add_error) }
                ) //todo more details
                .addTo(compDisp)
    }
}