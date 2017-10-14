package io.github.vladimirmi.radius.presentation.root

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.arellomobile.mvp.MvpPresenter
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.ui.root.RootActivity
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootPresenter
@Inject constructor(private val router: Router) : MvpPresenter<RootView>() {

    companion object {
        const val REQUEST_WRITE = 102
    }

    override fun onFirstViewAttach() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (checkAndRequestPermissions(permissions, REQUEST_WRITE)) {
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

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val requestCanceled = grantResults.contains(PackageManager.PERMISSION_DENIED) ||
                grantResults.isEmpty() || permissions.isEmpty()
        if (requestCanceled) {
//            showPermissionSnackBar()
        } else if (requestCode == REQUEST_WRITE) {
            nextScreen()
        }
    }

    private fun nextScreen() {
        router.navigateTo(Screens.MEDIA_SCREEN)
    }
}