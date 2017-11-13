package io.github.vladimirmi.radius.presentation.root

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.github.vladimirmi.radius.ui.root.RootActivity
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

@InjectViewState
class RootPresenter
@Inject constructor(private val router: Router,
                    private val mediaBrowserController: MediaBrowserController,
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
        mediaBrowserController.connect()
    }

    override fun onDestroy() {
        mediaBrowserController.disconnect()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val requestCanceled = grantResults.contains(PackageManager.PERMISSION_DENIED) ||
                grantResults.isEmpty() || permissions.isEmpty()
        if (requestCanceled) {
//            showPermissionSnackBar()
        } else if (requestCode == REQUEST_WRITE) {
            nextScreen()
        }
    }

    fun addMedia(uri: Uri) {
        repository.parseStation(uri)
                .subscribeBy(onSuccess = { viewState.showToast(R.string.toast_add_success) },
                        onError = { viewState.showToast(R.string.toast_add_error) })
                .addTo(compDisp)

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
        router.newRootScreen(Screens.MEDIA_SCREEN)
    }
}